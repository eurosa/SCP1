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

public class BootForegroundService extends Service {
    private static final String TAG = "BootForegroundService";
    private static final int NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "boot_channel";
    private static final String CHANNEL_NAME = "Boot Service";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");

        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Service for launching app on boot");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        // Create notification
        Notification notification = createNotification();

        // Start as foreground service
        startForeground(NOTIFICATION_ID, notification);

        // Launch the app
        launchApp();

        // Stop the service after launching the app
        stopSelf();

        return START_NOT_STICKY;
    }

    private void launchApp() {
        Intent launchIntent = new Intent(this, SplashScreen.class);
        launchIntent.putExtra("from_boot", true);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(launchIntent);
    }

    private Notification createNotification() {
        // Create intent for notification tap
        Intent notificationIntent = new Intent(this, SplashScreen.class);
        notificationIntent.putExtra("from_boot", true);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        // Build notification
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Surgeon Control Panel")
                .setContentText("Starting application...")
                .setSmallIcon(R.mipmap.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

                // 🔥 REQUIRED TO POP APP TO FRONT
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setFullScreenIntent(pendingIntent, true)

                .setAutoCancel(true)
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}