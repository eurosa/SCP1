package com.surg.scp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.StrictMode;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Html;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.text.HtmlCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.exifinterface.media.ExifInterface;

import com.google.android.gms.instantapps.InstantApps;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.kyleduo.switchbutton.SwitchButton;
import com.surg.scp.bluetooth.BluetoothConnectionManager;
import com.surg.scp.bluetooth.BluetoothController;
import com.surg.scp.bluetooth.BluetoothService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.BreakIterator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.ContentValues.TAG;

public class DeviceList extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener,
        BluetoothConnectionManager.ConnectionCallback {
    private static final String MY_PREFS_NAME = "MyTxtFile";
    private ImageView connectionStatusIcon;

    /***************************************************************************************
     *                           Start Increment and Decrement
     ****************************************************************************************/
    private boolean doubleBackToExitPressedOnce = false;
    ImageButton tempMinusButton;
    ImageButton tempPlusButton;
    ImageButton[] arrayOfControlButtons;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    boolean isLightOneOn = false;
    boolean isLightTwoOn = false;
    boolean isLightThreeOn = false;
    boolean isLightFourOn = false;

    // Timer mode constants
    private static final int MODE_STOPWATCH = 0;
    private static final int MODE_COUNT_UP = 1;
    private static final int MODE_COUNT_DOWN = 2;

    private int currentTimerMode = MODE_STOPWATCH;
    private boolean isTimerRunning = false;

    // Timer variables
    private int timeVar;
    private int timeVarEdit;
    private int hr, min, sec;

    private int cdflag = 0; // 0 = paused, 1 = running
    private boolean isCountUp = false;
    private int maxTimeInSeconds = 0;
    private int currentTimeInSeconds = 0;
    private int pausedTimeInSeconds = 0;
    private int pausedCountDownTimeInSeconds = 0;
    /***************************************************************************************
     *                          End Increment and Decrement
     ****************************************************************************************/

    /***************************************************************************************
     *   Play and pause in only one button - Android
     ****************************************************************************************/
    boolean isPlaying = false;
    /***************************************************************************************
     *  Play and pause in only one button - Android
     ****************************************************************************************/

    //==============================To Connect Bluetooth Device=============================
    private ProgressDialog progress;
    private boolean isBtConnected = false;
    BluetoothSocket btSocket = null;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String address = null;
    private Button sendBtn;
    boolean listViewFlag = true;

    //==============================To connect bluetooth devices=============================
    //-----------------------------Camera----------------------------------------------------
    private static final String LOG_TAG = "JBCamera";
    private static final int REQUEST_CAMERA_PERMISSION = 21;
    private static final int REQUEST_STORAGE_PERMISSION = 22;
    private int cameraId = 1;
    private Camera camera = null;
    private boolean waitForPermission = false;
    private boolean waitForStoragePermission = false;
    //-----------------------------------Camera-----------------------------------------------
    // ------------------------ Auto Repeat increment and decrement --------------------------
    Integer currentDisplayValue = 0;
    Integer currentHumValue = 0;

    final int DisplayValueMin = 0;
    final int DisplayValueMax1 = 99;
    final int DisplayValueMax2 = 999;
    final int DisplayValueMax3 = 9999;
    //+++++++++++++++++++++++++ Auto Repeat increment and decrement ++++++++++++++++++++++++++
    private static final String PREFS_DISPLAY_VALUE = "display_value";
    private static final String PREFS_HUM_VALUE = "hum_value";
    Button On, Off, Discnt, Abt;

    //widgets
    Button btnPaired, scanDevices;
    ListView devicelist;
    //Bluetooth
    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";
    public static String EXTRA_INFO = "device_info";

    //screenshott
    private final static String[] requestWritePermission =
            {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private ImageView imageView;
    private Bitmap bitmap;

    private String str_celcius;
    private String str_fahrenheit;
    private ImageView bmpView;
    //=================================For temperature limit count==================================
    private String double_str_fahrenheit;
    private ImageView iv_your_image;
    private String str_cel, str_fah;
    //=========================================End temperature limit count==========================
    String[] permissions = new String[]{

            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    private FrameLayout layout;

    private TextView one, two, three, four, five, six, seven, eight, nine, zero, div, multi, sub, plus, dot, equals, display, clear;
    private ImageButton backDelete;

    /***************************************************************************************
     * Navigation Drawer Layout
     *
     ***************************************************************************************/
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView mNavigationView;
    private ListView idList, sndList, digitList, typeList;
    private ArrayAdapter<String> typeAdapter;
    private ArrayAdapter<String> sndAdapter;
    private ArrayAdapter<String> idAdapter;
    private ArrayAdapter<String> digitAdapter;
    private Dialog idDialog;
    private Dialog digitDialog;
    private Dialog sndDialog;
    private Dialog typeDialog;
    private DatabaseHandler dbHandler;
    private DataModel dataModel;
    private boolean success = false;
    private ImageView connectionStatus;
    private String infoBLE = null;
    private String addressBLE = null;
    private String name = null;
    private String info_address;
    private TextView clockShow;

    /****************************************************************************************
     * End of Navigation Drawer
     *
     * */

    /***************************************************************************************
     * Start Stop Watch
     ****************************************************************************************/
    private TextView timerValue;
    private long startTime = 0L;
    private Handler customHandler = new Handler();

    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    private ImageButton playPause;
    private ImageButton stopButton;
    private ImageButton resetButton;
    private ImageButton settingButton;

    private SwitchButton switch1;
    private SwitchButton switch2;
    private ImageButton humBtnPlus;
    private ImageButton humBtnMinus;
    private ImageButton lightFourBtn;
    private ImageButton lightThreeBtn;
    private ImageButton lightTwoBtn;
    private ImageButton lightOneBtn;
    private BluetoothConnectionManager bluetoothManager;
    private static final int PERMISSION_REQUEST_CODE = 1001;
    // Timer
    private CountDownTimer countDownTimer;
    private CountUpTimer countUpTimer;
    // SharedPreferences
    private SharedPreferences sharedPreferences1;
    private static final String PREFS_NAME = "TimerPrefs";

    private SharedPreferences lastDevicePrefs;
    private String currentConnectionAddress;
    private String currentConnectionInfo;

    /***************************************************************************************
     * End Stop Watch
     ****************************************************************************************/

    // Handler for UI updates
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    // Executor for background tasks
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    private IncrementDecrementSlider customSlider1,customSlider2,customSlider3,customSlider4;
    private int myVariable1,myVariable2,myVariable3,myVariable4;
    private TextView temperatureTextView, humidityTextView, pressureTextView, tempSetTextView,humidSetTextView,pressureSetTextView;
    public TextView gasOneStatus, gasTwoStatus, gasThreeStatus,gasFourStatus,gasFiveStatus,gasSixStatus,gasSevenStatus;
    private LinearLayout rightPart3;
    private TextView gasOne, gasTwo, gasThree, gasFour, gasFive, gasSix;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scp);
        // Initialize SharedPreferences for storing last device
        lastDevicePrefs = getSharedPreferences("last_device", MODE_PRIVATE);
        temperatureTextView = findViewById(R.id.temperatureTextView);
        humidityTextView = findViewById(R.id.humidityTextView);
        pressureTextView = findViewById(R.id.pressureTextView);
        tempSetTextView = findViewById(R.id.tempSetTextView);
        humidSetTextView = findViewById(R.id.humidSetTextView);
        gasSevenStatus = findViewById(R.id.gasSevenStatus);
        rightPart3 = findViewById(R.id.rightPart3);

        // Gas names
        gasOne = findViewById(R.id.gasOne);
        gasTwo = findViewById(R.id.gasTwo);
        gasThree = findViewById(R.id.gasThree);
        gasFour = findViewById(R.id.gasFour);
        gasFive = findViewById(R.id.gasFive);
        gasSix = findViewById(R.id.gasSix);

        gasOneStatus = findViewById(R.id.gasOneStatus);
        gasTwoStatus = findViewById(R.id.gasTwoStatus);
        gasThreeStatus = findViewById(R.id.gasThreeStatus);
        gasFourStatus = findViewById(R.id.gasFourStatus);
        gasFiveStatus = findViewById(R.id.gasFiveStatus);
        gasSixStatus = findViewById(R.id.gasSixStatus);

        // Initialize components that might take time
        bluetoothManager = new BluetoothConnectionManager(this);
        customSlider1 = findViewById(R.id.customSlider1);
        customSlider2 = findViewById(R.id.customSlider2);
        customSlider3 = findViewById(R.id.customSlider3);
        customSlider4 = findViewById(R.id.customSlider4);

        customSlider1.configureSteps(10, 100f, 0f);
        customSlider2.configureSteps(10, 100f, 0f);
        customSlider3.configureSteps(10, 100f, 0f);
        customSlider4.configureSteps(10, 100f, 0f);


        SharedPreferences prefs = getSharedPreferences("MyIntensityPrefs", MODE_PRIVATE);

        int saved1 = prefs.getInt("intensity1", 0); // default 0 if not saved
        int saved2 = prefs.getInt("intensity2", 0);
        int saved3 = prefs.getInt("intensity3", 0);
        int saved4 = prefs.getInt("intensity4", 0);
        currentDisplayValue = prefs.getInt(PREFS_DISPLAY_VALUE, 0);
        currentHumValue = prefs.getInt(PREFS_HUM_VALUE, 0);
        bluetoothManager.tempSet =currentDisplayValue;
        bluetoothManager.humidSet =currentHumValue;

        // Initialize UI components first to prevent ANR
        initializeUIComponents();
        //------------------------------------------------------------------------------------------------
        //=========================Adding Toolbar in android layout=======================================
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setDisplayShowHomeEnabled(true);
        //=========================Toolbar End============================================================
        Drawable drawable = myToolbar.getOverflowIcon();
        if (drawable != null) {
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable.mutate(), getResources().getColor(R.color.whiteColor));
            myToolbar.setOverflowIcon(drawable);
        }
        // ******************** Changing the color of three dot or overflow button *****************************


        /***************************************************************************************
         * Navigation Drawer Layout
         *
         ***************************************************************************************/
        // drawer layout instance to toggle the menu icon to open
        // drawer and back button to close drawer
        drawerLayout = findViewById(R.id.draw_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                // Do whatever you want here
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // Do whatever you want here
                //  initItemData();

            }

            public void onDrawerStateChanged(int i) {

            }

            public void onDrawerSlide(View view, float v) {

            }


        };

        // pass the Open and Close toggle for the drawer layout listener
        // to toggle the button
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();


        // to change hamburger icon color
        actionBarDrawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.whiteColor));


        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        // After giving this Navigation menu Item Icon becomes colorful
        mNavigationView.setItemIconTintList(null); // <-- HERE add this code for icon color
        // to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);  // ← Important!
        // Run heavy operations in background
        backgroundExecutor.execute(() -> {
            // Perform background initialization
            initializeBackgroundComponents();

            // Update UI on main thread
            uiHandler.post(this::initializeUIAfterBackground);
        });


// also update bluetoothManager at startup
        bluetoothManager.intensity1 = saved1;
        bluetoothManager.intensity2 = saved2;
        bluetoothManager.intensity3 = saved3;
        bluetoothManager.intensity4 = saved4;
        customSlider1.setValue(saved1);
        customSlider2.setValue(saved2);
        customSlider3.setValue(saved3);
        customSlider4.setValue(saved4);

        SharedPreferences.Editor editor = prefs.edit();

// customSlider1
        customSlider1.getSlider().addOnChangeListener((slider, value, fromUser) -> {
            bluetoothManager.intensity1 = (int) value;
            editor.putInt("intensity1", bluetoothManager.intensity1);
            editor.apply();
            Log.d("MainActivity", "myVariable = " + bluetoothManager.intensity1);
        });

// customSlider2
        customSlider2.getSlider().addOnChangeListener((slider, value, fromUser) -> {
            bluetoothManager.intensity2 = (int) value;
            editor.putInt("intensity2", bluetoothManager.intensity2);
            editor.apply();
            Log.d("MainActivity", "myVariable = " + bluetoothManager.intensity2);
        });

// customSlider3
        customSlider3.getSlider().addOnChangeListener((slider, value, fromUser) -> {
            bluetoothManager.intensity3 = (int) value;
            editor.putInt("intensity3", bluetoothManager.intensity3);
            editor.apply();
            Log.d("MainActivity", "myVariable = " + bluetoothManager.intensity3);
        });

// customSlider4
        customSlider4.getSlider().addOnChangeListener((slider, value, fromUser) -> {
            bluetoothManager.intensity4 = (int) value;
            editor.putInt("intensity4", bluetoothManager.intensity4);
            editor.apply();
            Log.d("MainActivity", "myVariable = " + bluetoothManager.intensity4);
        });



        // Example: set initial value
        // Handle intent data - BOTH from direct start and from activity result
        handleIncomingIntent();

        setupGasTexts();
        setupStatusTexts();

        // After UI is initialized, try auto-connect
        new Handler().postDelayed(() -> {
            tryAutoConnect();
        }, 1500); // Wait 1.5 seconds for UI to load

    }

    private boolean isBluetoothConnected() {
        return bluetoothManager != null && bluetoothManager.isConnected();
    }
    private void tryAutoConnect() {
        // Don't auto-connect if already connected
        if (isBluetoothConnected()) {
            Log.d("AutoConnect", "Already connected");
            return;
        }

        // Get last connected device from SharedPreferences
        String savedAddress = lastDevicePrefs.getString("address", "");
        String savedInfo = lastDevicePrefs.getString("info", "");

        if (!savedAddress.isEmpty()) {
            Log.d("AutoConnect", "Found saved device: " + savedAddress);

            // Try to connect to the saved device
            connectToDevice(savedAddress, savedInfo);
        } else {
            Log.d("AutoConnect", "No saved device found");
        }
    }

    private void setupGasTexts() {
        // Method 1: Using HtmlCompat (recommended for Android X)
        gasOne.setText(HtmlCompat.fromHtml(
                getString(R.string.oxygen),
                HtmlCompat.FROM_HTML_MODE_LEGACY
        ));

        // Method 2: Direct Unicode (simpler)
        gasThree.setText("N\u2082O"); // Nitrous Oxide

        // Method 3: Using helper method
        setHtmlText(gasFive, R.string.carbon_dioxide);

        // Regular text
        gasTwo.setText(getString(R.string.air_4_bar));
        gasFour.setText(getString(R.string.air_7_bar));
        gasSix.setText(getString(R.string.vacuum));
    }

    private void setHtmlText(TextView textView, int stringResId) {
        String htmlText = getString(stringResId);
        textView.setText(HtmlCompat.fromHtml(htmlText, HtmlCompat.FROM_HTML_MODE_LEGACY));
    }

    private void setupStatusTexts() {
        // Set initial status
        String normalStatus = getString(R.string.normal);

        gasOneStatus.setText(normalStatus);
        gasTwoStatus.setText(normalStatus);
        gasThreeStatus.setText(normalStatus);
        gasFourStatus.setText(normalStatus);
        gasFiveStatus.setText(normalStatus);
        gasSixStatus.setText(normalStatus);

        // You can update status dynamically
        // Example: updateGasStatus("oxygen", "Warning");
    }

    public void updateGasStatus(String gasType, String status) {
        switch (gasType.toLowerCase()) {
            case "oxygen":
                gasOneStatus.setText(status);
                break;
            case "air_4_bar":
                gasTwoStatus.setText(status);
                break;
            case "nitrous_oxide":
                gasThreeStatus.setText(status);
                break;
            case "air_7_bar":
                gasFourStatus.setText(status);
                break;
            case "carbon_dioxide":
                gasFiveStatus.setText(status);
                break;
            case "vacuum":
                gasSixStatus.setText(status);
                break;
        }
    }

    // To update all status at once
    public void updateAllStatus(String status) {
        gasOneStatus.setText(status);
        gasTwoStatus.setText(status);
        gasThreeStatus.setText(status);
        gasFourStatus.setText(status);
        gasFiveStatus.setText(status);
        gasSixStatus.setText(status);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Reset the double back press flag when returning to this activity
        doubleBackToExitPressedOnce = false;
        // Try to reconnect if we're not connected
        if (!isBluetoothConnected()) {
            new Handler().postDelayed(() -> {
                tryAutoConnect();
            }, 1000);
        }

    }

    private void handleIncomingIntent() {
        Intent intent = getIntent();
        String address = intent.getStringExtra(DeviceList.EXTRA_ADDRESS);
        String info_address = intent.getStringExtra(DeviceList.EXTRA_INFO);

        if (address != null) {
            connectToDevice(address, info_address);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIncomingIntent();
    }
    private void initializeUIComponents() {
        // Initialize all UI components here
        setAnimation();

        // Find all views
        connectionStatusIcon = findViewById(R.id.connectionStatus);
        playPause = findViewById(R.id.startButton);
        settingButton = findViewById(R.id.settingButton);
        timerValue = findViewById(R.id.timerValue);

        tempMinusButton = findViewById(R.id.tempBtnMinus);
        tempPlusButton = findViewById(R.id.tempBtnPlus);
        humBtnPlus = findViewById(R.id.humBtnPlus);
        humBtnMinus = findViewById(R.id.humBtnMinus);
        switch1 = findViewById(R.id.switch1);
        switch2 = findViewById(R.id.switch2);
        lightOneBtn = findViewById(R.id.lightOneBtn);
        lightTwoBtn = findViewById(R.id.lightTwoBtn);
        lightThreeBtn = findViewById(R.id.lightThreeBtn);
        lightFourBtn = findViewById(R.id.lightFourBtn);

        resetButton = findViewById(R.id.resetButton);

        // Setup toolbar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Setup navigation drawer
        drawerLayout = findViewById(R.id.draw_layout);
        mNavigationView = findViewById(R.id.nav_view);

        // Setup button listeners
        setupButtonListeners();
    }

    private void initializeBackgroundComponents() {

        sharedPreferences1 = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sharedPreferences = getSharedPreferences("LightPrefs", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Initialize database handler
        dataModel = new DataModel();
        dbHandler = new DatabaseHandler(this);
        dbHandler.getQmsUtilityById("1", dataModel);

        // Check Bluetooth availability
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
    }
    public void setAnimation() {
        if (Build.VERSION.SDK_INT > 20) {
            Slide slide = new Slide();
            slide.setSlideEdge(Gravity.LEFT);
            slide.setDuration(400);
            slide.setInterpolator(new AccelerateDecelerateInterpolator());
            getWindow().setExitTransition(slide);
            getWindow().setEnterTransition(slide);
        }
    }

    private void saveCurrentValues() {
        SharedPreferences.Editor editor = getSharedPreferences("MyIntensityPrefs", MODE_PRIVATE).edit();
        editor.putInt(PREFS_DISPLAY_VALUE, currentDisplayValue);
        editor.putInt(PREFS_HUM_VALUE, currentHumValue);
        editor.apply();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            int id = item.getItemId();

            if (id == R.id.action_disconnect) {
                Disconnect();
                return true;
            } else if (id == R.id.action_searchList) {
                ScanDevicesList();
                return true;
            } else if (id == R.id.action_pairedList) {
                pairedDevicesList();
                return true;
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void Disconnect() {
        if (btSocket != null) {
            try {
                btSocket.close();
                Toast.makeText(DeviceList.this, "Bluetooth device has been disconnected", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
             //   msg("Error");
            }
        } else {
            Toast.makeText(DeviceList.this, "No device connected", Toast.LENGTH_LONG).show();
        }
    }
    private void loadTimerValues() {
        String hour = sharedPreferences1.getString("timer_hour", "0");
        String minute = sharedPreferences1.getString("timer_minute", "0");
        String second = sharedPreferences1.getString("timer_second", "0");
        boolean upChecked = sharedPreferences1.getBoolean("upcheckbox", false);
        boolean downChecked = sharedPreferences1.getBoolean("downcheckbox", false);

        hr = Integer.parseInt(hour);
        min = Integer.parseInt(minute);
        sec = Integer.parseInt(second);

        timeVar = hr * 60 * 60 + min * 60 + sec;
        timeVarEdit = timeVar;
        maxTimeInSeconds = timeVar;

        // Reset current states when loading new values
        currentTimeInSeconds = 0;
        pausedTimeInSeconds = 0;
        pausedCountDownTimeInSeconds = 0;
        cdflag = 0;
        isTimerRunning = false;

        if (upChecked) {
            currentTimerMode = MODE_COUNT_UP;
            isCountUp = true;
        } else if (downChecked) {
            currentTimerMode = MODE_COUNT_DOWN;
            isCountUp = false;
        } else {
            currentTimerMode = MODE_STOPWATCH;
        }

        updateDisplayForCurrentMode();
    }

    private void updateDisplayForCurrentMode() {
        switch (currentTimerMode) {
            case MODE_STOPWATCH:
                timerValue.setText("00:00:00");
                break;
            case MODE_COUNT_UP:
                timerValue.setText(method5(0));
                break;
            case MODE_COUNT_DOWN:
                timerValue.setText(method5(timeVar));
                break;
        }
    }
    private String method5(int secs) {
        int hours = secs / 3600;
        int minutes = (secs % 3600) / 60;
        int seconds = secs % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    private void restoreStates() {
        boolean switch1State = sharedPreferences.getBoolean("switch1", false);
        boolean switch2State = sharedPreferences.getBoolean("switch2", false);

        if (switch1State) {
            switch1.setChecked(true);
            switch1.setThumbColorRes(R.color.red);
            bluetoothManager.DigitalOUT[1] &= 0xFE;
        } else {
            switch1.setChecked(false);
            switch1.setThumbColorRes(R.color.limeGreen);
            bluetoothManager.DigitalOUT[1] |= 0x01;
        }

        if (switch2State) {
            switch2.setChecked(true);
            switch2.setThumbColorRes(R.color.red);
            bluetoothManager.DigitalOUT[1] &= 0xFD;
        } else {
            switch2.setChecked(false);
            switch2.setThumbColorRes(R.color.limeGreen);
            bluetoothManager.DigitalOUT[1] |= 0x02;
        }

        boolean light1State = sharedPreferences.getBoolean("light1", false);
        boolean light2State = sharedPreferences.getBoolean("light2", false);
        boolean light3State = sharedPreferences.getBoolean("light3", false);
        boolean light4State = sharedPreferences.getBoolean("light4", false);

        isLightOneOn = light1State;
        isLightTwoOn = light2State;
        isLightThreeOn = light3State;
        isLightFourOn = light4State;

        lightOneBtn.setImageResource(light1State ? R.drawable.ic_bulb_on : R.drawable.ic_bulb_off);
        lightTwoBtn.setImageResource(light2State ? R.drawable.ic_bulb_on : R.drawable.ic_bulb_off);
        lightThreeBtn.setImageResource(light3State ? R.drawable.ic_bulb_on : R.drawable.ic_bulb_off);
        lightFourBtn.setImageResource(light4State ? R.drawable.ic_bulb_on : R.drawable.ic_bulb_off);

        if (light1State) bluetoothManager.DigitalOUT[1] |= 0x04; else bluetoothManager.DigitalOUT[1] &= 0xFB;
        if (light2State) bluetoothManager.DigitalOUT[1] |= 0x08; else bluetoothManager.DigitalOUT[1] &= 0xF7;
        if (light3State) bluetoothManager.DigitalOUT[1] |= 0x10; else bluetoothManager.DigitalOUT[1] &= 0xEF;
        if (light4State) bluetoothManager.DigitalOUT[1] |= 0x20; else bluetoothManager.DigitalOUT[1] &= 0xDF;
    }
    private void initializeUIAfterBackground() {
        // Load saved timer values
        loadTimerValues();

        // Update UI with loaded values
        updateDisplayForCurrentMode();
        updateDisplay();
        updateHumDisplay();
        restoreStates();

        // Setup navigation listener
        mNavigationView.setNavigationItemSelectedListener(this);

        // Handle intent data
        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS);
        info_address = newint.getStringExtra(DeviceList.EXTRA_INFO);

        if (address != null) {
            connectToDevice(address, info_address);
        }

        // Setup clock
        setupClock();
    }

    private void showTimerSettingsPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_timer_settings, null);

        int width = ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = ViewGroup.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        final EditText timeHourValue = popupView.findViewById(R.id.timeHourValue);
        final EditText timeMinValue = popupView.findViewById(R.id.timeMinValue);
        final EditText timeSecValue = popupView.findViewById(R.id.timeSecValue);
        final CheckBox upCheckBox1 = popupView.findViewById(R.id.upCheckBox1);
        final CheckBox downCheckBox1 = popupView.findViewById(R.id.downCheckBox1);
        Button okBtn = popupView.findViewById(R.id.okBtn);
        Button cancelBtn = popupView.findViewById(R.id.cancelBtn);
        Button minUpBtn = popupView.findViewById(R.id.minUpBtn);
        Button minDownBtn = popupView.findViewById(R.id.minDownBtn);
        Button secUpBtn = popupView.findViewById(R.id.secUpBtn);
        Button secDownBtn = popupView.findViewById(R.id.secDownBtn);
        Button hrUpBtn = popupView.findViewById(R.id.hrUpBtn);
        Button hrDownBtn = popupView.findViewById(R.id.hrDownBtn);

        String hour = sharedPreferences1.getString("timer_hour", "0");
        String minute = sharedPreferences1.getString("timer_minute", "0");
        String second = sharedPreferences1.getString("timer_second", "0");
        boolean upChecked = sharedPreferences1.getBoolean("upcheckbox", false);
        boolean downChecked = sharedPreferences1.getBoolean("downcheckbox", false);

        timeHourValue.setText(hour);
        timeMinValue.setText(minute);
        timeSecValue.setText(second);

        if (currentTimerMode == MODE_COUNT_UP) {
            upCheckBox1.setChecked(true);
            downCheckBox1.setChecked(false);
        } else if (currentTimerMode == MODE_COUNT_DOWN) {
            upCheckBox1.setChecked(false);
            downCheckBox1.setChecked(true);
        } else {
            upCheckBox1.setChecked(false);
            downCheckBox1.setChecked(false);
        }

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hours = Integer.parseInt(timeHourValue.getText().toString());
                int minutes = Integer.parseInt(timeMinValue.getText().toString());
                int seconds = Integer.parseInt(timeSecValue.getText().toString());
                saveTimerValues(hours, minutes, seconds, upCheckBox1.isChecked(), downCheckBox1.isChecked());
                popupWindow.dismiss();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });

        minUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int minutes = Integer.parseInt(timeMinValue.getText().toString());
                if (minutes < 59) minutes++;
                else minutes = 0;
                timeMinValue.setText(String.valueOf(minutes));
            }
        });

        minDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int minutes = Integer.parseInt(timeMinValue.getText().toString());
                if (minutes > 0) minutes--;
                else minutes = 59;
                timeMinValue.setText(String.valueOf(minutes));
            }
        });

        secUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int seconds = Integer.parseInt(timeSecValue.getText().toString());
                if (seconds < 59) seconds++;
                else seconds = 0;
                timeSecValue.setText(String.valueOf(seconds));
            }
        });

        secDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int seconds = Integer.parseInt(timeSecValue.getText().toString());
                if (seconds > 0) seconds--;
                else seconds = 59;
                timeSecValue.setText(String.valueOf(seconds));
            }
        });

        hrUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hours = Integer.parseInt(timeHourValue.getText().toString());
                if (hours < 99) hours++;
                else hours = 0;
                timeHourValue.setText(String.valueOf(hours));
            }
        });

        hrDownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int hours = Integer.parseInt(timeHourValue.getText().toString());
                if (hours > 0) hours--;
                else hours = 99;
                timeHourValue.setText(String.valueOf(hours));
            }
        });

        upCheckBox1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                downCheckBox1.setChecked(false);
            }
        });

        downCheckBox1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                upCheckBox1.setChecked(false);
            }
        });

        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }

    private void handleCountTimerPlayPause() {
        if (isTimerRunning) {
            timerPause();
        } else {
            timerStart();
        }
    }
    private void saveTimerValues(int hours, int minutes, int seconds, boolean upChecked, boolean downChecked) {
        SharedPreferences.Editor editor = sharedPreferences1.edit();
        editor.putString("timer_hour", String.valueOf(hours));
        editor.putString("timer_minute", String.valueOf(minutes));
        editor.putString("timer_second", String.valueOf(seconds));
        editor.putBoolean("upcheckbox", upChecked);
        editor.putBoolean("downcheckbox", downChecked);
        editor.apply();

        if (upChecked) {
            currentTimerMode = MODE_COUNT_UP;
            isCountUp = true;
        } else if (downChecked) {
            currentTimerMode = MODE_COUNT_DOWN;
            isCountUp = false;
        } else {
            currentTimerMode = MODE_STOPWATCH;
        }

        timeVar = hours * 60 * 60 + minutes * 60 + seconds;
        timeVarEdit = timeVar;
        maxTimeInSeconds = timeVar;

        pausedTimeInSeconds = 0;
        pausedCountDownTimeInSeconds = 0;

        updateDisplayForCurrentMode();
    }
    public void timerPause() {
        cdflag = 0;
        isTimerRunning = false;
        playPause.setImageResource(R.drawable.ic_play);

        if (countDownTimer != null) {
            pausedCountDownTimeInSeconds = timeVarEdit;
            countDownTimer.cancel();
        }
        if (countUpTimer != null) {
            // For count up, store the current progress
            pausedTimeInSeconds = currentTimeInSeconds;
            countUpTimer.cancel();
        }
    }
    private void timeDone() {
        resetAllTimerStates();

        if (currentTimerMode == MODE_COUNT_UP) {
            updateTimerDisplay(method5(maxTimeInSeconds));
        } else if (currentTimerMode == MODE_COUNT_DOWN) {
            updateTimerDisplay("00:00:00");
        }

        playPause.setImageResource(R.drawable.ic_play);
        resetButton.setClickable(true);

        // Optional: Add a completion sound or vibration
        // Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // vibrator.vibrate(500);
    }
    public void timerStart() {
        if (cdflag == 0) {
            cdflag = 1;
            isTimerRunning = true;
            playPause.setImageResource(R.drawable.ic_pause);
            resetButton.setClickable(true);

            switch (currentTimerMode) {
                case MODE_COUNT_UP:
                    // If starting count up from reset, ensure we start from 0
                    if (pausedTimeInSeconds == 0) {
                        currentTimeInSeconds = 0;
                    }
                    startCountUpTimer();
                    break;

                case MODE_COUNT_DOWN:
                    // If starting count down from reset, ensure we start from full time
                    if (pausedCountDownTimeInSeconds == 0) {
                        timeVarEdit = timeVar;
                    }
                    startCountDownTimer();
                    break;
            }
        }
    }

    private void startCountUpTimer() {
        if (countUpTimer != null) {
            countUpTimer.cancel();
            countUpTimer = null;
        }

        // Calculate the time to count up to (from current position to max)
        long timeToCount = (maxTimeInSeconds - currentTimeInSeconds) * 1000L;

        countUpTimer = new  CountUpTimer(timeToCount, 1000) {
            @Override
            public void onTick(long millisElapsed) {
                if (cdflag == 1 && isTimerRunning) {
                    int elapsedSeconds = (int) (millisElapsed / 1000);
                    int totalSeconds = currentTimeInSeconds + elapsedSeconds;

                    // Update display
                    updateTimerDisplay(method5(totalSeconds));

                    // Check if we've reached the maximum time
                    if (totalSeconds >= maxTimeInSeconds) {
                        timeDone();
                    }
                }
            }

            @Override
            public void onFinish() {
                timeDone();
            }
        };

        countUpTimer.start();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        if (doubleBackToExitPressedOnce) {
            // Second back press - exit
            cleanupBeforeExit();
            super.onBackPressed(); // This is okay here since we want to exit
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please press BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler(Looper.getMainLooper()).postDelayed(() ->
                doubleBackToExitPressedOnce = false, 2000);
    }

    private void cleanupBeforeExit() {
        if (bluetoothManager != null) {
            bluetoothManager.disconnect();
            finish();
        }
        saveCurrentValues();
        stopAutoRepeat();
    }

    public void exitApplication() {
        final AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setMessage("Are you sure you want to exit application?");
        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(DeviceList.this, "Cancel", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        adb.setNeutralButton("Rate", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String appPackageName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                } catch (ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                }
            }
        });
        AlertDialog alert = adb.create();
        alert.show();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_exit) {
            exitApplication();
        } else if (id == R.id.action_share) {
            shareApp();
        } else if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_searchList) {
            ScanDevicesList();
        } else if (id == R.id.action_pairedList) {
            pairedDevicesList();
        } else if (id == R.id.action_disconnect) {
            bluetoothManager.disconnect();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    private static final int SCAN_ACTIVITY_REQUEST_CODE = 1001;

    private void ScanDevicesList() {
        Intent intent = new Intent(this, ScanActivity.class);
        if (Build.VERSION.SDK_INT > 20) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
            startActivityForResult(intent, SCAN_ACTIVITY_REQUEST_CODE, options.toBundle());
        } else {
            startActivityForResult(intent, SCAN_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SCAN_ACTIVITY_REQUEST_CODE) {
            // Handle any results from ScanActivity if needed
        }
    }
    public void shareApp() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name");
            String shareMessage = "\nLet me recommend you this application\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + getPackageName() + "\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception e) {
        }
    }


    // Custom CountUpTimer class
    public abstract class CountUpTimer {
        private final long interval;
        private final long duration;
        private long elapsedTime = 0;
        private boolean isRunning = false;
        private Handler handler;
        private Runnable timerRunnable;

        public CountUpTimer(long duration, long interval) {
            this.duration = duration;
            this.interval = interval;
            this.handler = new Handler(Looper.getMainLooper());

            this.timerRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isRunning) {
                        elapsedTime += interval;

                        if (elapsedTime >= duration) {
                            onFinish();
                            cancel();
                        } else {
                            onTick(elapsedTime);
                            handler.postDelayed(this, interval);
                        }
                    }
                }
            };
        }

        public abstract void onTick(long millisElapsed);
        public abstract void onFinish();

        public void start() {
            if (!isRunning) {
                isRunning = true;
                handler.postDelayed(timerRunnable, interval);
            }
        }

        public void cancel() {
            isRunning = false;
            handler.removeCallbacks(timerRunnable);
        }

        public void pause() {
            isRunning = false;
            handler.removeCallbacks(timerRunnable);
        }

        public void resume() {
            if (!isRunning) {
                isRunning = true;
                handler.postDelayed(timerRunnable, interval);
            }
        }

        public long getElapsedTime() {
            return elapsedTime;
        }
    }
    private void handleStopwatchPlayPause() {
        if (isPlaying) {
            pauseStopwatch();
            isPlaying = false;
            resetButton.setClickable(true);
        } else {
            playStopwatch();
            isPlaying = true;
            resetButton.setClickable(false);
        }
    }
    private void setupButtonListeners() {
        // Initialize the array first
        arrayOfControlButtons = new ImageButton[] {
                tempMinusButton,
                tempPlusButton,
                humBtnMinus,
                humBtnPlus
                // Add any other buttons that should have long press functionality
        };

        // Set up all button listeners
        tempMinusButton.setOnClickListener(this);
        tempPlusButton.setOnClickListener(this);
        humBtnMinus.setOnClickListener(this);
        humBtnPlus.setOnClickListener(this);
        switch1.setOnClickListener(this);
        switch2.setOnClickListener(this);
        lightOneBtn.setOnClickListener(this);
        lightTwoBtn.setOnClickListener(this);
        lightThreeBtn.setOnClickListener(this);
        lightFourBtn.setOnClickListener(this);

        // Timer buttons
        playPause.setOnClickListener(v -> {
            if (currentTimerMode == MODE_STOPWATCH) {
                handleStopwatchPlayPause();
            } else {
                handleCountTimerPlayPause();
            }
        });

        resetButton.setOnClickListener(v -> resetTimer());
        settingButton.setOnClickListener(v -> showTimerSettingsPopup());

        // Check if array is not null before iterating
        if (arrayOfControlButtons != null) {
            for (ImageButton b : arrayOfControlButtons) {
                b.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(final View v) {
                        startAutoRepeat(v);
                        return true;
                    }
                });

                b.setOnTouchListener((v, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                        stopAutoRepeat();
                    }
                    return false;
                });
            }
        }
    }
    private void resetAllTimerStates() {
        cdflag = 0;
        isTimerRunning = false;
        isPlaying = false;
        pausedTimeInSeconds = 0;
        pausedCountDownTimeInSeconds = 0;
        currentTimeInSeconds = 0;
        timeSwapBuff = 0;
        timeInMilliseconds = 0L;
        updatedTime = 0L;

        // Cancel and nullify all timers
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        if (countUpTimer != null) {
            countUpTimer.cancel();
            countUpTimer = null;
        }
        customHandler.removeCallbacks(updateTimerThread);

        // Reset UI
        playPause.setImageResource(R.drawable.ic_play);
    }
    public void resetTimer() {
        // First, completely reset all timer states
        resetAllTimerStates();

        // Now handle mode-specific reset
        switch (currentTimerMode) {
            case MODE_STOPWATCH:
                // For stopwatch, reset to 00:00:00
                timerValue.setText("00:00:00");
                startTime = SystemClock.uptimeMillis();
                break;

            case MODE_COUNT_UP:
                // For count up, reset to 00:00:00
                currentTimeInSeconds = 0;
                timeVarEdit = 0;
                updateTimerDisplay(method5(0));
                break;

            case MODE_COUNT_DOWN:
                // For count down, reset to the full configured time
                timeVarEdit = timeVar;
                updateTimerDisplay(method5(timeVar));
                break;
        }

        // Make sure UI reflects reset state
        playPause.setImageResource(R.drawable.ic_play);
        resetButton.setClickable(false);
    }
    private void updateTimerDisplay(String timeText) {
        timerValue.setText(timeText);
    }
    private void startAutoRepeat(View v) {
        stopAutoRepeat(); // Stop any existing auto-repeat

        autoRepeatTimer = new Timer();
        autoRepeatTask = new TimerTask() {
            @Override
            public void run() {
                uiHandler.post(() -> {
                    int id = v.getId();
                    if (id == R.id.tempBtnPlus) {
                        currentDisplayValue = Math.min(DisplayValueMax3, currentDisplayValue + 1);

                    } else if (id == R.id.tempBtnMinus) {
                        currentDisplayValue = Math.max(DisplayValueMin, currentDisplayValue - 1);

                    } else if (id == R.id.humBtnPlus) {
                        currentHumValue = Math.min(DisplayValueMax3, currentHumValue + 1);

                    } else if (id == R.id.humBtnMinus) {
                        currentHumValue = Math.max(DisplayValueMin, currentHumValue - 1);

                    }
                    updateHumDisplay();
                    updateDisplay();
                });
            }
        };

        autoRepeatTimer.schedule(autoRepeatTask, 500, 100); // Start after 500ms, repeat every 100ms
    }

    private void stopAutoRepeat() {
        if (autoRepeatTimer != null) {
            autoRepeatTimer.cancel();
            autoRepeatTimer = null;
        }
        if (autoRepeatTask != null) {
            autoRepeatTask.cancel();
            autoRepeatTask = null;
        }
    }

    private void setupClock() {
        TextView clockView = findViewById(R.id.hk_date);
        TextView clocTime = findViewById(R.id.hk_time);

        // Use a single handler for both date and time updates
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String pattern = "dd MMM yyyy";
                String pattern2 = "hh:mm";
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
                SimpleDateFormat clocTime2 = new SimpleDateFormat(pattern2, Locale.getDefault());
                clockView.setText(sdf.format(new Date()));
                clocTime.setText(clocTime2.format(new Date()));
                customHandler.postDelayed(this, 1000); // update every second
            }
        };
        customHandler.post(runnable);
    }

    // Add these class variables for auto-repeat functionality
    private Timer autoRepeatTimer;
    private TimerTask autoRepeatTask;

    /*************************************************************************************************
     *                              Start Increment and Decrement
     *************************************************************************************************/
    // ON-CLICKS (referred to from XML)
    public void tempBtnMinusPressed() {
        currentDisplayValue = Math.max(DisplayValueMin, currentDisplayValue - 1);
        bluetoothManager.tempSet =currentDisplayValue;
        updateDisplay();
        saveCurrentValues(); // Save after change
    }

    public void tempBtnPlusPressed() {
        currentDisplayValue = Math.min(DisplayValueMax3, currentDisplayValue + 1);
        bluetoothManager.tempSet =currentDisplayValue;
        updateDisplay();
        saveCurrentValues(); // Save after change
    }

    // ON-CLICKS (referred to from XML)
    public void humBtnMinusPressed() {
        currentHumValue = Math.max(DisplayValueMin, currentHumValue - 1);
        bluetoothManager.humidSet =currentHumValue;
        updateHumDisplay();
        saveCurrentValues(); // Save after change
    }

    public void humBtnPlusPressed() {
        currentHumValue = Math.min(DisplayValueMax3, currentHumValue + 1);
        bluetoothManager.humidSet =currentHumValue;
        updateHumDisplay();
        saveCurrentValues(); // Save after change
    }

    // INTERNAL
    private void updateDisplay() {
        tempSetTextView.setText(currentDisplayValue.toString());
    }

    private void updateHumDisplay() {
        humidSetTextView.setText(currentHumValue.toString());
    }

    /*************************************************************************************************
     *                              End Increment and Decrement
     *************************************************************************************************/

    // Timer methods optimized for performance
    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            if (isPlaying) {
                timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
                updatedTime = timeSwapBuff + timeInMilliseconds;

                int seconds = (int) (updatedTime / 1000);
                int minutes = seconds / 60;
                int hours = seconds / 3600;
                seconds = seconds % 60;

                // Only update UI if the text has changed
                String newString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                if (!newString.equals(timerValue.getText().toString())) {
                    timerValue.setText(newString);
                }

                if (isPlaying) {
                    customHandler.postDelayed(this, 1000); // Reduced frequency from 0ms to 100ms
                }
            }
        }
    };

    public void playStopwatch() {
        startTime = SystemClock.uptimeMillis();
        customHandler.postDelayed(updateTimerThread, 0);
        playPause.setImageResource(R.drawable.ic_pause);
        resetButton.setClickable(false);
    }

    public void pauseStopwatch() {
        timeSwapBuff += timeInMilliseconds;
        customHandler.removeCallbacks(updateTimerThread);
        playPause.setImageResource(R.drawable.ic_play);
        resetButton.setClickable(true);
    }

    // Optimized Bluetooth connection method
    private void connectToDevice(String address, String info) {

        // Save the address and info for use in callback
        this.currentConnectionAddress = address;
        this.currentConnectionInfo = info;

        if (!BluetoothConnectionManager.checkBluetoothPermissions(this)) {
            BluetoothConnectionManager.requestBluetoothPermissions(this, 1001);
            return;
        }

        // Show progress in a non-blocking way
        uiHandler.post(() -> {
            if (progress == null) {
                progress = new ProgressDialog(this);
                progress.setMessage("Connecting...");
                progress.setCancelable(false);
            }
            progress.show();
        });

        // Run connection in background
        backgroundExecutor.execute(() -> {
            bluetoothManager.connect(address, info, this);

            // Dismiss progress on UI thread
            uiHandler.post(() -> {
                if (progress != null && progress.isShowing()) {
                    progress.dismiss();
                }
            });
        });
    }

    @Override
    public void onConnectionResult(int resultCode, String message) {
        uiHandler.post(() -> {
            switch (resultCode) {
                case BluetoothConnectionManager.CONNECTION_SUCCESS:
                    connectionStatusIcon.setImageResource(R.drawable.ic_bluetooth_connected);
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                    // SAVE DEVICE FOR AUTO-CONNECT
                    saveDeviceForAutoConnect();
                    break;
                case BluetoothConnectionManager.CONNECTION_FAILED:
                    connectionStatusIcon.setImageResource(R.drawable.ic_bluetooth_disconnected);
                    Toast.makeText(this, "Bluetooth connection failed " + message, Toast.LENGTH_LONG).show();
                    break;
                // Handle other cases...
            }
        });
    }

    private void saveDeviceForAutoConnect() {
        if (currentConnectionAddress != null && currentConnectionInfo != null) {
            lastDevicePrefs.edit()
                    .putString("address", currentConnectionAddress)
                    .putString("info", currentConnectionInfo)
                    .putLong("timestamp", System.currentTimeMillis())
                    .apply();

            Log.d("AutoConnect", "Device saved for auto-connect: " + currentConnectionAddress);
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        saveCurrentValues();
    }
    @Override
    public void onDataReceived(byte[] data) {
        uiHandler.post(() -> {
            String received = new String(data);
            Log.d("onDataReceived", "Received Data: " + received);
            // Process data without blocking UI
        });
    }

    @Override
    public void onConnectionLost() {
        uiHandler.post(() -> {
           // connectionStatus.setText("Connection lost");
            connectionStatusIcon.setImageResource(R.drawable.ic_bluetooth_disconnected);
            Toast.makeText(this, "Connection lost, attempting to reconnect...", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onSensorDataUpdated(int temperature, int humidity, int pressure) {
        // Update UI with sensor values
        runOnUiThread(() -> {
            temperatureTextView.setText(String.valueOf(temperature));
            humidityTextView.setText(String.valueOf(humidity));
            pressureTextView.setText(String.valueOf(pressure));

            checkdigitalinputs();

        });
    }

    /*
    private void checkdigitalinputs() {
        bluetoothManager.ac = 0;

        // Gas One
        if ((bluetoothManager.DigitalIN[1] & bluetoothManager.DigitalMASK[0]) == bluetoothManager.DigitalMASK[0]) {
            bluetoothManager.ac++;
            gasOneStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasOneStatus.setText("HIGH");
            gasOneStatus.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.GONE);
        } else if ((bluetoothManager.DigitalIN[1] & bluetoothManager.DigitalMASK[1]) == bluetoothManager.DigitalMASK[1]) {
            bluetoothManager.ac++;
            gasOneStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasOneStatus.setText("LOW");
            gasOneStatus.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.GONE);
        } else {
            gasOneStatus.setBackgroundResource(R.drawable.mybutton);
            gasOneStatus.setText("OK");
            if (gasOneStatus.getVisibility() == View.GONE) {
                gasOneStatus.setVisibility(View.VISIBLE);
            }
        }

        // Gas Two
        if ((bluetoothManager.DigitalIN[1] & bluetoothManager.DigitalMASK[2]) == bluetoothManager.DigitalMASK[2]) {
            bluetoothManager.ac++;
            gasTwoStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasTwoStatus.setText("HIGH");
            gasTwoStatus.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.GONE);
        } else if ((bluetoothManager.DigitalIN[1] & bluetoothManager.DigitalMASK[3]) == bluetoothManager.DigitalMASK[3]) {
            bluetoothManager.ac++;
            gasTwoStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasTwoStatus.setText("LOW");
            gasTwoStatus.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.GONE);
        } else {
            gasTwoStatus.setBackgroundResource(R.drawable.mybutton);
            gasTwoStatus.setText("OK");
            if (gasTwoStatus.getVisibility() == View.GONE) {
                gasTwoStatus.setVisibility(View.VISIBLE);
            }
        }

        // Gas Three
        if ((bluetoothManager.DigitalIN[1] & bluetoothManager.DigitalMASK[4]) == bluetoothManager.DigitalMASK[4]) {
            bluetoothManager.ac++;
            gasThreeStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasThreeStatus.setText("HIGH");
            gasThreeStatus.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.GONE);
        } else if ((bluetoothManager.DigitalIN[1] & bluetoothManager.DigitalMASK[5]) == bluetoothManager.DigitalMASK[5]) {
            bluetoothManager.ac++;
            gasThreeStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasThreeStatus.setText("LOW");
            gasThreeStatus.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.GONE);
        } else {
            gasThreeStatus.setBackgroundResource(R.drawable.mybutton);
            gasThreeStatus.setText("OK");
            if (gasThreeStatus.getVisibility() == View.GONE) {
                gasThreeStatus.setVisibility(View.VISIBLE);
            }
        }

        // Gas Four
        if ((bluetoothManager.DigitalIN[1] & bluetoothManager.DigitalMASK[6]) == bluetoothManager.DigitalMASK[6]) {
            bluetoothManager.ac++;
            gasFourStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasFourStatus.setText("HIGH");
            gasFourStatus.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.GONE);
        } else if ((bluetoothManager.DigitalIN[1] & bluetoothManager.DigitalMASK[7]) == bluetoothManager.DigitalMASK[7]) {
            bluetoothManager.ac++;
            gasFourStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasFourStatus.setText("LOW");
            gasFourStatus.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.GONE);
        } else {
            gasFourStatus.setBackgroundResource(R.drawable.mybutton);
            gasFourStatus.setText("OK");
            if (gasFourStatus.getVisibility() == View.GONE) {
                gasFourStatus.setVisibility(View.VISIBLE);
            }
        }

        // Gas Five
        if ((bluetoothManager.DigitalIN[0] & bluetoothManager.DigitalMASK[0]) == bluetoothManager.DigitalMASK[0]) {
            bluetoothManager.ac++;
            gasFiveStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasFiveStatus.setText("HIGH");
            gasFiveStatus.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.GONE);
        } else if ((bluetoothManager.DigitalIN[0] & bluetoothManager.DigitalMASK[1]) == bluetoothManager.DigitalMASK[1]) {
            bluetoothManager.ac++;
            gasFiveStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasFiveStatus.setText("LOW");
            gasFiveStatus.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.GONE);
        } else {
            gasFiveStatus.setBackgroundResource(R.drawable.mybutton);
            gasFiveStatus.setText("OK");
            if (gasFiveStatus.getVisibility() == View.GONE) {
                gasFiveStatus.setVisibility(View.VISIBLE);
            }
        }

        // Gas Six
        if ((bluetoothManager.DigitalIN[0] & bluetoothManager.DigitalMASK[2]) == bluetoothManager.DigitalMASK[2]) {
            bluetoothManager.ac++;
            gasSixStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasSixStatus.setText("HIGH");
            gasSixStatus.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.GONE);
        } else if ((bluetoothManager.DigitalIN[0] & bluetoothManager.DigitalMASK[3]) == bluetoothManager.DigitalMASK[3]) {
            bluetoothManager.ac++;
            gasSixStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasSixStatus.setText("LOW");
            gasSixStatus.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.GONE);
        } else {
            gasSixStatus.setBackgroundResource(R.drawable.mybutton);
            gasSixStatus.setText("OK");
            if (gasSixStatus.getVisibility() == View.GONE) {
                gasSixStatus.setVisibility(View.VISIBLE);
            }
        }

        // HEPA Filter (Gas Seven)
        if ((bluetoothManager.DigitalIN[0] & bluetoothManager.DigitalMASK[4]) == bluetoothManager.DigitalMASK[4]) {
            bluetoothManager.ac++;
            gasSevenStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasSevenStatus.setText("CHOKE");
            gasSevenStatus.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.GONE);
        } else {
            gasSevenStatus.setBackgroundResource(R.drawable.mybutton);
            gasSevenStatus.setText("HEALTHY");
            if (gasSevenStatus.getVisibility() == View.GONE) {
                gasSevenStatus.setVisibility(View.VISIBLE);
            }
        }

        // Buzzer control logic
        if (bluetoothManager.ac > bluetoothManager.pac) {
            bluetoothManager.DigitalOUT[0] |= 0x01; // buzzer on
            bluetoothManager.Muteflag = 0;           // Unmute

         //  dataModel.speakerStatus = String.valueOf(bluetoothManager.speakerStatus);
         //   speakerOnOff.setBackground(dbHandlr.byteArrayToBitmap(dataModel.icon_speaker));
          //  dbHandlr.updateSpeakerStatus(m_dbConnection, dataModel);
        }

        if (bluetoothManager.ac2 == 1) {
            bluetoothManager.ac2 = 2;
            bluetoothManager.DigitalOUT[0] |= 0x01; // buzzer on
            bluetoothManager.Muteflag = 0;           // Unmute

          //  dataModel.speakerStatus = String.valueOf(bluetoothManager.speakerStatus);
         //   speakerOnOff.setBackground(dbHandlr.byteArrayToBitmap(dataModel.icon_speaker));
           // dbHandlr.updateSpeakerStatus(m_dbConnection, dataModel);
        }

        if (bluetoothManager.ac == 0 && bluetoothManager.ac2 == 0) {
            bluetoothManager.DigitalOUT[0] &= 0xFE; // buzzer off
            bluetoothManager.Muteflag = 0;           // Unmute

           // dataModel.speakerStatus = String.valueOf(bluetoothManager.speakerStatus);
            //speakerOnOff.setBackground(dbHandlr.byteArrayToBitmap(dataModel.icon_speaker));
           // dbHandlr.updateSpeakerStatus(m_dbConnection, dataModel);
        }

        bluetoothManager.pac = bluetoothManager.ac;
    }

     */

    private void checkdigitalinputs() {
        bluetoothManager.ac = 0;

        // Gas One
        if ((bluetoothManager.DigitalIN[1] & bluetoothManager.DigitalMASK[0]) == bluetoothManager.DigitalMASK[0]) {
            bluetoothManager.ac++;
            gasOneStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasOneStatus.setText("HIGH");
            gasOneStatus.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.INVISIBLE);
        } else if ((bluetoothManager.DigitalIN[1] & bluetoothManager.DigitalMASK[1]) == bluetoothManager.DigitalMASK[1]) {
            bluetoothManager.ac++;
            gasOneStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasOneStatus.setText("LOW");
            gasOneStatus.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.INVISIBLE);
        } else {
            gasOneStatus.setBackgroundResource(R.drawable.mybutton);
            gasOneStatus.setText("OK");
            if (gasOneStatus.getVisibility() == View.INVISIBLE) {
                gasOneStatus.setVisibility(View.VISIBLE);
            }
        }

        // Gas Two
        if ((bluetoothManager.DigitalIN[1] & bluetoothManager.DigitalMASK[2]) == bluetoothManager.DigitalMASK[2]) {
            bluetoothManager.ac++;
            gasTwoStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasTwoStatus.setText("HIGH");
            gasTwoStatus.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.INVISIBLE);
        } else if ((bluetoothManager.DigitalIN[1] & bluetoothManager.DigitalMASK[3]) == bluetoothManager.DigitalMASK[3]) {
            bluetoothManager.ac++;
            gasTwoStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasTwoStatus.setText("LOW");
            gasTwoStatus.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.INVISIBLE);
        } else {
            gasTwoStatus.setBackgroundResource(R.drawable.mybutton);
            gasTwoStatus.setText("OK");
            if (gasTwoStatus.getVisibility() == View.INVISIBLE) {
                gasTwoStatus.setVisibility(View.VISIBLE);
            }
        }

        // Gas Three
        if ((bluetoothManager.DigitalIN[1] & bluetoothManager.DigitalMASK[4]) == bluetoothManager.DigitalMASK[4]) {
            bluetoothManager.ac++;
            gasThreeStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasThreeStatus.setText("HIGH");
            gasThreeStatus.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.INVISIBLE);
        } else if ((bluetoothManager.DigitalIN[1] & bluetoothManager.DigitalMASK[5]) == bluetoothManager.DigitalMASK[5]) {
            bluetoothManager.ac++;
            gasThreeStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasThreeStatus.setText("LOW");
            gasThreeStatus.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.INVISIBLE);
        } else {
            gasThreeStatus.setBackgroundResource(R.drawable.mybutton);
            gasThreeStatus.setText("OK");
            if (gasThreeStatus.getVisibility() == View.INVISIBLE) {
                gasThreeStatus.setVisibility(View.VISIBLE);
            }
        }

        // Gas Four
        if ((bluetoothManager.DigitalIN[1] & bluetoothManager.DigitalMASK[6]) == bluetoothManager.DigitalMASK[6]) {
            bluetoothManager.ac++;
            gasFourStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasFourStatus.setText("HIGH");
            gasFourStatus.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.INVISIBLE);
        } else if ((bluetoothManager.DigitalIN[1] & bluetoothManager.DigitalMASK[7]) == bluetoothManager.DigitalMASK[7]) {
            bluetoothManager.ac++;
            gasFourStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasFourStatus.setText("LOW");
            gasFourStatus.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.INVISIBLE);
        } else {
            gasFourStatus.setBackgroundResource(R.drawable.mybutton);
            gasFourStatus.setText("OK");
            if (gasFourStatus.getVisibility() == View.INVISIBLE) {
                gasFourStatus.setVisibility(View.VISIBLE);
            }
        }

        // Gas Five
        if ((bluetoothManager.DigitalIN[0] & bluetoothManager.DigitalMASK[0]) == bluetoothManager.DigitalMASK[0]) {
            bluetoothManager.ac++;
            gasFiveStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasFiveStatus.setText("HIGH");
            gasFiveStatus.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.INVISIBLE);
        } else if ((bluetoothManager.DigitalIN[0] & bluetoothManager.DigitalMASK[1]) == bluetoothManager.DigitalMASK[1]) {
            bluetoothManager.ac++;
            gasFiveStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasFiveStatus.setText("LOW");
            gasFiveStatus.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.INVISIBLE);
        } else {
            gasFiveStatus.setBackgroundResource(R.drawable.mybutton);
            gasFiveStatus.setText("OK");
            if (gasFiveStatus.getVisibility() == View.INVISIBLE) {
                gasFiveStatus.setVisibility(View.VISIBLE);
            }
        }

        // Gas Six
        if ((bluetoothManager.DigitalIN[0] & bluetoothManager.DigitalMASK[2]) == bluetoothManager.DigitalMASK[2]) {
            bluetoothManager.ac++;
            gasSixStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasSixStatus.setText("HIGH");
            gasSixStatus.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.INVISIBLE);
        } else if ((bluetoothManager.DigitalIN[0] & bluetoothManager.DigitalMASK[3]) == bluetoothManager.DigitalMASK[3]) {
            bluetoothManager.ac++;
            gasSixStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasSixStatus.setText("LOW");
            gasSixStatus.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.INVISIBLE);
        } else {
            gasSixStatus.setBackgroundResource(R.drawable.mybutton);
            gasSixStatus.setText("OK");
            if (gasSixStatus.getVisibility() == View.INVISIBLE) {
                gasSixStatus.setVisibility(View.VISIBLE);
            }
        }

        // HEPA Filter (Gas Seven)
        if ((bluetoothManager.DigitalIN[0] & bluetoothManager.DigitalMASK[4]) == bluetoothManager.DigitalMASK[4]) {
            bluetoothManager.ac++;
            rightPart3.setBackgroundResource(R.drawable.mybutton_red);
           // gasSevenStatus.setBackgroundResource(R.drawable.mybutton_red);
            gasSevenStatus.setText("CHOKE");
            rightPart3.setVisibility(bluetoothManager.toggle ? View.VISIBLE : View.INVISIBLE);
        } else {
          //  gasSevenStatus.setBackgroundResource(R.drawable.mybutton);
            rightPart3.setBackgroundResource(R.drawable.mybutton);
            gasSevenStatus.setText("HEALTHY");
            if (rightPart3.getVisibility() == View.INVISIBLE) {
                rightPart3.setVisibility(View.VISIBLE);
            }
        }

        // Buzzer control logic
        if (bluetoothManager.ac > bluetoothManager.pac) {
            bluetoothManager.DigitalOUT[0] |= 0x01; // buzzer on
            bluetoothManager.Muteflag = 0;           // Unmute

            //  dataModel.speakerStatus = String.valueOf(bluetoothManager.speakerStatus);
            //   speakerOnOff.setBackground(dbHandlr.byteArrayToBitmap(dataModel.icon_speaker));
            //  dbHandlr.updateSpeakerStatus(m_dbConnection, dataModel);
        }

        if (bluetoothManager.ac2 == 1) {
            bluetoothManager.ac2 = 2;
            bluetoothManager.DigitalOUT[0] |= 0x01; // buzzer on
            bluetoothManager.Muteflag = 0;           // Unmute

            //  dataModel.speakerStatus = String.valueOf(bluetoothManager.speakerStatus);
            //   speakerOnOff.setBackground(dbHandlr.byteArrayToBitmap(dataModel.icon_speaker));
            // dbHandlr.updateSpeakerStatus(m_dbConnection, dataModel);
        }

        if (bluetoothManager.ac == 0 && bluetoothManager.ac2 == 0) {
            bluetoothManager.DigitalOUT[0] &= 0xFE; // buzzer off
            bluetoothManager.Muteflag = 0;           // Unmute

            // dataModel.speakerStatus = String.valueOf(bluetoothManager.speakerStatus);
            //speakerOnOff.setBackground(dbHandlr.byteArrayToBitmap(dataModel.icon_speaker));
            // dbHandlr.updateSpeakerStatus(m_dbConnection, dataModel);
        }

        bluetoothManager.pac = bluetoothManager.ac;
    }
    @Override
    public void onSettingsUpdated(int tempSet, int humidSet, int pressureSet) {
        // Update UI with settings values
        runOnUiThread(() -> {
            tempSetTextView.setText(String.valueOf(tempSet));
            humidSetTextView.setText(String.valueOf(humidSet));
            pressureSetTextView.setText(String.valueOf(pressureSet));
        });
    }

    // Optimized paired devices list
    private void pairedDevicesList() {
        backgroundExecutor.execute(() -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            pairedDevices = myBluetooth.getBondedDevices();
            ArrayList<String> list = new ArrayList<>();

            if (pairedDevices.size() > 0) {
                for (BluetoothDevice bt : pairedDevices) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    list.add(bt.getName() + "\n" + bt.getAddress());
                }
            }

            // Update UI on main thread
            uiHandler.post(() -> {
                if (list.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
                    return;
                }

                Dialog dialog = new Dialog(this);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select a paired device for connecting");

                LinearLayout parent = new LinearLayout(DeviceList.this);
                parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                parent.setOrientation(LinearLayout.VERTICAL);

                ListView modeList = new ListView(this);
                setListViewHeightBasedOnItems(modeList);

                final ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
                modeList.setAdapter(modeAdapter);
                modeList.setOnItemClickListener(myListClickListener);
                builder.setView(modeList);
                dialog = builder.create();
                dialog.show();
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 600);
            });
        });
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            if (address != null) {
                connectToDevice(address, info);
            }
        }
    };
    public static boolean setListViewHeightBasedOnItems(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {
            int numberOfItems = listAdapter.getCount();
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            int totalDividersHeight = listView.getDividerHeight() * (numberOfItems - 1);
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();
            return true;
        } else {
            return false;
        }
    }

    // Optimized timer methods
    private void startCountDownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        long startTimeMillis = (pausedCountDownTimeInSeconds > 0) ?
                pausedCountDownTimeInSeconds * 1000L :
                timeVarEdit * 1000L;

        countDownTimer = new CountDownTimer(startTimeMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (cdflag == 1 && isTimerRunning) {
                    timeVarEdit = (int) (millisUntilFinished / 1000);
                    // Update UI efficiently
                    String newTime = method5(timeVarEdit);
                    if (!newTime.equals(timerValue.getText().toString())) {
                        uiHandler.post(() -> timerValue.setText(newTime));
                    }
                    pausedCountDownTimeInSeconds = timeVarEdit;
                }
            }

            @Override
            public void onFinish() {
                uiHandler.post(() -> timeDone());
            }
        }.start();
    }

    // Memory management improvements
    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveCurrentValues();
        // Clean up handlers and executors
        if (customHandler != null) {
            customHandler.removeCallbacksAndMessages(null);
        }

        if (uiHandler != null) {
            uiHandler.removeCallbacksAndMessages(null);
        }

        if (backgroundExecutor != null) {
            backgroundExecutor.shutdownNow();
        }

        // Clean up Bluetooth
        if (bluetoothManager != null) {
            bluetoothManager.shutdown();
        }

        // Clean up timers
        stopAutoRepeat();
        resetAllTimerStates();
    }

    // Additional optimizations for UI responsiveness
    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.switch1) {
            switch1(v);
        } else if (id == R.id.switch2) {
            switch2(v);
        } else if (id == R.id.tempBtnPlus) {
            tempBtnPlusPressed();
        } else if (id == R.id.tempBtnMinus) {
            tempBtnMinusPressed();
        } else if (id == R.id.humBtnPlus) {
            humBtnPlusPressed();
        } else if (id == R.id.humBtnMinus) {
            humBtnMinusPressed();
        } else if (id == R.id.lightOneBtn) {
            switch1Light(v);
        } else if (id == R.id.lightTwoBtn) {
            switch2Light(v);
        } else if (id == R.id.lightThreeBtn) {
            switch3Light(v);
        } else if (id == R.id.lightFourBtn) {
            switch4Light(v);
        }
    }
    public void switch1Light(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (isLightOneOn) {
                lightOneBtn.setImageResource(R.drawable.ic_bulb_off);
                bluetoothManager.DigitalOUT[1] &= 0xFB;
                isLightOneOn = false;
                editor.putBoolean("light1", false);
            } else {
                lightOneBtn.setImageResource(R.drawable.ic_bulb_on);
                bluetoothManager.DigitalOUT[1] |= 0x04;
                isLightOneOn = true;
                editor.putBoolean("light1", true);
            }
            editor.apply();
        }
    }

    public void switch2Light(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (isLightTwoOn) {
                lightTwoBtn.setImageResource(R.drawable.ic_bulb_off);
                bluetoothManager.DigitalOUT[1] &= 0xF7;
                editor.putBoolean("light2", false);
                isLightTwoOn = false;
            } else {
                lightTwoBtn.setImageResource(R.drawable.ic_bulb_on);
                bluetoothManager.DigitalOUT[1] |= 0x08;
                editor.putBoolean("light2", true);
                isLightTwoOn = true;
            }
            editor.apply();
        }
    }

    public void switch3Light(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (isLightThreeOn) {
                lightThreeBtn.setImageResource(R.drawable.ic_bulb_off);
                bluetoothManager.DigitalOUT[1] &= 0xEF;
                isLightThreeOn = false;
                editor.putBoolean("light3", false);
            } else {
                lightThreeBtn.setImageResource(R.drawable.ic_bulb_on);
                bluetoothManager.DigitalOUT[1] |= 0x10;
                isLightThreeOn = true;
                editor.putBoolean("light3", true);
            }
            editor.apply();
        }
    }

    public void switch4Light(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (isLightFourOn) {
                lightFourBtn.setImageResource(R.drawable.ic_bulb_off);
                bluetoothManager.DigitalOUT[1] &= 0xDF;
                isLightFourOn = false;
                editor.putBoolean("light4", false);
            } else {
                lightFourBtn.setImageResource(R.drawable.ic_bulb_on);
                bluetoothManager.DigitalOUT[1] |= 0x20;
                isLightFourOn = true;
                editor.putBoolean("light4", true);
            }
            editor.apply();
        }
    }
    // Optimized switch methods
    @SuppressLint("NewApi")
    public void switch1(View v) {
        boolean isChecked = switch1.isChecked();

        if (isChecked) {
            switch1.setThumbColorRes(R.color.red);
            bluetoothManager.DigitalOUT[1] &= 0xFE;
            editor.putBoolean("switch1", true);
        } else {
            switch1.setThumbColorRes(R.color.limeGreen);
            bluetoothManager.DigitalOUT[1] |= 0x01;
            editor.putBoolean("switch1", false);
        }
        editor.apply();

        // Send data in background
        backgroundExecutor.execute(() -> bluetoothManager.sendControllerData());
    }


    public void switch2(View v) {
        boolean isChecked = switch2.isChecked();

        if (isChecked) {
            switch2.setThumbColorRes(R.color.red);
            bluetoothManager.DigitalOUT[1] &= 0xFD;
            editor.putBoolean("switch2", true);
        } else {
            switch2.setThumbColorRes(R.color.limeGreen);
            bluetoothManager.DigitalOUT[1] |= 0x02;
            editor.putBoolean("switch2", false);
        }
        editor.apply();

        // Send data in background
        backgroundExecutor.execute(() -> bluetoothManager.sendControllerData());
    }
    // Similar optimizations for other switch methods...

    // Memory-efficient bitmap handling if needed
    private Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}