package io.islnd.android.islnd.messaging.event;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.models.CommentKey;
import io.islnd.android.islnd.app.models.PostAliasKey;
import io.islnd.android.islnd.app.models.PostKey;
import io.islnd.android.islnd.app.util.ImageUtil;

public class EventProcessor {
    private static final String TAG = EventProcessor.class.getSimpleName();
    private static ContentResolver mContentResolver;

    public static boolean process(Context context, Event event) {
        Log.v(TAG, "processing " + event);
        mContentResolver = context.getContentResolver();
        if (alreadyProcessed(event)) {
            return false;
        }

        int eventType = event.getType();
        switch (eventType) {
            case EventType.CHANGE_DISPLAY_NAME: {
                changeDisplayName(context, (ChangeDisplayNameEvent) event);
                break;
            }
            case EventType.NEW_POST: {
                addPost(context, (NewPostEvent) event);
                break;
            }
            case EventType.DELETE_POST: {
                deletePost(context, (DeletePostEvent) event);
                break;
            }
            case EventType.NEW_COMMENT: {
                addComment(context, (NewCommentEvent) event);
                break;
            }
            case EventType.DELETE_COMMENT: {
                deleteComment(context, (DeleteCommentEvent) event);
                break;
            }
            case EventType.CHANGE_PROFILE_PICTURE: {
                changeProfilePicture(context, (ChangeProfilePictureEvent) event);
                break;
            }
            case EventType.CHANGE_HEADER_PICTURE: {
                changeHeaderPicture(context, (ChangeHeaderPictureEvent) event);
                break;
            }
            case EventType.CHANGE_ABOUT_ME: {
                changeAboutMe(context, (ChangeAboutMeEvent) event);
                break;
            }
            case EventType.CHANGE_ALIAS: {
                changeAlias(context, (ChangeAliasEvent) event);
                break;
            }
        }

        recordEventProcessed(event);
        return true;
    }

    private static void changeAlias(Context context, ChangeAliasEvent event) {
        int userId = DataUtils.getUserIdFromAlias(context, event.getAlias());
        ContentValues values = new ContentValues();
        values.put(IslndContract.AliasEntry.COLUMN_ALIAS, event.getNewAlias());

        mContentResolver.update(
                IslndContract.AliasEntry.buildAliasWithUserId(userId),
                values,
                null,
                null);

        Log.v(TAG, String.format("user %d changed alias from %s to %s",
                userId, event.getAlias(), event.getNewAlias()));
    }

    private static void changeHeaderPicture(Context context, ChangeHeaderPictureEvent event) {
        int userId = DataUtils.getUserIdFromAlias(context, event.getAlias());
        Uri headerPictureUri = ImageUtil.saveBitmapToInternalFromByteArray(
                context,
                event.getHeaderPicture());
        ContentValues values = new ContentValues();
        values.put(
                IslndContract.ProfileEntry.COLUMN_HEADER_IMAGE_URI,
                headerPictureUri.toString());
        mContentResolver.update(
                IslndContract.ProfileEntry.buildProfileUriWithUserId(userId),
                values,
                null,
                null
        );
    }

    private static void changeAboutMe(Context context, ChangeAboutMeEvent event) {
        int userId = DataUtils.getUserIdFromAlias(context, event.getAlias());
        ContentValues values = new ContentValues();
        values.put(IslndContract.ProfileEntry.COLUMN_ABOUT_ME, event.getAboutMe());
        mContentResolver.update(
                IslndContract.ProfileEntry.buildProfileUriWithUserId(userId),
                values,
                null,
                null
        );
    }

    private static void changeProfilePicture(Context context, ChangeProfilePictureEvent event) {
        int userId = DataUtils.getUserIdFromAlias(context, event.getAlias());
        Uri profilePictureUri = ImageUtil.saveBitmapToInternalFromByteArray(
                context,
                event.getProfilePicture());
        ContentValues values = new ContentValues();
        values.put(IslndContract.ProfileEntry.COLUMN_PROFILE_IMAGE_URI, profilePictureUri.toString());
        mContentResolver.update(
                IslndContract.ProfileEntry.buildProfileUriWithUserId(userId),
                values,
                null,
                null
        );
    }

    private static void changeDisplayName(Context context, ChangeDisplayNameEvent event) {
        int userId = DataUtils.getUserIdFromAlias(context, event.getAlias());
        ContentValues values = new ContentValues();
        values.put(
                IslndContract.DisplayNameEntry.COLUMN_DISPLAY_NAME,
                event.getNewDisplayName());

        mContentResolver.update(
                IslndContract.DisplayNameEntry.buildDisplayNameWithUserId(userId),
                values,
                null,
                null
        );
    }

    private static void addPost(Context context, NewPostEvent newPostEvent) {
        int postUserId = DataUtils.getUserIdFromAlias(context, newPostEvent.getAlias());
        ContentValues values = new ContentValues();
        values.put(IslndContract.PostEntry.COLUMN_USER_ID, postUserId);
        values.put(IslndContract.PostEntry.COLUMN_POST_ID, newPostEvent.getPostId());
        values.put(IslndContract.PostEntry.COLUMN_ALIAS, newPostEvent.getAlias());
        values.put(IslndContract.PostEntry.COLUMN_CONTENT, newPostEvent.getContent());
        values.put(IslndContract.PostEntry.COLUMN_TIMESTAMP, newPostEvent.getTimestamp());
        values.put(IslndContract.PostEntry.COLUMN_COMMENT_COUNT, 0);
        context.getContentResolver().insert(
                IslndContract.PostEntry.CONTENT_URI,
                values);

        Log.v(TAG, String.format("adding post id %s for alias %s ",
                newPostEvent.getPostId(), newPostEvent.getAlias()));
    }

    private static void deletePost(Context context, DeletePostEvent deletePostEvent) {
        int postUserId = DataUtils.getUserIdFromAlias(context, deletePostEvent.getAlias());
        PostKey postToDelete = new PostKey(postUserId, deletePostEvent.getPostId());
        DataUtils.deletePost(context, postToDelete);
    }

    private static void addComment(Context context, NewCommentEvent newCommentEvent) {
        //--If the userId was set previously, that takes precendence over the alias
        //--This is a hack to defer comment processing when receiving events
        int commentUserId = newCommentEvent.getUserId();
        if (commentUserId == 0) {
            commentUserId = DataUtils.getUserIdFromAlias(context, newCommentEvent.getAlias());
        }

        int postUserId = 0;
        try {
            postUserId = DataUtils.getUserIdFromPostAuthorAlias(context, newCommentEvent.getPostAuthorAlias());
        } catch (IllegalArgumentException e) {
            Log.v(TAG, e.toString());
            Log.v(TAG, "not friends with post author");
            return;
        }

        ContentValues values = new ContentValues();
        values.put(IslndContract.CommentEntry.COLUMN_POST_AUTHOR_ALIAS, newCommentEvent.getPostAuthorAlias());
        values.put(IslndContract.CommentEntry.COLUMN_POST_ID, newCommentEvent.getPostId());
        values.put(IslndContract.CommentEntry.COLUMN_COMMENT_USER_ID, commentUserId);
        values.put(IslndContract.CommentEntry.COLUMN_COMMENT_ID, newCommentEvent.getCommentId());
        values.put(IslndContract.CommentEntry.COLUMN_CONTENT, newCommentEvent.getContent());
        values.put(IslndContract.CommentEntry.COLUMN_TIMESTAMP, newCommentEvent.getTimestamp());
        context.getContentResolver().insert(
                IslndContract.CommentEntry.CONTENT_URI,
                values);

        if (IslndContract.UserEntry.MY_USER_ID != commentUserId
                && IslndContract.UserEntry.MY_USER_ID == postUserId) {
            DataUtils.insertNewCommentNotification(
                    context,
                    commentUserId,
                    newCommentEvent.getPostId(),
                    newCommentEvent.getTimestamp()
            );
        }

        int commentCount = DataUtils.getCommentCount(
                context,
                newCommentEvent.getPostAuthorAlias(),
                newCommentEvent.getPostId());
        Log.v(TAG, "comment count is " + commentCount);

        Log.v(
                TAG,
                "update comment count for " + newCommentEvent.getPostAuthorAlias() + " " + newCommentEvent.getPostId());
        ContentValues updateValues = new ContentValues();
        updateValues.put(IslndContract.PostEntry.COLUMN_COMMENT_COUNT, commentCount + 1);
        String selection = IslndContract.PostEntry.COLUMN_ALIAS + " = ? AND " +
                IslndContract.PostEntry.COLUMN_POST_ID + " = ?";
        String[] selectionArgs = new String[] {
                newCommentEvent.getPostAuthorAlias(),
                newCommentEvent.getPostId()
        };
        context.getContentResolver().update(
                IslndContract.PostEntry.CONTENT_URI,
                updateValues,
                selection,
                selectionArgs);
    }

    private static void deleteComment(Context context, DeleteCommentEvent deleteCommentEvent) {
        //--If the userId was set previously, that takes precendence over the alias
        //--This is a hack to defer comment processing when receiving events
        int commentUserId = deleteCommentEvent.getUserId();
        if (commentUserId == 0) {
            commentUserId = DataUtils.getUserIdFromAlias(context, deleteCommentEvent.getAlias());
        }

        PostAliasKey postAliasKey = null;
        try {
            postAliasKey = DataUtils.getParentPostFromComment(
                    context,
                    commentUserId,
                    deleteCommentEvent.getCommentId());
        } catch (IllegalArgumentException e) {
            Log.v(TAG, "ignoring delete comment because we don't have the post");
            return;
        }

        CommentKey commentToDelete = new CommentKey(commentUserId, deleteCommentEvent.getCommentId());
        int deletedCount = DataUtils.deleteComment(context, commentToDelete);
        if (deletedCount == 0) {
            Log.d(TAG, "exit from delete comment");
            return;
        }

        int commentCount = DataUtils.getCommentCount(
                context,
                postAliasKey.getPostAuthorAlias(),
                postAliasKey.getPostId());

        ContentValues updateValues = new ContentValues();
        updateValues.put(IslndContract.PostEntry.COLUMN_COMMENT_COUNT, commentCount - 1);
        String selection = IslndContract.PostEntry.COLUMN_ALIAS + " = ? AND " +
                IslndContract.PostEntry.COLUMN_POST_ID + " = ?";
        String[] selectionArgs = new String[] {
                postAliasKey.getPostAuthorAlias(),
                postAliasKey.getPostId()
        };
        context.getContentResolver().update(
                IslndContract.PostEntry.CONTENT_URI,
                updateValues,
                selection,
                selectionArgs);
    }

    private static void recordEventProcessed(Event event) {
        mContentResolver.insert(
                IslndContract.ReceivedEventEntry.buildEventUriWithPseudonymAndEventId(event),
                new ContentValues()
        );
    }

    private static boolean alreadyProcessed(Event event) {
        String[] projection = new String[] {
                IslndContract.ReceivedEventEntry._ID
        };

        Cursor cursor = null;
        boolean alreadyProcessed;
        try {
            cursor = mContentResolver.query(
                    IslndContract.ReceivedEventEntry.buildEventUriWithPseudonymAndEventId(event),
                    projection,
                    null,
                    null,
                    null
            );

            alreadyProcessed = cursor.moveToFirst();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if (alreadyProcessed) {
            Log.v(TAG, "already processed " + event);
        }

        return alreadyProcessed;
    }
}
