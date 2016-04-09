package io.islnd.android.islnd.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.islnd.android.islnd.app.database.DataUtils;
import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.database.NotificationType;

public class NotificationHelper {

    private static final String TAG = NotificationHelper.class.getSimpleName();

    private static final int NOTIFICATION_ID = 7403;

    public static void updateNotification(Context context) {
        Log.v(TAG, "updateNotification");

        // Handle notification cancel
        PendingIntent deleteIntent = PendingIntent.getBroadcast(
                context,
                0,
                new Intent(context, NotificationCancelListener.class),
                0);

        // Build active notifications
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        List<SpannableStringBuilder> activeNotifications = getActiveNotifications(context);
        String bigContentTitle = "";
        int notificationCount = activeNotifications.size();
        if (notificationCount == 1) {
            bigContentTitle = context.getString(R.string.notification_big_content_title_single);
        } else {
            bigContentTitle = Integer.toString(notificationCount)
                    + " "
                    + context.getString(R.string.notification_big_content_title);
        }
        inboxStyle.setBigContentTitle(bigContentTitle);

        for (int i = 0; i < notificationCount; ++i) {
            inboxStyle.addLine(activeNotifications.get(i));
        }

        // Build and notify
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(bigContentTitle)
                .setContentText(activeNotifications.get(0))
                .setDeleteIntent(deleteIntent)
                .setStyle(inboxStyle);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public static void cancelNotification() {
        Log.v(TAG, "cancelNotification");
    }

    private static List<SpannableStringBuilder> getActiveNotifications(Context context) {
        List<SpannableStringBuilder> activeNotifications = new ArrayList<>();

        String[] projection = new String[] {
                IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry.COLUMN_NOTIFICATION_USER_ID,
                IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry.COLUMN_NOTIFICATION_TYPE
        };

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(
                    IslndContract.NotificationEntry.CONTENT_URI,
                    projection,
                    IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry.COLUMN_ACTIVE + " = ?",
                    new String[] {Integer.toString(IslndContract.NotificationEntry.ACTIVE)},
                    IslndContract.NotificationEntry.TABLE_NAME + "." + IslndContract.NotificationEntry.COLUMN_TIMESTAMP + " DESC");
            while (cursor.moveToNext()) {
                int userId = cursor.getInt(cursor.getColumnIndex(IslndContract.NotificationEntry.COLUMN_NOTIFICATION_USER_ID));
                int notificationType = cursor.getInt(cursor.getColumnIndex(IslndContract.NotificationEntry.COLUMN_NOTIFICATION_TYPE));
                String displayName = DataUtils.getDisplayName(context, userId);
                activeNotifications.add(buildSpannableNotificationString(
                        context,
                        displayName,
                        notificationType)
                );
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return activeNotifications;
    }

    public static SpannableStringBuilder buildSpannableNotificationString(Context context,
                                                                  String displayName,
                                                                  int notificationType) {
        String contentInfo = "";

        switch (notificationType) {
            case NotificationType.COMMENT:
                contentInfo = displayName + " " + context.getString(R.string.notification_comment_content);
                break;
            case NotificationType.NEW_FRIEND:
                contentInfo = displayName + " " + context.getString(R.string.notification_new_friend_content);
                break;
        }

        SpannableStringBuilder stringBuilder = new SpannableStringBuilder(contentInfo);
        StyleSpan styleSpan = new StyleSpan(android.graphics.Typeface.BOLD);
        stringBuilder.setSpan(styleSpan, 0, displayName.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        return stringBuilder;
    }
}
