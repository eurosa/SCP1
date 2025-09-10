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
    TextView setTempDisplay;
    TextView setHumDisplay;
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
    private TextView connectionStatus;
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

    /***************************************************************************************
     * End Stop Watch
     ****************************************************************************************/

    //screenshot
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scp);
//----------------------------Grant storage permission--------------------------------------------------
        setAnimation();
        bluetoothManager = new BluetoothConnectionManager(this);
        /***************************************************************************************
         *   Play and pause in only one button - Android
         ****************************************************************************************/
        playPause = findViewById(R.id.startButton);
        settingButton = findViewById(R.id.settingButton);
        // Start from your activity
        //startService(new Intent(this, BluetoothService.class));

        timerValue = (TextView) findViewById(R.id.timerValue);
        // Initialize SharedPreferences
        sharedPreferences1 = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Initialize UI components
        initUIComponents();

        // Load saved timer values
        loadTimerValues();

        // Set up button listeners
        setupButtonListeners();

        // Initialize timer display
        updateDisplayForCurrentMode();

        /***************************************************************************************
         *  Play and pause in only one button - Android
         ****************************************************************************************/
        /*********************************************************************************
         * Initialize Database
         *
         * *******************************************************************************/
        dataModel = new DataModel();
        dbHandler = new DatabaseHandler(this);
        dbHandler.getQmsUtilityById("1", dataModel);
        /*********************************************************************************
         * Initialize Database
         *
         * *******************************************************************************/
        /************************************************************************************
         *
         * Discrete Slider
         *
         *************************************************************************************/
        connectionStatusIcon = findViewById(R.id.connectionStatusIcon);
        // mSlider1 = findViewById(R.id.discreteSlider1);
        // mSlider2 = findViewById(R.id.discreteSlider2);
        // mSlider3 = findViewById(R.id.discreteSlider3);
        // mSlider4 = findViewById(R.id.discreteSlider4);
        // setUpView(mSlider1);
        // setUpView(mSlider2);
        // setUpView(mSlider3);
        // setUpView(mSlider4);

        /************************************************************************************
         *
         * Discrete Slider
         *
         *************************************************************************************/
        /*************************************************************************************
         * Switch configure
         **************************************************************************************/
        switch1 = findViewById(R.id.switch1);
        switch2 = findViewById(R.id.switch2);
        lightOneBtn = findViewById(R.id.lightOneBtn);
        lightTwoBtn = findViewById(R.id.lightTwoBtn);
        lightThreeBtn = findViewById(R.id.lightThreeBtn);
        lightFourBtn = findViewById(R.id.lightFourBtn);
        switch1.setOnClickListener(this);
        switch2.setOnClickListener(this);
        lightOneBtn.setOnClickListener(this);
        lightTwoBtn.setOnClickListener(this);
        lightThreeBtn.setOnClickListener(this);
        lightFourBtn.setOnClickListener(this);
        /*************************************************************************************
         * Switch configure
         **************************************************************************************/
        //TabLayout   tabLayout = (TabLayout) findViewById(R.id.simpleTabLayout); // get the reference of TabLayout
        //TabLayout.Tab firstTab = tabLayout.newTab(); // Create a new Tab names
        //firstTab.setText("First Tab"); // set the Text for the first Tab
        //firstTab.setIcon(R.drawable.ic_left_arrow); // set an icon for the first tab
        //tabLayout.addTab(firstTab); // add  the tab to the TabLayout
        //https://abhiandroid.com/materialdesign/tablayout-example-android-studio.html

        connectionStatus = findViewById(R.id.connectionStatus);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        //this.registerReceiver(mReceiver, filter);
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
        /***************************************************************************************
         * Navigation Drawer Layout
         *
         ***************************************************************************************/

        Typeface tf = Typeface.createFromAsset(getApplicationContext().getAssets(), "DSEG7Classic-Bold.ttf");
        //   display.setTypeface(tf);


        //============================Keyboard====================================================//


        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); //receive the address of the bluetooth device
        info_address = newint.getStringExtra(DeviceList.EXTRA_INFO);
        if (address != null) {
            if (address != null) {
                connectToDevice(address, info_address);
            }
        }
        //-------------------------------------To Receive device address from background==================
        //====================================Camera======================================================

        boolean isInstantApp = InstantApps.getPackageManagerCompat(this).isInstantApp();
        Log.d(LOG_TAG, "are we instant?" + isInstantApp);


        //if the device has bluetooth
        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if (myBluetooth == null) {
            //Show a Mensag. That the device has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();

            //finish apk
            finish();
        } else if (!myBluetooth.isEnabled()) {
            //Ask to the user turn the bluetooth on
            Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            startActivityForResult(turnBTon, 1);
        }


        //Camera screenshot
        final boolean hasWritePermission = RuntimePermissionUtil.checkPermissonGranted(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //  imageView = findViewById(R.id.imageView2);


//----------------------------------screen_shot xml view-----------------------------------------
        //Camera screenshot

        //=================================FileExposed============================
        /*
         *
         *
         * android.os.FileUriExposedException: file:///storage/emulated/0/test.txt exposed beyond app through Intent.getData()
         * solved using this
         * */
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
        //=======================================================================

        // ************************************ Floating Action Button ********************************************************
    /*    FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Here's a Snackbar", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    */

/**************************************************************************************
 * Start Stop Watch
 ***************************************************************************************/
        final ImageButton startButton;

        resetButton = findViewById(R.id.resetButton);
        settingButton = findViewById(R.id.settingButton);
        resetButton.setClickable(false);

        resetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                resetElapsedTimer();
            }
        });

        /**************************************************************************************
         * End Stop Watch
         ***************************************************************************************/

        /*final TextClock textClock = new TextClock(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(40, 40, 40, 40);
        textClock.setLayoutParams(layoutParams);


        textClock.setFormat12Hour("hh:mm:ss a");

        RelativeLayout relativeLayout = findViewById(R.id.clkLayout);
        if (relativeLayout != null) {
            relativeLayout.addView(textClock);
        }*/


        /********************************************************************************************
         *                          Start Increment and Decrement
         *********************************************************************************************/
        setTempDisplay = findViewById(R.id.setTemp);
        setHumDisplay = findViewById(R.id.setHum);
        tempMinusButton = findViewById(R.id.tempBtnMinus);
        tempPlusButton = findViewById(R.id.tempBtnPlus);

        humBtnPlus = findViewById(R.id.humBtnPlus);
        humBtnMinus = findViewById(R.id.humBtnMinus);

        tempMinusButton.setOnClickListener(this);
        tempPlusButton.setOnClickListener(this);
        humBtnMinus.setOnClickListener(this);
        humBtnPlus.setOnClickListener(this);

        arrayOfControlButtons = new ImageButton[]{tempPlusButton, tempMinusButton, humBtnPlus, humBtnMinus}; // this could be a large set of buttons

        updateDisplay(); // initial setting of display

        for (ImageButton b : arrayOfControlButtons) {
            b.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(final View v) {
                    final Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (v.isPressed()) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        int id = v.getId();
                                        if (id == R.id.tempBtnPlus) {
                                            currentDisplayValue = currentDisplayValue + 10;
                                        } else if (id == R.id.tempBtnMinus) {
                                            currentDisplayValue = currentDisplayValue - 10;
                                        } else if (id == R.id.humBtnPlus) {
                                            currentHumValue = currentHumValue + 10;
                                        } else if (id == R.id.humBtnMinus) {
                                            currentHumValue = currentHumValue - 10;
                                        }
                                        updateHumDisplay();
                                        updateDisplay();
                                    }
                                });
                            } else {
                                timer.cancel();
                            }
                        }
                    }, 100, 200);
                    return true;
                }
            });
        }


        /********************************************************************************************
         *                          End Increment and Decrement
         *********************************************************************************************/

        sharedPreferences = getSharedPreferences("LightPrefs", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // restore saved states
        restoreStates();
        TextView clockView = findViewById(R.id.hk_date);
        TextView clocTime = findViewById(R.id.hk_time);
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String pattern = "dd MMM yyyy";
                String pattern2 = "hh:mm:ss";
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
                SimpleDateFormat clocTime2 = new SimpleDateFormat(pattern2, Locale.getDefault());
                clockView.setText(sdf.format(new Date()));
                clocTime.setText(clocTime2.format(new Date()));
                handler.postDelayed(this, 1000); // update every second
            }
        };
        handler.post(runnable);
    }

    private void connectToDevice(String address, String info) {
        if (!BluetoothConnectionManager.checkBluetoothPermissions(this)) {
            BluetoothConnectionManager.requestBluetoothPermissions(this, 1001);
            return;
        }

        bluetoothManager.connect(address, info, this);
    }

    /*************************************************************************************************
     *                              Start Increment and Decrement
     *************************************************************************************************/
    // ON-CLICKS (referred to from XML)
    public void tempBtnMinusPressed() {
        currentDisplayValue--;
        updateDisplay();
    }

    public void tempBtnPlusPressed() {
        currentDisplayValue++;
        updateDisplay();
    }

    // ON-CLICKS (referred to from XML)
    public void humBtnMinusPressed() {
        currentHumValue--;
        updateHumDisplay();
    }

    public void humBtnPlusPressed() {
        currentHumValue++;
        updateHumDisplay();
    }

    // INTERNAL
    private void updateDisplay() {
        setTempDisplay.setText(currentDisplayValue.toString());
    }

    private void updateHumDisplay() {
        setHumDisplay.setText(currentHumValue.toString());
    }

    /*************************************************************************************************
     *                              End Increment and Decrement
     *************************************************************************************************/

    public void resetElapsedTimer() {
        resetAllTimerStates();
        timerValue.setText("00:00:00");
        startTime = SystemClock.uptimeMillis();
        resetButton.setClickable(false);
    }

    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            if (isPlaying) {
                timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
                updatedTime = timeSwapBuff + timeInMilliseconds;

                int seconds = (int) (updatedTime / 1000);
                int minutes = seconds / 60;
                int hours = seconds / 3600;
                seconds = seconds % 60;

                String string = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                timerValue.setText(string);

                if (isPlaying) {
                    customHandler.postDelayed(this, 0);
                }
            }
        }
    };

    private void sendData() {
        if (btSocket != null) {
            try {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        handler.postDelayed(this, 100);
                    }
                }, 100);
                String digitNo = dataModel.getDigitNo();
                dataModel.getTypeNo();
                dataModel.getDevId();
                dataModel.getSoundType();
                String displayText = display.getText().toString();
                if (displayText.length() == 2) {
                    displayText = "0" + displayText;
                    digitNo = "3";
                }
                String displayData = fixedLengthString(displayText, 4);

                String data = "$" + dataModel.getDevId() + digitNo + dataModel.getSound_id() + displayData + ";";
                btSocket.getOutputStream().write(data.getBytes());
                Log.d("Display_Digit", data);
            } catch (IOException e) {
                msg("Error");
            }
        }
    }

    private String fixedLengthString(String textData, int length) {
        String stringData = null;
        return stringData;
    }

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

    private void ScanDevicesList() {
        Intent intent = new Intent(this, ScanActivity.class);
        if (Build.VERSION.SDK_INT > 20) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (address != null) {
                    connectToDevice(address, info_address);
                }
            }
        }
    }

    private void pairedDevicesList() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt : pairedDevices) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        Dialog dialog = new Dialog(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a paired device for connecting");

        LinearLayout parent = new LinearLayout(DeviceList.this);
        parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        parent.setOrientation(LinearLayout.VERTICAL);

        ListView modeList = new ListView(this);
        setListViewHeightBasedOnItems(modeList);

        final ArrayAdapter modeAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        modeList.setAdapter(modeAdapter);
        modeList.setOnItemClickListener(myListClickListener);
        builder.setView(modeList);
        dialog = builder.create();
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 600);
    }

    private void pairedDevicesListOriginal() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt : pairedDevices) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener);
    }

    @Override
    public void onConnectionResult(int resultCode, String message) {
        runOnUiThread(() -> {
            switch (resultCode) {
                case BluetoothConnectionManager.CONNECTION_SUCCESS:
                    connectionStatusIcon.setImageResource(R.drawable.ic_bluetooth_connected);
                    Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothConnectionManager.CONNECTION_FAILED:
                    connectionStatusIcon.setImageResource(R.drawable.ic_bluetooth_disconnected);
                    Toast.makeText(this, "Bluetooth connection failed "+message, Toast.LENGTH_LONG).show();
                    break;
                case BluetoothConnectionManager.IO_EXCEPTION:
                    connectionStatusIcon.setImageResource(R.drawable.ic_bluetooth_disconnected);
                    Toast.makeText(this, "IO_EXCEPTION "+message, Toast.LENGTH_LONG).show();
                    break;
                case BluetoothConnectionManager.CONNECTION_LOST:
                    connectionStatusIcon.setImageResource(R.drawable.ic_bluetooth_disconnected);
                    Toast.makeText(this, "Connection Lost "+message, Toast.LENGTH_LONG).show();
                    break;
                case BluetoothConnectionManager.PERMISSION_DENIED:
                    connectionStatus.setText("Permission denied");
                    connectionStatusIcon.setImageResource(R.drawable.ic_bluetooth_disconnected);
                    Toast.makeText(this, "Bluetooth permission required "+message, Toast.LENGTH_LONG).show();
                    break;
                case BluetoothConnectionManager.BLUETOOTH_DISABLED:
                    connectionStatusIcon.setImageResource(R.drawable.ic_bluetooth_disconnected);
                    Toast.makeText(this, "Bluetooth Disabled "+message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    @Override
    public void onDataReceived(byte[] data) {
        runOnUiThread(() -> {
            String received = new String(data);
            Log.d("onDataReceived", "Received Data: " + received);
        });
    }

    @Override
    public void onConnectionLost() {
        runOnUiThread(() -> {
            connectionStatus.setText("Connection lost");
            connectionStatusIcon.setImageResource(R.drawable.ic_bluetooth_disconnected);
            Toast.makeText(this, "Connection lost, attempting to reconnect...", Toast.LENGTH_SHORT).show();
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
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        if (viewId == R.id.switch1) {
            switch1(v);
        } else if (viewId == R.id.switch2) {
            switch2(v);
        } else if (viewId == R.id.tempBtnPlus) {
            tempBtnPlusPressed();
        } else if (viewId == R.id.tempBtnMinus) {
            tempBtnMinusPressed();
        } else if (viewId == R.id.humBtnPlus) {
            humBtnPlusPressed();
        } else if (viewId == R.id.humBtnMinus) {
            humBtnMinusPressed();
        } else if(viewId == R.id.lightOneBtn){
            switch1Light(v);
        } else if(viewId == R.id.lightTwoBtn){
            switch2Light(v);
        } else if(viewId == R.id.lightThreeBtn){
            switch3Light(v);
        } else if(viewId == R.id.lightFourBtn){
            switch4Light(v);
        }
    }

    @SuppressLint("NewApi")
    public void switch1(View v) {
        Log.d("switch_button", String.valueOf(v.getStateDescription()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (v.getStateDescription().toString().contains("checked")) {
                switch1.setThumbColorRes(R.color.red);
                bluetoothManager.DigitalOUT[1] &= 0xFE;

                editor.putBoolean("switch1", true);
                editor.apply();
                //Toast.makeText(getApplicationContext(), "" + v.getStateDescription(), Toast.LENGTH_SHORT).show();
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (v.getStateDescription().toString().contains("not checked")) {

                switch1.setThumbColorRes(R.color.limeGreen);
                bluetoothManager.DigitalOUT[1] |= 0x01;
                editor.putBoolean("switch1", false);
                editor.apply();
                //Toast.makeText(getApplicationContext(), "" + v.getStateDescription(), Toast.LENGTH_SHORT).show();
            }
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

    public void switch2(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (v.getStateDescription().toString().contains("checked")) {
                switch2.setThumbColorRes(R.color.red);
                bluetoothManager.DigitalOUT[1] &= 0xFD;
                editor.putBoolean("switch2", true);
                editor.apply();
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (v.getStateDescription().toString().contains("not checked")) {
                switch2.setThumbColorRes(R.color.limeGreen);
                bluetoothManager.DigitalOUT[1] |= 0x02;
                editor.putBoolean("switch2", false);
                editor.apply();
            }
        }
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

    private void updateConnectionStatus(boolean isConnected, String deviceName) {
        runOnUiThread(() -> {
            if (isConnected) {
                connectionStatusIcon.setImageResource(R.drawable.ic_bluetooth_connected);
            } else {
                connectionStatusIcon.setImageResource(R.drawable.ic_bluetooth_disconnected);
            }
        });
    }

    private void initUIComponents() {
        resetButton = findViewById(R.id.resetButton);
        settingButton = findViewById(R.id.settingButton);
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

    private void setupButtonListeners() {
        playPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentTimerMode == MODE_STOPWATCH) {
                    handleStopwatchPlayPause();
                } else {
                    handleCountTimerPlayPause();
                }
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTimer();
            }
        });

        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimerSettingsPopup();
            }
        });
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

    private void handleCountTimerPlayPause() {
        if (isTimerRunning) {
            timerPause();
        } else {
            timerStart();
        }
    }

    private void showTimerSettingsPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.popup_timer_settings, null);

        int width = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
        int height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
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

    private void startCountDownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        // Use current time or start from beginning if reset
        long startTimeMillis = (pausedCountDownTimeInSeconds > 0) ?
                pausedCountDownTimeInSeconds * 1000L :
                timeVarEdit * 1000L;

        countDownTimer = new CountDownTimer(startTimeMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (cdflag == 1 && isTimerRunning) {
                    timeVarEdit = (int) (millisUntilFinished / 1000);
                    updateTimerDisplay(method5(timeVarEdit));
                    pausedCountDownTimeInSeconds = timeVarEdit;
                }
            }

            @Override
            public void onFinish() {
                timeDone();
            }
        }.start();
    }

    private void startCountUpTimer() {
        if (countUpTimer != null) {
            countUpTimer.cancel();
            countUpTimer = null;
        }

        // Calculate the time to count up to (from current position to max)
        long timeToCount = (maxTimeInSeconds - currentTimeInSeconds) * 1000L;

        countUpTimer = new CountUpTimer(timeToCount, 1000) {
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

    private String method5(int secs) {
        int hours = secs / 3600;
        int minutes = (secs % 3600) / 60;
        int seconds = secs % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private void updateTimerDisplay(String timeText) {
        timerValue.setText(timeText);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceList.this);
        builder.setMessage("Do you want to Exit?");
        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(DeviceList.this, "i wanna stay on this", Toast.LENGTH_LONG).show();
                dialog.cancel();
            }
        });
        builder.setNeutralButton("Rate", new DialogInterface.OnClickListener() {
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
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

       
        resetAllTimerStates(); // Clean up all timers
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //The BroadcastReceiver that listens for bluetooth broadcasts
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                if (ActivityCompat.checkSelfPermission(DeviceList.this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                String deviceName = device != null ? device.getName() : "Bluetooth device";
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            }

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Toast.makeText(getApplicationContext(),""+"Device found",Toast.LENGTH_LONG).show();
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(getApplicationContext(),""+"Device Searching",Toast.LENGTH_LONG).show();
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                Toast.makeText(getApplicationContext(),""+"Device disconnected",Toast.LENGTH_LONG).show();
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                connectionStatus.setText("No bluetooth device connected");
                connectionStatus.setTextColor(ContextCompat.getColor(context, R.color.redColor));
            }
        }
    };

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

    // fast way to call Toast
    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

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

    private void Disconnect() {
        if (btSocket != null) {
            try {
                btSocket.close();
                Toast.makeText(DeviceList.this, "Bluetooth device has been disconnected", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                msg("Error");
            }
        } else {
            Toast.makeText(DeviceList.this, "No device connected", Toast.LENGTH_LONG).show();
        }
    }
}