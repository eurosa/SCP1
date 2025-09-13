package com.surg.scp.bluetooth;

import static com.surg.scp.bluetooth.BluetoothUtils.bytesToHex;

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

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class BluetoothConnectionManager {

    // Constants
    private static final String TAG = "BluetoothConnection";
    private static final int PACKET_SIZE = 22;
    private static final long KEEP_ALIVE_INTERVAL_MS = 1000;
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final int RECONNECT_BASE_DELAY_MS = 5000;

    // UUIDs for SPP (Serial Port Profile)
    private static final UUID[] SPP_UUIDS = {
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"), // Standard SPP
            UUID.fromString("00001105-0000-1000-8000-00805F9B34FB"), // OBEX
            UUID.fromString("00001124-0000-1000-8000-00805F9B34FB")  // HID
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

    // Application context and handlers
    private final Context context;
    private final Handler mainHandler;
    private final ExecutorService executorService;

    // Bluetooth components
    private BluetoothSocket btSocket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);

    // Device information
    private String currentDeviceAddress;
    private String currentDeviceInfo;
    private int reconnectAttempts = 0;

    // Callback interface
    private ConnectionCallback connectionCallback;

    // Data buffers
    private final byte[] rxBuffer = new byte[PACKET_SIZE];
    private final byte[] txBuffer = new byte[PACKET_SIZE];
    private final byte[] digitalMask = new byte[]{0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, (byte) 0x80};

    // Controller state
    public int deviceId;
    public int tempSet;
    public int humidSet;
    public int diffPressureSet;
    public byte[] DigitalOUT = new byte[]{0x00, 0x00};
    public int intensity1, intensity2, intensity3, intensity4;
    private int SET_TEMP, SET_HUMD, SET_AIRP, STM, SHM, STP;
    private int READ_TEMP, READ_HUMD, READ_PRES;

    // Add these near your other state variables
    private int serialtimeout = 0;
    private int  DISP_TEMP,  DISP_HUMD,  DISP_PRES;

    public byte buzzerHexGas1, buzzerHexGas2, buzzerHexGas3, buzzerHexGas4;
    public byte buzzerHexGas5, buzzerHexGas6, buzzerHexGas7, buzzerHexGas8;
    public int autoManualStatus;

    // Sensor readings
    private int readTemp, readHumd, readPres;
    private int dispTemp, dispHumd, dispPres;

    // Broadcast receiver for Bluetooth state changes
    private final BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if (state == BluetoothAdapter.STATE_OFF) {
                    disconnect();
                    notifyConnectionResult(BLUETOOTH_DISABLED, "Bluetooth was turned off");
                }
            }
        }
    };

   /* public interface ConnectionCallback {
        void onConnectionResult(int resultCode, String message);

        void onDataReceived(byte[] data);

        void onConnectionLost();
    }*/

    public BluetoothConnectionManager(Context context) {
        this.context = context.getApplicationContext();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.executorService = Executors.newFixedThreadPool(2); // Separate threads for RX and TX
// Initialize DigitalOUT array
        this.DigitalOUT = new byte[]{0x00, 0x00};
        this.intensity1 =0;
        this.intensity2=0;
        this.intensity3=0;
        this.intensity4=0;
        // Register Bluetooth state receiver
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(bluetoothStateReceiver, filter);

        // Initialize default values
        resetControllerState();
    }

    private void resetControllerState() {
        deviceId = 0;
        tempSet = 0;
        humidSet = 0;
        diffPressureSet = 0;
        intensity1 = intensity2 = intensity3 = intensity4 = 0;
        buzzerHexGas1 = buzzerHexGas2 = buzzerHexGas3 = buzzerHexGas4 = 0;
        buzzerHexGas5 = buzzerHexGas6 = buzzerHexGas7 = buzzerHexGas8 = 0;
        autoManualStatus = 0;
        readTemp = readHumd = readPres = 0;
        dispTemp = dispHumd = dispPres = 0;
    }

    // Connection Management
    public void connect(String deviceAddress, String deviceInfo, ConnectionCallback callback) {
        this.connectionCallback = callback;
        this.currentDeviceAddress = deviceAddress;
        this.currentDeviceInfo = deviceInfo;
        this.reconnectAttempts = 0;

        executorService.execute(() -> {
            notifyConnectionResult(-1, "Connecting...");

            // Check if already connected to this device
            if (validateConnection() && deviceAddress.equals(btSocket.getRemoteDevice().getAddress())) {
                notifyConnectionResult(CONNECTION_SUCCESS, "Already connected to this device");
                return;
            }

            // Clean up any existing connection
            disconnect();

            // Check permissions
            if (!checkBluetoothPermissions()) {
                notifyConnectionResult(PERMISSION_DENIED, "Bluetooth permission required");
                return;
            }

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                notifyConnectionResult(CONNECTION_FAILED, "Bluetooth not supported");
                return;
            }

            if (!bluetoothAdapter.isEnabled()) {
                notifyConnectionResult(BLUETOOTH_DISABLED, "Bluetooth is disabled");
                return;
            }

            try {
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
                establishConnection(device);
                notifyConnectionResult(CONNECTION_SUCCESS, "Connected to " + getDeviceDisplayName(device));
            } catch (IllegalArgumentException e) {
                notifyConnectionResult(CONNECTION_FAILED, "Invalid device address");
            } catch (SecurityException e) {
                notifyConnectionResult(SECURITY_EXCEPTION, "Bluetooth permission denied");
            } catch (IOException e) {
                notifyConnectionResult(CONNECTION_FAILED, "Connection failed: " + e.getMessage());
                startAutoReconnect();
            }
        });
    }

    private void establishConnection(BluetoothDevice device) throws IOException {
        BluetoothSocket socket = null;
        IOException lastException = null;

        // Try all known UUIDs for SPP devices
        for (UUID uuid : SPP_UUIDS) {
            try {
                socket = createSocket(device, uuid);
                if (ActivityCompat.checkSelfPermission(BluetoothController.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                socket.connect();

                // Connection successful
                btSocket = socket;
                inputStream = btSocket.getInputStream();
                outputStream = btSocket.getOutputStream();

                if (inputStream == null || outputStream == null) {
                    throw new IOException("Failed to establish streams");
                }

                isConnected.set(true);

                verifyProtocol();
                startListeningThread();
                startKeepAlive();
                // Send initial controller data after connection

                return;

            } catch (IOException e) {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException ce) { /* ignore */ }
                }
                lastException = e;
            }
        }

        // Fallback method if standard connection fails
        try {
            Method m = device.getClass().getMethod("createInsecureRfcommSocket", int.class);
            BluetoothSocket fallbackSocket = (BluetoothSocket) m.invoke(device, 1);
            if (ActivityCompat.checkSelfPermission(BluetoothController.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            fallbackSocket.connect();

            btSocket = fallbackSocket;
            inputStream = btSocket.getInputStream();
            outputStream = btSocket.getOutputStream();
            isConnected.set(true);

            startListeningThread();
            startKeepAlive();
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

    private BluetoothSocket createSocket(BluetoothDevice device, UUID uuid) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                throw new SecurityException("Bluetooth permission required");
            }
            return device.createInsecureRfcommSocketToServiceRecord(uuid);
        } else {
            return device.createRfcommSocketToServiceRecord(uuid);
        }
    }

    private String getDeviceDisplayName(BluetoothDevice device) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                        == PackageManager.PERMISSION_GRANTED) {
                    return device.getName() != null ? device.getName() : "Unknown Device";
                }
            }
            return device.getName() != null ? device.getName() : "Unknown Device";
        } catch (SecurityException e) {
            return "Unknown Device";
        }
    }

    // Data Transmission
    public void sendControllerData() {
        if (!isConnected.get()) {
            Log.w(TAG, "Not connected - cannot send data");
            return;
        }

        // Initialize buffer properly
        byte[] txBuffer = new byte[22];
        Arrays.fill(txBuffer, (byte) 0);

        // Prepare device ID message (if needed separately)
        byte[] deviceIdMsg = ("$ID" + deviceId + ";").getBytes();

        // Prepare main controller data
        txBuffer[0] = 0x02;
        txBuffer[1] = (byte) '1';
        txBuffer[2] = (byte) 'C';
        txBuffer[3] = DigitalOUT[0];
        txBuffer[4] = DigitalOUT[1];
        txBuffer[5] = (byte) (intensity1 >> 8);
        txBuffer[6] = (byte) intensity1;
        txBuffer[7] = (byte) (intensity2 >> 8);
        txBuffer[8] = (byte) intensity2;
        txBuffer[9] = (byte) (intensity3 >> 8);
        txBuffer[10] = (byte) intensity3;
        txBuffer[11] = (byte) (tempSet >> 8);
        txBuffer[12] = (byte) tempSet;
        txBuffer[13] = (byte) (humidSet >> 8);
        txBuffer[14] = (byte) humidSet;
        txBuffer[15] = (byte) (intensity4 >> 8);
        txBuffer[16] = (byte) intensity4;

        SET_TEMP = tempSet;
        SET_HUMD = humidSet;
        SET_AIRP = diffPressureSet;

        if (autoManualStatus == 0) {
            // Auto mode calculations
            STM = Math.max(0, Math.min(50, READ_TEMP - (SET_TEMP * 10))) * 20;
            SHM = Math.max(0, Math.min(100, READ_HUMD - (SET_HUMD * 10))) * 10;
        } else {
            // Manual mode
            STM = SET_TEMP * 10;
            SHM = SET_HUMD * 10;
        }

        txBuffer[17] = (byte) (STM >> 8);
        txBuffer[18] = (byte) STM;
        txBuffer[19] = (byte) (SHM >> 8);
        txBuffer[20] = (byte) SHM;
        txBuffer[21] = 0x20;

        try {
            synchronized (this) {
                if (isConnected.get() && outputStream != null) {
                    outputStream.write(txBuffer);
                    outputStream.flush();

                    // Better debugging with hex format
                    Log.d(TAG, "Data sent successfully");
                    Log.d(TAG, "txBuffer as HEX: " + bytesToHex(txBuffer));
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error sending data", e);
            handleConnectionLost();
        }
    }

    // Helper method for hex debugging
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(String.format("%02X", bytes[i]));
            if (i < bytes.length - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private void verifyProtocol() throws IOException {
        // Send verification packet
        byte[] verificationPacket = new byte[]{0x01, 0x02, 0x03};
        outputStream.write(verificationPacket);
        outputStream.flush();

        // Wait for response
        byte[] response = new byte[3];
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 2000) { // 2 second timeout
            if (inputStream.available() >= 3) {
                inputStream.read(response);
                if (Arrays.equals(response, new byte[]{0x06, 0x06, 0x06})) { // Example ACK
                    Log.d(TAG, "Protocol verified");
                    return;
                }
            }
        }
        throw new IOException("Protocol verification failed");
    }

    // Add this method
    private void disableFlowControl() {
        try {
            // This approach doesn't work on most Android versions
            // Method m = btSocket.getClass().getMethod("setRcvBufSize", int.class);
            // m.invoke(btSocket, 8192);

            // Alternative approach that actually works:
            if (btSocket != null) {
                // For Bluetooth Classic (RFCOMM) sockets
                if (ActivityCompat.checkSelfPermission(BluetoothController.context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                if (btSocket.getRemoteDevice().getType() == BluetoothDevice.DEVICE_TYPE_CLASSIC) {
                    // Try to set socket buffer sizes through reflection
                    try {
                        Method setReceiveBuffer = btSocket.getClass().getMethod("setReceiveBufferSize", int.class);
                        setReceiveBuffer.invoke(btSocket, 8192); // 8KB buffer

                        Method setSendBuffer = btSocket.getClass().getMethod("setSendBufferSize", int.class);
                        setSendBuffer.invoke(btSocket, 8192);

                        Log.d(TAG, "Socket buffer sizes adjusted");
                    } catch (Exception e) {
                        Log.w(TAG, "Couldn't adjust socket buffers (normal on some devices)", e);
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Flow control adjustment failed", e);
        }
    }


    public void sendData(byte[] data) {
        if (!isConnected.get() || outputStream == null) {
            Log.w(TAG, "Cannot send data - not connected");
            return;
        }

        executorService.execute(() -> {
            try {
                synchronized (this) {
                    outputStream.write(data);
                    outputStream.flush();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error sending data", e);
                handleConnectionLost();
            }
        });
    }
    private void startListeningThread() {
            final int BUFFER_SIZE = 4096; // 4KB buffer
        executorService.execute(() -> {
            byte[] buffer = new byte[BUFFER_SIZE];
            while (isConnected.get()) {
                try {
                    // Read with timeout
                    int available = inputStream.available();
                    if (available > 0) {
                        int bytesRead = inputStream.read(buffer, 0, Math.min(available, BUFFER_SIZE));
                        if (bytesRead > 0) {
                            processIncomingData(buffer, bytesRead);
                        }
                    }
                    Thread.sleep(50); // Reduce CPU usage
                } catch (Exception e) {
                    Log.e(TAG, "Read error", e);
                    handleConnectionLost();
                    break;
                }
            }
        });
    }

    private void processIncomingData(byte[] buffer, int length) {
        // Process raw bytes here
        Log.d(TAG, "Received " + length + " bytes: " + bytesToHex(Arrays.copyOf(buffer, length)));
        // ... your packet processing logic ...
        if (length >= 22) {
            // Copy the received data to Rxbuf (assuming you have a class-level Rxbuf array)
            System.arraycopy(buffer, 0, rxBuffer, 0, 22);

            // Process the data based on the packet type
            processSerialPort1();
        }
    }

    private void processSerialPort1() {
        // First condition block: Rxbuf[2] == 'C' (Data response)
        if (rxBuffer[0] == 0x02) {
            if (rxBuffer[1] == (byte)'1') {
                if (rxBuffer[2] == (byte)'C') {
                    if (rxBuffer[21] == 0x20) {
                        serialtimeout = 0;

                        // Combine bytes directly (little-endian)
                      //  int tempValue = ((rxBuffer[6] & 0xFF) << 8) | (rxBuffer[5] & 0xFF);
                        int tempValue = (short) (((rxBuffer[5] & 0xFF) << 8) | (rxBuffer[6] & 0xFF));

                        READ_TEMP = tempValue;
                        DISP_TEMP = tempValue / 10;
                        if (DISP_TEMP > 99) DISP_TEMP = 99;
                        Log.d(TAG, "Temperature Read Value: " + DISP_TEMP);

                        tempValue = (short) (((rxBuffer[7] & 0xFF) << 8) | (rxBuffer[8] & 0xFF));

                       // tempValue = ((rxBuffer[8] & 0xFF) << 8) | (rxBuffer[7] & 0xFF);
                        READ_HUMD = tempValue;
                        DISP_HUMD = tempValue / 10;
                        if (DISP_HUMD > 99) DISP_HUMD = 99;

                        tempValue = (short) (((rxBuffer[9] & 0xFF) << 8) | (rxBuffer[10] & 0xFF));

                        //tempValue = ((rxBuffer[10] & 0xFF) << 8) | (rxBuffer[9] & 0xFF);
                        READ_PRES = tempValue;
                        DISP_PRES = tempValue;
                        if (DISP_PRES < 0) DISP_PRES = 0;
                        Log.d(TAG, "Differential Pressure: " + DISP_PRES);

                        notifySensorDataUpdated(DISP_TEMP, DISP_HUMD, DISP_PRES);
                    }
                }
            }
        }

        // Second condition block: Rxbuf[2] == 'W' (Settings response)
        if (rxBuffer[0] == 0x02) {
            if (rxBuffer[1] == (byte)'1') {
                if (rxBuffer[2] == (byte)'W') {
                    if (rxBuffer[11] == 0x20) {
                        serialtimeout = 0;

                        int tempValue = ((rxBuffer[6] & 0xFF) << 8) | (rxBuffer[5] & 0xFF);
                        SET_TEMP = tempValue / 10;

                        tempValue = ((rxBuffer[8] & 0xFF) << 8) | (rxBuffer[7] & 0xFF);
                        SET_HUMD = tempValue / 10;

                        tempValue = ((rxBuffer[10] & 0xFF) << 8) | (rxBuffer[9] & 0xFF);
                        SET_AIRP = tempValue;

                        notifySettingsUpdated(SET_TEMP, SET_HUMD, SET_AIRP);
                    }
                }
            }
        }
    }

    // Helper method to convert two bytes to short
    private short bytesToShort(byte high, byte low) {
        return (short) (((high & 0xFF) << 8) | (low & 0xFF));
    }


    // Add these callback methods to your interface and implementation
    public interface ConnectionCallback {
        void onConnectionResult(int resultCode, String message);
        void onDataReceived(byte[] data);
        void onConnectionLost();
        void onSensorDataUpdated(int temperature, int humidity, int pressure);
        void onSettingsUpdated(int tempSet, int humidSet, int pressureSet);






    }

    // Helper methods to notify callbacks
    private void notifySensorDataUpdated(int temperature, int humidity, int pressure) {
        mainHandler.post(() -> {
            if (connectionCallback != null) {
                connectionCallback.onSensorDataUpdated(temperature, humidity, pressure);
            }
        });
    }

    private void notifySettingsUpdated(int tempSet, int humidSet, int pressureSet) {
        mainHandler.post(() -> {
            if (connectionCallback != null) {
                connectionCallback.onSettingsUpdated(tempSet, humidSet, pressureSet);
            }
        });
    }
    // Data Reception
  /* private void startListeningThread() {
        executorService.execute(() -> {
            Log.d(TAG, "Listening thread started");

            while (isConnected.get()) {
                try {
                    // Read exactly PACKET_SIZE bytes
                    int bytesRead = 0;
                    while (bytesRead < PACKET_SIZE && isConnected.get()) {
                        int read = inputStream.read(rxBuffer, bytesRead, PACKET_SIZE - bytesRead);
                        if (read == -1) {
                            throw new IOException("Stream closed");
                        }
                        bytesRead += read;
                    }

                    if (bytesRead == PACKET_SIZE) {
                        processReceivedData(rxBuffer);
                        notifyDataReceived(rxBuffer);
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error in listening thread", e);
                    if (isConnected.get()) {
                        handleConnectionLost();
                    }
                    break;
                }
            }
            Log.d(TAG, "Listening thread stopped");
        });
    }*/

    private void processReceivedData(byte[] data) {
        if (data.length >= PACKET_SIZE) {
            // Parse digital inputs
            // digitalIn[0] = data[3];
            // digitalIn[1] = data[4];

            // Parse analog values
            readTemp = ((data[5] & 0xFF) << 8) | (data[6] & 0xFF);
            readHumd = ((data[7] & 0xFF) << 8) | (data[8] & 0xFF);
            readPres = ((data[9] & 0xFF) << 8) | (data[10] & 0xFF);

            // Update display values
            dispTemp = readTemp / 10;
            dispHumd = readHumd / 10;
            dispPres = readPres;

            Log.d(TAG, String.format("Received data - Temp: %d, Humid: %d, Pres: %d",
                    dispTemp, dispHumd, dispPres));
        }
    }

    // Connection Maintenance
    private void startKeepAlive() {
        mainHandler.postDelayed(keepAliveRunnable, KEEP_ALIVE_INTERVAL_MS);
    }

    private final Runnable keepAliveRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isConnected.get() || outputStream == null) {
                return;
            }

            try {
                synchronized (BluetoothConnectionManager.this) {
                  //  outputStream.write(0x00); // Simple keep-alive
                   // outputStream.flush();
                    sendControllerData();
                }
            } catch (Exception e) {
                Log.e(TAG, "Keep-alive failed", e);
                handleConnectionLost();
                return;
            }

            // Schedule next keep-alive
            mainHandler.postDelayed(this, KEEP_ALIVE_INTERVAL_MS);
        }
    };

    private void startAutoReconnect() {
        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
            reconnectAttempts++;
            long delay = RECONNECT_BASE_DELAY_MS * (long) Math.pow(2, reconnectAttempts - 1);

            Log.d(TAG, "Scheduling reconnect attempt " + reconnectAttempts + " in " + delay + "ms");
            mainHandler.postDelayed(() -> {
                if (!isConnected.get() && currentDeviceAddress != null && connectionCallback != null) {
                    connect(currentDeviceAddress, currentDeviceInfo, connectionCallback);
                }
            }, delay);
        } else {
            Log.d(TAG, "Max reconnect attempts reached");
            reconnectAttempts = 0;
        }
    }

    // Connection State Management
    public void disconnect() {
        executorService.execute(() -> {
            isConnected.set(false);

            // Remove all pending callbacks
            mainHandler.removeCallbacks(keepAliveRunnable);

            // Close streams and socket
            synchronized (this) {
                if (inputStream != null) {
                    try { inputStream.close(); } catch (IOException e) { Log.e(TAG, "Error closing input stream", e); }
                    inputStream = null;
                }

                if (outputStream != null) {
                    try { outputStream.close(); } catch (IOException e) { Log.e(TAG, "Error closing output stream", e); }
                    outputStream = null;
                }

                if (btSocket != null) {
                    try { btSocket.close(); } catch (IOException e) { Log.e(TAG, "Error closing socket", e); }
                    btSocket = null;
                }
            }

            notifyConnectionLost();
        });
    }

    private void handleConnectionLost() {
        if (isConnected.compareAndSet(true, false)) {
            disconnect();
            startAutoReconnect();
        }
    }

    private boolean validateConnection() {
        if (btSocket == null || !btSocket.isConnected() || inputStream == null || outputStream == null) {
            return false;
        }

        try {
            synchronized (this) {
                outputStream.write(0x00);
                outputStream.flush();
                return true;
            }
        } catch (IOException e) {
            return false;
        }
    }

    // Callback Notifications
    private void notifyConnectionResult(int resultCode, String message) {
        mainHandler.post(() -> {
            if (connectionCallback != null) {
                connectionCallback.onConnectionResult(resultCode, message);
            }
        });
    }

    private void notifyDataReceived(byte[] data) {
        mainHandler.post(() -> {
            if (connectionCallback != null) {
                connectionCallback.onDataReceived(data);
            }
        });
    }

    private void notifyConnectionLost() {
        mainHandler.post(() -> {
            if (connectionCallback != null) {
                connectionCallback.onConnectionLost();
            }
        });
    }

    // Utility Methods
    public boolean isConnected() {
        return isConnected.get() && validateConnection();
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

    public static boolean checkBluetoothPermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                    == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private boolean checkBluetoothPermissions() {
        return checkBluetoothPermissions(context);
    }

    public static void requestBluetoothPermissions(@NonNull android.app.Activity activity, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    requestCode);
        }
    }
}