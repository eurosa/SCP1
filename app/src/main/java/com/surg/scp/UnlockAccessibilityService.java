package com.surg.scp;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class UnlockAccessibilityService extends AccessibilityService {
    private static final String TAG = "UnlockAccessibility";
    private boolean wasLocked = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName() != null ?
                    event.getPackageName().toString() : "";
            String className = event.getClassName() != null ?
                    event.getClassName().toString() : "";

            Log.d(TAG, "Package: " + packageName + ", Class: " + className);

            // Check if lock screen is showing
            if (packageName.equals("com.android.systemui") &&
                    className.toLowerCase().contains("keyguard")) {
                wasLocked = true;
                Log.d(TAG, "Lock screen detected");
            }
            // Check if we're leaving lock screen (device unlocked)
            else if (wasLocked && !packageName.equals("com.android.systemui") &&
                    !packageName.equals("android") && !packageName.equals(getPackageName())) {
                wasLocked = false;
                Log.d(TAG, "Device unlocked via swipe");

                // Launch the app
                launchApp();
            }
        }
    }

    private void launchApp() {
        try {
            Intent intent = new Intent(this, SplashScreen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("from_unlock", true);
            startActivity(intent);
            Log.d(TAG, "App launched from unlock");
        } catch (Exception e) {
            Log.e(TAG, "Error launching app: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Service interrupted");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Accessibility service connected");

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.DEFAULT;
        info.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        info.notificationTimeout = 100;

        this.setServiceInfo(info);
    }
}
