package com.apiman.go4lunch.broadcast;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.apiman.go4lunch.MainActivity;
import com.apiman.go4lunch.R;
import com.apiman.go4lunch.models.Booking;
import com.apiman.go4lunch.services.FireStoreUtils;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class AppNotificationReceiver extends BroadcastReceiver {


    /**
     * Create and show a simple notification containing the received message.
     *
     * @param messageBody message body.
     */
    private void sendNotification(Context context, String messageBody) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = context.getString(R.string.default_notification_channel_id);
        String channelName = context.getString(R.string.default_notification_channel_name);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(context.getString(R.string.notification_title))
                        .setContentText(messageBody)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(messageBody))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    channelName,
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0, notificationBuilder.build());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("AppNotificationReceiver", "Notifications alarm received");
        getNotificationData(context);
    }

    private Observable<String> getAllWorkmatesNames(QuerySnapshot querySnapshot, String userId) {
        return Observable.fromIterable(querySnapshot.getDocuments())
                .reduce(new StringBuilder(), (s, documentSnapshot) -> {
                    Booking booking = documentSnapshot.toObject(Booking.class);
                    if(booking != null && !Objects.equals(booking.user.uuid, userId)){
                        s.append(booking.user.displayName);
                        s.append(",");
                    }
                    return s;
                })
                .map(stringBuilder -> {
                    if(stringBuilder.length() > 0){
                        stringBuilder = stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                    }

                    return stringBuilder.toString();
                })
                .toObservable();
    }

    private void buildAndShowMessages(Context context, NotificationData notificationData){
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("It's time to go eat!");
        messageBuilder.append("\n");
        messageBuilder.append("Your restaurant ");
        messageBuilder.append("[");
        messageBuilder.append(notificationData.restaurantName);
        messageBuilder.append("] ");
        messageBuilder.append(" at [");
        messageBuilder.append(notificationData.restaurantAddress);
        messageBuilder.append("] wait you.");

        if(notificationData.listOfWorkmates != null && !notificationData.listOfWorkmates.isEmpty()) {
            messageBuilder.append(" Your workmates [");
            messageBuilder.append(notificationData.listOfWorkmates);
            messageBuilder.append("] ");
            messageBuilder.append("will also be present");
        }

        sendNotification(context, messageBuilder.toString());
    }

    private void getNotificationData(Context context) {
        String userId = FireStoreUtils.getCurrentFirebaseUser().getUid();

        FireStoreUtils.getWorkmateBookOfDay(userId)
            .addOnSuccessListener(documentSnapshot -> {
                Booking booking = documentSnapshot.toObject(Booking.class);
                if(booking == null) return;

                getWorkmatesAndBuildNotificationData(context, booking, userId);
            });
    }

    private void    getWorkmatesAndBuildNotificationData(Context context, Booking userBooking, String userId) {
        final NotificationData notificationData = new NotificationData();

        Observable.just(userBooking)
                .flatMap(booking -> {
                    QuerySnapshot querySnapshot = Tasks.await(FireStoreUtils.getTodayBookingCollection()
                            .whereEqualTo(FireStoreUtils.FIELD_PLACE_ID, booking.placeId)
                            .get());

                    return getAllWorkmatesNames(querySnapshot, userId);
                })
                .map(s -> {
                    notificationData.listOfWorkmates = s;
                    notificationData.restaurantAddress = userBooking.restaurantAddress;
                    notificationData.restaurantName = userBooking.restaurantName;
                    return s;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doFinally(() -> buildAndShowMessages(context, notificationData))
                .subscribe();
    }

    class NotificationData {
        String restaurantName;
        String restaurantAddress;
        String listOfWorkmates;
    }
}
