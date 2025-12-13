package com.surg.scp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UnlockReceiver extends BroadcastReceiver {
    private static final String TAG = "UnlockReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Action received: " + action);

        if (action == null) return;

        if (action.equals(Intent.ACTION_BOOT_COMPLETED) ||
                action.equals(Intent.ACTION_REBOOT)) {
            Log.d(TAG, "Device booted, starting unlock service");

            // Start your app or service
            Intent launchIntent = new Intent(context, SplashScreen.class);
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchIntent.putExtra("from_boot", true);
            context.startActivity(launchIntent);

        } else if (action.equals(Intent.ACTION_USER_PRESENT)) {
            Log.d(TAG, "Device unlocked");

            // Only launch if accessibility service is enabled
            if (isAccessibilityEnabled(context)) {
                Intent launchIntent = new Intent(context, SplashScreen.class);
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                launchIntent.putExtra("from_unlock", true);
                context.startActivity(launchIntent);
                Log.d(TAG, "App launched from unlock");
            }
        }
    }

    private boolean isAccessibilityEnabled(Context context) {
        String serviceName = context.getPackageName() + "/.UnlockAccessibilityService";
        try {
            String enabledServices = android.provider.Settings.Secure.getString(
                    context.getContentResolver(),
                    android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            );
            return enabledServices != null && enabledServices.contains(serviceName);
        } catch (Exception e) {
            return false;
        }
    }
}