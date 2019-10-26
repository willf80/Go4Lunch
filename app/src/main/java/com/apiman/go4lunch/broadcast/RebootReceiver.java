package com.apiman.go4lunch.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.apiman.go4lunch.helpers.NotificationHelper;
import com.apiman.go4lunch.helpers.SettingsHelper;

public class RebootReceiver extends BroadcastReceiver {
    private static final String TAG = "RebootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Notifications alarm started");

        if(SettingsHelper.isNotificationEnabled(context)) {
            NotificationHelper notificationHelper = new NotificationHelper(context);
            notificationHelper.startAlarm();
        }
    }

}