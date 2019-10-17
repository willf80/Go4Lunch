package com.apiman.go4lunch.services;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsHelper {
    private final static String SHARED_PREF_NAME = "myPref";
    private final static String PREF_NOTIFICATION_STATUS = "notificationStatus";
    private final static String PREF_ALARM_STATUS = "alarmStatus";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }

    public static boolean isNotificationEnabled(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getBoolean(PREF_NOTIFICATION_STATUS, true);
    }

    public static boolean isAlarmStarted(Context context) {
        SharedPreferences preferences = getSharedPreferences(context);
        return preferences.getBoolean(PREF_ALARM_STATUS, false);
    }

    public static void saveNotificationStatus(Context context, boolean status) {
        SharedPreferences preferences = getSharedPreferences(context);
        preferences.edit()
                .putBoolean(PREF_NOTIFICATION_STATUS, status)
                .apply();
    }

    static void saveAlarmStatus(Context context, boolean status) {
        SharedPreferences preferences = getSharedPreferences(context);
        preferences.edit()
                .putBoolean(PREF_ALARM_STATUS, status)
                .apply();
    }
}
