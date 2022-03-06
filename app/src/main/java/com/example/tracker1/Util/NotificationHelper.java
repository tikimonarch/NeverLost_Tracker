package com.example.tracker1.Util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.tracker1.R;

public class NotificationHelper extends ContextWrapper {
    public static final String TRACKER_CHANNEL_ID = "com.example.tracker1";
    public static final String TRACKER_CHANNEL_NAME = "NeverLost";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            createChannel();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationChannel trckrChannel = new NotificationChannel(TRACKER_CHANNEL_ID,
                TRACKER_CHANNEL_NAME,NotificationManager.IMPORTANCE_DEFAULT);
        trckrChannel.enableLights(false);
        trckrChannel.enableVibration(true);
        trckrChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(trckrChannel);
    }

    public NotificationManager getManager() {
        if(manager == null)
            manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        return manager;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getRealtimeTrackingNotification(String title, String content, Uri defaultSound) {
        return new Notification.Builder(getApplicationContext(),TRACKER_CHANNEL_ID)
                .setSmallIcon(R.mipmap.app_logo_round)
                .setContentTitle(title)
                .setContentText(content)
                .setSound(defaultSound)
                .setAutoCancel(false);
    }
}
