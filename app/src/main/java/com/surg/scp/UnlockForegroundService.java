package com.surg.scp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class UnlockForegroundService extends Service {
    private static final String TAG = "UnlockService";
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "unlock_service_channel";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Unlock service created");

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Auto Launch Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Keeps auto-launch feature active");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Start as foreground service to prevent being killed
        startForeground(NOTIFICATION_ID, createNotification());
        Log.d(TAG, "Unlock service started");

        // Return sticky to keep service running
        return START_STICKY;
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, DeviceList.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Auto-Launch Active")
                .setContentText("App will launch on unlock")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setAutoCancel(false)
                .setSilent(true)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Unlock service destroyed");
    }
}