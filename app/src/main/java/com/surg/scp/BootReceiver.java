package com.surg.scp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Boot completed received - Action: " + intent.getAction());

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
                "android.intent.action.QUICKBOOT_POWERON".equals(intent.getAction()) ||
                "com.htc.intent.action.QUICKBOOT_POWERON".equals(intent.getAction())) {

            // For Android 10+, use foreground service for better reliability
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent serviceIntent = new Intent(context, BootForegroundService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }
            } else {
                // For older Android versions, directly start the activity
                try {
                    // Wait 3 seconds to ensure system is fully ready
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Intent launchIntent = new Intent(context, SplashScreen.class);
                launchIntent.putExtra("from_boot", true);
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(launchIntent);
                Log.d(TAG, "SplashScreen launched after boot (direct)");
            }
        }
    }
}