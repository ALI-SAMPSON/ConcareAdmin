package io.icode.concareghadmin.application.activities.Notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import io.icode.concareghadmin.application.R;

public class OreoNotification extends ContextWrapper {

    // static variables to hold the channel Id and Name
    private static final String CHANNEL_ID = "io.icode.concaregh.application";
    private static final String CHANNEL_NAME = "application";

    private NotificationManager notificationManager;

    public OreoNotification(Context base) {
        super(base);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // method call
            createChannel();
        }

    }

    @TargetApi(Build.VERSION_CODES.O)
    // Creates notification channel through which notifications can be sent
    public void createChannel() {
        NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);

        notificationChannel.enableLights(false);
        notificationChannel.enableVibration(true);
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        // calling and setting channel
        getManager().createNotificationChannel(notificationChannel);

    }

    // Returns the notification Manager
    public NotificationManager getManager(){
        if(notificationManager == null){
            notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    // Returns the Notification.Builder object
    public Notification.Builder getOreoNotification(String title, String body, PendingIntent pendingIntent,
                                                 Uri soundUri,String icon){

        return new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.notification_icon_round)
                .setSound(soundUri)
                .setAutoCancel(true);


    }

}
