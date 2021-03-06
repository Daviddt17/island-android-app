package io.islnd.android.islnd.app;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import io.islnd.android.islnd.app.database.IslndContract;
import io.islnd.android.islnd.app.util.Util;

public class SyncAlarm extends BroadcastReceiver {

    private static final String TAG = SyncAlarm.class.getSimpleName();

    private static final int MILLISECONDS_IN_MINUTE = 60000;
    private static final int MILLISECONDS_IN_HOUR = 60 * MILLISECONDS_IN_MINUTE;

    public static final int SYNC_INTERVAL_FIVE_MINUTES = 5 * MILLISECONDS_IN_MINUTE;
    public static final int SYNC_INTERVAL_TEN_MINUTES = 10 * MILLISECONDS_IN_MINUTE;
    public static final int SYNC_INTERVAL_FIFTEEN_MINUTES = 15 * MILLISECONDS_IN_MINUTE;
    public static final int SYNC_INTERVAL_THIRTY_MINUTES = 30 * MILLISECONDS_IN_MINUTE;
    public static final int SYNC_INTERVAL_ONE_HOUR = MILLISECONDS_IN_HOUR;
    public static final int SYNC_INTERVAL_TWO_HOURS = 2 * MILLISECONDS_IN_HOUR;
    public static final int SYNC_INTERVAL_FOUR_HOURS = 4 * MILLISECONDS_IN_HOUR;
    public static final int SYNC_INTERVAL_TWELVE_HOURS = 12 * MILLISECONDS_IN_HOUR;
    public static final int SYNC_INTERVAL_TWENTY_FOUR_HOURS = 24 * MILLISECONDS_IN_HOUR;

    private StopSystemNotificationsReceiver mNotificationsReceiver;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "onReceive");

        if (mNotificationsReceiver == null) {
            mNotificationsReceiver = new StopSystemNotificationsReceiver();
        }

        IntentFilter intentFilter = new IntentFilter(IslndAction.EVENT_SYNC_COMPLETE);
        context.getApplicationContext().registerReceiver(mNotificationsReceiver, intentFilter);

        NotificationHelper.startListening(context);

        Log.d(TAG, "requestSync");
        context.getContentResolver().requestSync(
                Util.getSyncAccount(context),
                IslndContract.CONTENT_AUTHORITY,
                new Bundle());
    }

    public static void setAlarm(Context context, int intervalInMillis) {
        SyncAlarm.cancelAlarm(context);
        Log.v(TAG, "set sync alarm with interval of : " + intervalInMillis + " milliseconds");
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, SyncAlarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + intervalInMillis,
                intervalInMillis,
                pendingIntent);
    }

    public static void cancelAlarm(Context context) {
        Log.v(TAG, "cancel sync alarm");
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmIntent = new Intent(context, SyncAlarm.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        alarmManager.cancel(pendingIntent);
    }

    public static void enableReceiver(Context context) {
        ComponentName receiver = new ComponentName(context, SyncAlarm.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static void disableReceiver(Context context) {
        ComponentName receiver = new ComponentName(context, SyncAlarm.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}
