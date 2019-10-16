package com.apiman.go4lunch.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.apiman.go4lunch.services.NotificationHelper;

public class RebootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("RebootReceiver", "Notifications alarm started");

//        SearchArticleParameter searchArticleParameter = SearchArticleUtils.getParameterFromSharedPreferences(context);
//        if(searchArticleParameter.isValidForNotification()) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        notificationHelper.startAlarm();
//        }
    }


}