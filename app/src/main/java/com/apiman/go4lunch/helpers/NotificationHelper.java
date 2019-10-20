package com.apiman.go4lunch.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.apiman.go4lunch.broadcast.AppNotificationReceiver;

import java.util.Calendar;

public class NotificationHelper {
    private static final int NOTIFICATION_REQUEST_CODE = 100;
    private PendingIntent mAlarmPendingIntent;
    private Context context;

    public NotificationHelper(Context context) {
        this.context = context;
        configureAlarmManager();
    }

    private void configureAlarmManager() {
        Intent alarmIntent = new Intent(context, AppNotificationReceiver.class);
        mAlarmPendingIntent = PendingIntent.getBroadcast(context, NOTIFICATION_REQUEST_CODE,
                alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private long getTriggersMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);

        return calendar.getTimeInMillis();
    }

    public void startAlarm() {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                getTriggersMillis(),
                AlarmManager.INTERVAL_DAY/*/1000 * 30*/,
                mAlarmPendingIntent);

        SettingsHelper.saveAlarmStatus(context, true);
    }

    public void stopAlarm() {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(mAlarmPendingIntent);
        SettingsHelper.saveAlarmStatus(context, false);
    }
}
