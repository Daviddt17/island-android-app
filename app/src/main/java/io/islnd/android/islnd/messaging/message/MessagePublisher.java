package io.islnd.android.islnd.messaging.message;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

import java.security.Key;

import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.messaging.crypto.CryptoUtil;
import io.islnd.android.islnd.messaging.crypto.EncryptedMessage;

public class MessagePublisher {
    private static final String TAG = MessagePublisher.class.getSimpleName();

    public static void removeFriend(Context context, int userId) {
        String newAlias = CryptoUtil.createAlias();
        createMessagesForRemainingFriends(context, userId, newAlias);
        DataUtils.updateAlias(context, IslndContract.UserEntry.MY_USER_ID, newAlias);

        createMessageForFriendToRemove(context, userId);
    }

    private static void createMessageForFriendToRemove(Context context, int userId) {
        EncryptedMessage encryptedMessage = buildEncryptedMessageToRemoveFriend(context, userId);
        ContentValues values = new ContentValues();
        values.put(IslndContract.OutgoingMessageEntry.COLUMN_MAILBOX, encryptedMessage.getMailbox());
        values.put(IslndContract.OutgoingMessageEntry.COLUMN_BLOB, encryptedMessage.getBlob());
        context.getContentResolver().insert(
                IslndContract.OutgoingMessageEntry.CONTENT_URI,
                values
        );
        Log.v(TAG, "inserted 'delete me' message");
    }

    @NonNull
    private static EncryptedMessage buildEncryptedMessageToRemoveFriend(Context context, int userId) {
        String friendToRemoveMailbox = DataUtils.getMessageInbox(context, userId);
        Key friendToRemovePublicKey = DataUtils.getPublicKey(context, userId);
        Message deleteMeMessage = MessageBuilder.buildDeleteMeMessage(
                context,
                friendToRemoveMailbox);
        return new EncryptedMessage(deleteMeMessage, friendToRemovePublicKey);
    }

    private static void createMessagesForRemainingFriends(Context context, int userId, String newAlias) {
        ContentValues[] values = createContentValuesForRemainingFriends(context, userId, newAlias);
        int recordsInserted = context.getContentResolver().bulkInsert(
                IslndContract.OutgoingMessageEntry.CONTENT_URI,
                values
        );
        Log.v(TAG, String.format("wrote %d messages", recordsInserted));
    }

    private static ContentValues[] createContentValuesForRemainingFriends(Context context, int userId, String newAlias) {
        String[] projection = new String[] {
                IslndContract.UserEntry.COLUMN_PUBLIC_KEY,
                IslndContract.UserEntry.COLUMN_MESSAGE_INBOX
        };

        Cursor cursor = null;
        try {
            final String selection = IslndContract.UserEntry._ID + " != ? AND " +
                    IslndContract.UserEntry._ID + " != ?";
            cursor = context.getContentResolver().query(
                    IslndContract.UserEntry.CONTENT_URI,
                    projection,
                    selection,
                    new String[] {
                            Integer.toString(IslndContract.UserEntry.MY_USER_ID),
                            Integer.toString(userId)
                    },
                    null);

            ContentValues[] values = new ContentValues[cursor.getCount()];
            int index = 0;
            if (cursor.moveToFirst()) {
                do {
                    values[index] = new ContentValues();
                    EncryptedMessage encryptedMessage = new EncryptedMessage(
                            MessageBuilder.buildNewAliasMessage(
                                    context,
                                    cursor.getString(1),
                                    newAlias
                            ),
                            CryptoUtil.decodePublicKey(cursor.getString(0))
                    );
                    values[index].put(
                            IslndContract.OutgoingMessageEntry.COLUMN_MAILBOX,
                            cursor.getString(1));
                    values[index].put(
                            IslndContract.OutgoingMessageEntry.COLUMN_BLOB,
                            encryptedMessage.getBlob());
                    index++;
                } while (cursor.moveToNext());
            }

            return values;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
