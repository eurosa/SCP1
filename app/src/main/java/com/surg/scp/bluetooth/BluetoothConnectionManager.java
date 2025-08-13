package com.surg.scp.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BluetoothConnectionManager {
    private static final String TAG = "BluetoothConnection";
    private static final UUID[] SPP_UUIDS = {
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"), // Standard SPP
            UUID.fromString("00001105-0000-1000-8000-00805F9B34FB"), // OBEX
            UUID.fromString("00001124-0000-1000-8000-00805F9B34FB"),  // HID


            UUID.fromString("00001108-0000-1000-8000-00805F9B34FB"), // Headset (HSP)
            UUID.fromString("00001112-0000-1000-8000-00805F9B34FB"), // Generic (HFP)
            UUID.fromString("0000111E-0000-1000-8000-00805F9B34FB")  // Hands-Free (HFP)
    };

    // Connection status constants
    public static final int CONNECTION_SUCCESS = 0;
    public static final int CONNECTION_FAILED = 1;
    public static final int PERMISSION_DENIED = 2;
    public static final int SECURITY_EXCEPTION = 3;
    public static final int IO_EXCEPTION = 4;
    public static final int BLUETOOTH_DISABLED = 5;
    public static final int CONNECTION_LOST = 6;
    public static final int CONNECTION_TIMEOUT = 7;

    private final Context context;
    private final Handler mainHandler;
    private final ExecutorService executorService;
    private BluetoothSocket btSocket;
    private InputStream mmInputStream;
    private OutputStream mmOutputStream;
    private volatile boolean isBtConnected = false;
    private String currentDeviceAddress;
    private String currentDeviceInfo;
    private ConnectionCallback connectionCallback;
    private final BroadcastReceiver bluetoothStateReceiver;
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 5;

    public interface ConnectionCallback {
        void onConnectionResult(int resultCode, String message);
        void onDataReceived(byte[] data);
        void onConnectionLost();
    }

    public BluetoothConnectionManager(Context context) {
        this.context = context.getApplicationContext();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.executorService = Executors.newSingleThreadExecutor();

        this.bluetoothStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    if (state == BluetoothAdapter.STATE_OFF) {
                        disconnect();
                        if (connectionCallback != null) {
                            connectionCallback.onConnectionResult(BLUETOOTH_DISABLED, "Bluetooth was turned off");
                        }
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(bluetoothStateReceiver, filter);
    }

    public void connect(String deviceAddress, String deviceInfo, ConnectionCallback callback) {
        this.connectionCallback = callback;
        this.currentDeviceAddress = deviceAddress;
        this.currentDeviceInfo = deviceInfo;

        executorService.execute(() -> {
            mainHandler.post(() -> {
                if (callback != null) {
                    callback.onConnectionResult(-1, "Connecting...");
                }
            });

            // Check if already connected to this device
            if (validateConnection() && deviceAddress.equals(btSocket.getRemoteDevice().getAddress())) {
                notifyResult(CONNECTION_SUCCESS, "Already connected to this device", callback);
                return;
            }

            // Clean up any existing connection
         ///   disconnect();

            // Check permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(context,
                        Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    notifyResult(PERMISSION_DENIED, "Bluetooth permission required", callback);
                    return;
                }
            }

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                notifyResult(CONNECTION_FAILED, "Bluetooth not supported", callback);
                return;
            }

            if (!bluetoothAdapter.isEnabled()) {
                notifyResult(BLUETOOTH_DISABLED, "Bluetooth is disabled", callback);
                return;
            }

            try {
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
                establishConnection(device, callback);
            } catch (IllegalArgumentException e) {
                notifyResult(CONNECTION_FAILED, "Invalid device address", callback);
            } catch (SecurityException e) {
                notifyResult(SECURITY_EXCEPTION, "Bluetooth permission denied", callback);
            } catch (Exception e) {
                notifyResult(CONNECTION_FAILED, "Connection failed: " + e.getMessage(), callback);
            }
        });
    }

    private boolean validateConnection() {
        if (btSocket == null || !btSocket.isConnected() || mmInputStream == null || mmOutputStream == null) {
            return false;
        }

        try {
            // Simple write to validate connection
            mmOutputStream.write(0x00);
            mmOutputStream.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void establishConnection(BluetoothDevice device, ConnectionCallback callback) throws IOException {
        BluetoothSocket socket = null;
        IOException lastException = null;

        // Try all known UUIDs for SPP devices
        for (UUID uuid : SPP_UUIDS) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                            != PackageManager.PERMISSION_GRANTED) {
                        notifyResult(PERMISSION_DENIED, "Bluetooth permission required", callback);
                        return;
                    }
                    socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                } else {
                    socket = device.createRfcommSocketToServiceRecord(uuid);
                }

                // Cancel discovery to speed up connection
                try {
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                } catch (SecurityException e) {
                    Log.w(TAG, "Couldn't cancel discovery", e);
                }

                // Connect with timeout
                socket.connect();

                // Connection successful
                btSocket = socket;
                mmInputStream = btSocket.getInputStream();
                mmOutputStream = btSocket.getOutputStream();

                // Verify streams
                if (mmInputStream == null || mmOutputStream == null) {
                    throw new IOException("Failed to establish streams");
                }

                isBtConnected = true;
                reconnectAttempts = 0; // Reset on successful connection
                startKeepAlive();
                startListeningThread();

                String successMessage = "Connected to " +
                        (currentDeviceInfo != null ? currentDeviceInfo.replace(currentDeviceAddress, "") : device.getName());
                notifyResult(CONNECTION_SUCCESS, successMessage, callback);
                return;

            } catch (IOException e) {
                // Close failed socket
                if (socket != null) {
                    try { socket.close(); } catch (IOException ce) { /* ignore */ }
                }
                lastException = e;
                // Try next UUID
            }
        }

        // Fallback method if standard connection fails
        // Fallback method if standard connection fails
        try {
            Method m = device.getClass().getMethod("createInsecureRfcommSocket", int.class);
            BluetoothSocket fallbackSocket = (BluetoothSocket) m.invoke(device, 1);
            fallbackSocket.connect();

            btSocket = fallbackSocket;
            mmInputStream = btSocket.getInputStream();
            mmOutputStream = btSocket.getOutputStream();
            isBtConnected = true;
            reconnectAttempts = 0; // Reset on successful connection

            startKeepAlive();
            startListeningThread();

            // Safe device name handling
            String deviceName = "unknown device";
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ContextCompat.checkSelfPermission(context,
                            Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        deviceName = device.getName() != null ? device.getName() : deviceName;
                    }
                } else {
                    deviceName = device.getName() != null ? device.getName() : deviceName;
                }
            } catch (SecurityException e) {
                Log.w(TAG, "Couldn't get device name", e);
            }

            notifyResult(CONNECTION_SUCCESS,
                    "Connected (fallback method) to " + deviceName,
                    callback);
            return;
        } catch (Exception e) {
            Log.w(TAG, "Fallback connection method failed", e);
        }

        // All attempts failed
        if (lastException != null) {
            throw lastException;
        } else {
            throw new IOException("Failed to establish Bluetooth connection");
        }
    }

    private void startListeningThread() {
        executorService.execute(() -> {
            byte[] buffer = new byte[1024];
            int bytes;

            while (isBtConnected) {
                try {
                    bytes = mmInputStream.read(buffer);
                    if (bytes > 0) {
                        byte[] data = new byte[bytes];
                        System.arraycopy(buffer, 0, data, 0, bytes);
                        if (connectionCallback != null) {
                            mainHandler.post(() -> connectionCallback.onDataReceived(data));
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Connection lost", e);
                    if (isBtConnected) {
                        disconnect();
                        if (connectionCallback != null) {
                            mainHandler.post(() -> {
                                connectionCallback.onConnectionLost();
                                connectionCallback.onConnectionResult(CONNECTION_LOST, "Connection lost");
                            });
                        }
                        startAutoReconnect();
                    }
                    break;
                }
            }
        });
    }

    private void startAutoReconnect() {
        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
            reconnectAttempts++;
            long delay = calculateReconnectDelay(reconnectAttempts);
            Log.d(TAG, "Scheduling reconnect attempt " + reconnectAttempts + " in " + delay + "ms");
            mainHandler.postDelayed(reconnectRunnable, delay);
        } else {
            Log.d(TAG, "Max reconnect attempts reached");
            reconnectAttempts = 0;
        }
    }

    private long calculateReconnectDelay(int attempt) {
        // Exponential backoff: 5s, 10s, 20s, 40s, 80s
        return 5000 * (long)Math.pow(2, attempt-1);
    }

    private final Runnable reconnectRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isBtConnected && currentDeviceAddress != null && connectionCallback != null) {
                Log.d(TAG, "Attempting reconnect...");
                connect(currentDeviceAddress, currentDeviceInfo, connectionCallback);
            }
        }
    };

    private void startKeepAlive() {
        mainHandler.post(keepAliveRunnable);
    }

    private final Runnable keepAliveRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isBtConnected || mmOutputStream == null) {
                return;
            }

            try {
                // Use a more standard keep-alive message
                mmOutputStream.write(new byte[]{0x01}); // SOH character
                mmOutputStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Keep-alive failed", e);
                disconnect();
                if (connectionCallback != null) {
                    mainHandler.post(() -> {
                        connectionCallback.onConnectionLost();
                        connectionCallback.onConnectionResult(CONNECTION_LOST, "Keep-alive failed");
                    });
                }
                startAutoReconnect();
                return;
            }
            // Schedule next keep-alive
            mainHandler.postDelayed(this, 3000);
        }
    };

    public void disconnect() {
        executorService.execute(() -> {
            isBtConnected = false;

            // Remove all pending callbacks first
            mainHandler.removeCallbacks(keepAliveRunnable);
            mainHandler.removeCallbacks(reconnectRunnable);

            Log.d("sexy","love");
            // Close streams and socket
            if (mmInputStream != null) {
                try { mmInputStream.close(); } catch (IOException e) { Log.e(TAG, "Error closing input stream", e); }
                mmInputStream = null;
            }

            if (mmOutputStream != null) {
                try { mmOutputStream.close(); } catch (IOException e) { Log.e(TAG, "Error closing output stream", e); }
                mmOutputStream = null;
            }

            if (btSocket != null) {
                try { btSocket.close(); } catch (IOException e) { Log.e(TAG, "Error closing socket", e); }
                btSocket = null;
            }

            // Notify UI if this was an explicit disconnection
            if (connectionCallback != null) {
                mainHandler.post(() -> {
                    connectionCallback.onConnectionLost();
                    notifyResult(CONNECTION_LOST, "Connection Loss", connectionCallback);
                    connectionCallback.onConnectionResult(CONNECTION_LOST, "Disconnected from device");
                });
            }
        });
    }

    public void sendData(String data) {
        if (!isBtConnected || mmOutputStream == null) {
            Log.w(TAG, "Cannot send data - not connected");
            return;
        }

        executorService.execute(() -> {
            try {
                mmOutputStream.write(data.getBytes());
                mmOutputStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Error sending data", e);
                disconnect();
                if (connectionCallback != null) {
                    mainHandler.post(() -> {
                        connectionCallback.onConnectionLost();
                        connectionCallback.onConnectionResult(CONNECTION_LOST, "Error sending data");
                    });
                }
                startAutoReconnect();
            }
        });
    }

    public void sendData(byte[] data) {
        if (!isBtConnected || mmOutputStream == null) {
            Log.w(TAG, "Cannot send data - not connected");
            return;
        }

        executorService.execute(() -> {
            try {
                mmOutputStream.write(data);
                mmOutputStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Error sending data", e);
                disconnect();
                if (connectionCallback != null) {
                    mainHandler.post(() -> {
                        connectionCallback.onConnectionLost();
                        connectionCallback.onConnectionResult(CONNECTION_LOST, "Error sending data");
                    });
                }
                startAutoReconnect();
            }
        });
    }

    public boolean isConnected() {
        return isBtConnected && validateConnection();
    }

    public void shutdown() {
        disconnect();
        try {
            context.unregisterReceiver(bluetoothStateReceiver);
        } catch (IllegalArgumentException e) {
            // Receiver was not registered
        }
        executorService.shutdown();
    }

    private void notifyResult(int resultCode, String message, ConnectionCallback callback) {
        mainHandler.post(() -> {
            if (callback != null) {
                callback.onConnectionResult(resultCode, message);
            }
        });
    }

    public static boolean checkBluetoothPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public static void requestBluetoothPermissions(android.app.Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    requestCode);
        }
    }
}