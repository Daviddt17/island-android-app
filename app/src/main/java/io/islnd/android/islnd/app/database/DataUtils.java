package io.islnd.android.islnd.app.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import io.islnd.android.islnd.app.models.CommentKey;
import io.islnd.android.islnd.app.models.PostKey;

import io.islnd.android.islnd.messaging.PseudonymKey;
import io.islnd.android.islnd.messaging.crypto.CryptoUtil;

import java.security.Key;

public class DataUtils {
    public static void insertUser(Context context, PseudonymKey pk) {
        insertUser(context, pk.getUsername(), pk.getPseudonym(), pk.getKey());
    }

    public static void insertUser(Context context, String username, String pseudonym, Key groupKey) {
        ContentValues values = new ContentValues();
        values.put(IslndContract.UserEntry.COLUMN_PSEUDONYM, pseudonym);
        values.put(IslndContract.UserEntry.COLUMN_USERNAME, username);
        values.put(IslndContract.UserEntry.COLUMN_GROUP_KEY, CryptoUtil.encodeKey(groupKey));

        context.getContentResolver().insert(
                IslndContract.UserEntry.CONTENT_URI,
                values);
    }

    public static String getPseudonym(Context context, int userId) {
        String[] projection = new String[] {
                IslndContract.UserEntry.COLUMN_PSEUDONYM,
        };

        Cursor cursor = context.getContentResolver().query(
                IslndContract.UserEntry.CONTENT_URI,
                projection,
                IslndContract.UserEntry._ID + " = ?",
                new String[] {Integer.toString(userId)},
                null);

        try {
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }

            return null;
        } finally {
            cursor.close();
        }
    }

    public static int getUserId(Context context, String username) {
        String[] projection = new String[] {
                IslndContract.UserEntry._ID,
        };

        Cursor cursor = null;

        try {
            cursor = context.getContentResolver().query(
                    IslndContract.UserEntry.CONTENT_URI,
                    projection,
                    IslndContract.UserEntry.COLUMN_USERNAME + " = ?",
                    new String[] {username},
                    null);
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            else {
                return -1;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static int deleteUsers(Context context) {
        return context.getContentResolver().delete(
                IslndContract.UserEntry.CONTENT_URI,
                null,
                null
        );
    }

    public static Key getGroupKey(Context context, int userId) {
        String[] projection = new String[] {
                IslndContract.UserEntry.COLUMN_GROUP_KEY,
        };

        Cursor cursor = context.getContentResolver().query(
                IslndContract.UserEntry.CONTENT_URI,
                projection,
                IslndContract.UserEntry._ID + " = ?",
                new String[] {Integer.toString(userId)},
                null);

        try {
            if (cursor.moveToFirst()) {
                return CryptoUtil.decodeSymmetricKey(cursor.getString(0));
            }

            return null;
        } finally {
            cursor.close();
        }
    }

    public static String getUsernameFromPseudonym(Context context, String pseudonym) {
        String[] projection = new String[] {
                IslndContract.UserEntry.COLUMN_USERNAME,
        };

        Cursor cursor = context.getContentResolver().query(
                IslndContract.UserEntry.CONTENT_URI,
                projection,
                IslndContract.UserEntry.COLUMN_PSEUDONYM + " = ?",
                new String[] {pseudonym},
                null);

        try {
            if (cursor.moveToFirst()) {
                return cursor.getString(0);
            }

            return null;
        } finally {
            cursor.close();
        }
    }

    public static void deletePost(Context context, PostKey postKey) {
        String selection = IslndContract.PostEntry.COLUMN_USER_ID + " = ? AND " +
                IslndContract.PostEntry.COLUMN_POST_ID + " = ?";
        String[] args = new String[] {
                Integer.toString(postKey.getUserId()),
                postKey.getPostId()};
        context.getContentResolver().delete(
                IslndContract.PostEntry.CONTENT_URI,
                selection,
                args);
    }

    public static void deleteComment(ContentResolver contentResolver, CommentKey commentKey) {
        String selection = IslndContract.CommentEntry.COLUMN_COMMENT_USER_ID + " = ? AND " +
                IslndContract.CommentEntry.COLUMN_COMMENT_ID + " = ?";
        String[] args = new String[] {
                Integer.toString(commentKey.getCommentAuthorId()),
                commentKey.getCommentId()};
        contentResolver.delete(
                IslndContract.CommentEntry.CONTENT_URI,
                selection,
                args);
    }

    public static void deleteComment(Context context, CommentKey commentKey) {
        deleteComment(context.getContentResolver(), commentKey);
    }
}
