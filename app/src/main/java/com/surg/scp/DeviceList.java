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
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
    //  private CameraKitView cameraKitView;
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
        // Start from your activity
        //startService(new Intent(this, BluetoothService.class));
        playPause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (isPlaying) {
                    pause();
                } else {
                    play();
                }
                isPlaying = !isPlaying;
            }
        });
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


        // to change humburger icon color
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
            // new ConnectBT(address, info_address).execute(); //Call the class to

            if (address != null) {
                connectToDevice(address, info_address);
            }
         /*   bluetoothManager.connect(address, info_address != null ? info_address : "", new BluetoothConnectionManager.ConnectionCallback() {
                @Override
                public void onConnectionResult(int resultCode, String message) {
                    switch (resultCode) {
                        case BluetoothConnectionManager.CONNECTION_SUCCESS:
                            // Update UI for success
                            updateConnectionStatus(true, message);
                            connectionStatus.setText("Connected");
                            Toast.makeText(DeviceList.this, message, Toast.LENGTH_SHORT).show();
                            break;

                        case BluetoothConnectionManager.PERMISSION_DENIED:
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                ActivityCompat.requestPermissions(DeviceList.this,
                                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                                        PERMISSION_REQUEST_CODE);
                            }

                        case BluetoothConnectionManager.IO_EXCEPTION:
                            updateConnectionStatus(false, null);
                            new AlertDialog.Builder(DeviceList.this)
                                    .setTitle("Connection Failed")
                                    .setMessage(message)
                                    .setPositiveButton("OK", null)
                                    .show();
                            // fall through

                        default:
                            // Show error message
                            new AlertDialog.Builder(DeviceList.this)
                                    .setTitle("Connection Failed")
                                    .setMessage(message)
                                    .setPositiveButton("OK", null)
                                    .show();
                            break;
                    }
                }

                @Override
                public void onDataReceived(byte[] data) {

                }

                @Override
                public void onConnectionLost() {

                }
            });*/

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
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
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


        timerValue = (TextView) findViewById(R.id.timerValue);

        resetButton = findViewById(R.id.resetButton);
        resetButton.setClickable(false);
        // resetButton.setTextColor(Color.parseColor("#a6a6a6"));

        startButton = findViewById(R.id.startButton);

        //stopButton = (Button) findViewById (R.id.stopButton);
        //stopButton.setClickable(false);
        //stopButton.setTextColor(Color.parseColor("#a6a6a6"));

        resetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                resetButton.setClickable(false);
                // resetButton.setTextColor(Color.parseColor("#8e8e8e"));
                timerValue.setText(String.format("%02d", 00) + ":"
                        + String.format("%02d", 00) + ":"
                        + String.format("%02d", 00));
                startTime = SystemClock.uptimeMillis();
                timeSwapBuff = 0;
            }
        });
        /*
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startButton.setClickable(false);
                startButton.setTextColor(Color.parseColor("#a6a6a6"));
                resetButton.setClickable(false);
                resetButton.setTextColor(Color.parseColor("#a6a6a6"));
                stopButton.setClickable(true);
                stopButton.setTextColor(Color.parseColor("#000000"));
                startTime = SystemClock.uptimeMillis();
                customHandler.postDelayed(updateTimerThread, 0);
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startButton.setClickable(true);
                startButton.setTextColor(Color.parseColor("#000000"));
                stopButton.setClickable(false);
                stopButton.setTextColor(Color.parseColor("#a6a6a6"));
                resetButton.setClickable(true);
                resetButton.setTextColor(Color.parseColor("#000000"));
                timeSwapBuff += timeInMilliseconds;
                customHandler.removeCallbacks(updateTimerThread);
            }
        });
        */


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


    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;

            int seconds = (int) (updatedTime / 1000);
            int minutes = seconds / 60;
            int hours = seconds / 3600;
            seconds = seconds % 60;
            //  int milliseconds = (int) (updatedTime % 1000);

            String string = "";
            string += "" + String.format("%02d", hours);
            string += ":" + String.format("%02d", minutes);
            string += ":" + String.format("%02d", seconds);
            //  string += ":" + String.format("%03d", milliseconds);

            timerValue.setText(string);
            customHandler.postDelayed(this, 0);
        }
    };


    private void sendData() {
        if (btSocket != null) {
            try {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // myLabel.setText(sendEditText.getText().toString());
                        handler.postDelayed(this, 100);
                    }
                }, 100);
                String digitNo = dataModel.getDigitNo();
                dataModel.getTypeNo();
                dataModel.getDevId();
                dataModel.getSoundType();
                // String data="$134"+display.getText().toString()+";";
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
        // String stringData = textData.rightPad(lenght, ' ').Substring(0, length);
        // String stringData = leftpad(textData,28);
        return stringData;
    }


    public void play() {

        // playPause.setClickable(false);
        // playPause.setTextColor(Color.parseColor("#a6a6a6"));
        resetButton.setClickable(false);
        // resetButton.setTextColor(Color.parseColor("#a6a6a6"));
        //stopButton.setClickable(true);
        //stopButton.setTextColor(Color.parseColor("#000000"));
        startTime = SystemClock.uptimeMillis();
        customHandler.postDelayed(updateTimerThread, 0);

        // Drawable icon= getApplicationContext().getResources().getDrawable(R.drawable.ic_pause);
        playPause.setBackgroundResource(R.drawable.ic_pause);
        //     icon.setBounds(0, 0, 0, 0); //Left,Top,Right,Bottom
        // playPause.setCompoundDrawablesWithIntrinsicBounds( null, null, icon, null);

    }

    public void pause() {


        resetButton.setClickable(true);
        // resetButton.setTextColor(Color.parseColor("#000000"));
        timeSwapBuff += timeInMilliseconds;
        customHandler.removeCallbacks(updateTimerThread);

        //  Drawable icon= getApplicationContext().getResources().getDrawable(R.drawable.ic_play);
        playPause.setBackgroundResource(R.drawable.ic_play);
        // icon.setBounds(0, 0, 0, 0); //Left,Top,Right,Bottom
        // playPause.setCompoundDrawablesWithIntrinsicBounds( null, null, icon, null);
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
        // startActivity(intent);
        if (Build.VERSION.SDK_INT > 20) {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
        //overridePendingTransition(R.anim.slide_out_bottom, R.anim.slide_in_bottom);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, retry the connection
                if (address != null) {
                    // new ConnectBT(address, info_address).execute();

                 /*   bluetoothManager.connect(address, info_address != null ? info_address : "", new BluetoothConnectionManager.ConnectionCallback() {
                        @Override
                        public void onConnectionResult(int resultCode, String message) {
                            switch (resultCode) {
                                case BluetoothConnectionManager.CONNECTION_SUCCESS:
                                    // Update UI for success
                                    updateConnectionStatus(true, message);
                                    connectionStatus.setText("Connected");
                                    Toast.makeText(DeviceList.this, message, Toast.LENGTH_SHORT).show();
                                    break;

                                case BluetoothConnectionManager.PERMISSION_DENIED:
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        ActivityCompat.requestPermissions(DeviceList.this,
                                                new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                                                PERMISSION_REQUEST_CODE);
                                    }

                                case BluetoothConnectionManager.IO_EXCEPTION:
                                    updateConnectionStatus(false, null);
                                    new AlertDialog.Builder(DeviceList.this)
                                            .setTitle("Connection Failed")
                                            .setMessage(message)
                                            .setPositiveButton("OK", null)
                                            .show();
                                    // fall through

                                default:
                                    // Show error message
                                    new AlertDialog.Builder(DeviceList.this)
                                            .setTitle("Connection Failed")
                                            .setMessage(message)
                                            .setPositiveButton("OK", null)
                                            .show();
                                    break;
                            }
                        }

                        @Override
                        public void onDataReceived(byte[] data) {

                        }

                        @Override
                        public void onConnectionLost() {

                        }
                    });
                }
            } else {
                Toast.makeText(this, "Bluetooth permission required", Toast.LENGTH_SHORT).show();
            }*/
                    if (address != null) {
                        connectToDevice(address, info_address);
                    }
                }
            }
        }
    }

    private void pairedDevicesList() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt : pairedDevices) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }


        //--------------------------------------------------------------------------------------------------------------
        Dialog dialog = new Dialog(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a paired device for connecting");

        LinearLayout parent = new LinearLayout(DeviceList.this);

        parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        parent.setOrientation(LinearLayout.VERTICAL);

        ListView modeList = new ListView(this);


        //------------------To fixed height of listView------------------------------------
        setListViewHeightBasedOnItems(modeList);
        //RelativeLayout.LayoutParams params=new RelativeLayout.LayoutParams(200, 0);

        //------------------End of fixed height of listView---------------------------------

        final ArrayAdapter modeAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        modeList.setAdapter(modeAdapter);
        modeList.setOnItemClickListener(myListClickListener);
        builder.setView(modeList);
        //  builder.show();
        dialog = builder.create();
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 600); //Controlling width and height.


        //-------------------------------------------------------------------------------------------------------------


    }


    private void pairedDevicesListOriginal() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice bt : pairedDevices) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        } else {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked

    }

    @Override
    public void onConnectionResult(int resultCode, String message) {
        runOnUiThread(() -> {
            //Toast.makeText(this,resultCode+" code "+ message, Toast.LENGTH_LONG).show();
            switch (resultCode) {

                case BluetoothConnectionManager.CONNECTION_SUCCESS:
                 //   connectionStatus.setText("Connected: " + message);
                    connectionStatusIcon.setImageResource(R.drawable.ic_bluetooth_connected);
                    Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
                    break;

                case BluetoothConnectionManager.CONNECTION_FAILED:

                    connectionStatusIcon.setImageResource(R.drawable.ic_bluetooth_disconnected);
                    Toast.makeText(this, "Bluetooth connection failed "+message, Toast.LENGTH_LONG).show();
                    break;
                case BluetoothConnectionManager.IO_EXCEPTION:
                  //  connectionStatus.setText("Connection failed");
                    connectionStatusIcon.setImageResource(R.drawable.ic_bluetooth_disconnected);
                    Toast.makeText(this, "IO_EXCEPTION "+message, Toast.LENGTH_LONG).show();
                  //  showErrorDialog("Connection Error", message);
                    break;
                case BluetoothConnectionManager.CONNECTION_LOST:
                  //  connectionStatus.setText("Connection failed");
                    connectionStatusIcon.setImageResource(R.drawable.ic_bluetooth_disconnected);
                    Toast.makeText(this, "Connection Lost "+message, Toast.LENGTH_LONG).show();
                  //  showErrorDialog("Connection Error", message);
                    break;

                case BluetoothConnectionManager.PERMISSION_DENIED:
                    connectionStatus.setText("Permission denied");
                    connectionStatusIcon.setImageResource(R.drawable.ic_bluetooth_disconnected);
                    Toast.makeText(this, "Bluetooth permission required "+message, Toast.LENGTH_LONG).show();
                    break;

                case BluetoothConnectionManager.BLUETOOTH_DISABLED:
                   // connectionStatus.setText("Bluetooth disabled");
                    connectionStatusIcon.setImageResource(R.drawable.ic_bluetooth_disconnected);
                    Toast.makeText(this, "Bluetooth Disabled "+message, Toast.LENGTH_LONG).show();
                   // showEnableBluetoothDialog();
                    break;
            }
        });
    }

    @Override
    public void onDataReceived(byte[] data) {
        runOnUiThread(() -> {
            // Handle incoming data
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


            //Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            //Make an intent to start next activity.
            //Intent i = new Intent(DeviceList.this, DeviceList.class);

            //Change the activity.
            //i.putExtra(EXTRA_ADDRESS, address); //this will be received at DataControl (class) Activity
            //startActivity(i);
            //  new ConnectBT(address,info).execute(); //Call the class to connect
            if (address != null) {
                connectToDevice(address, info);
            }
          /*  bluetoothManager.connect(address, info_address != null ? info_address : "", new BluetoothConnectionManager.ConnectionCallback() {
                @Override
                public void onConnectionResult(int resultCode, String message) {
                    switch (resultCode) {
                        case BluetoothConnectionManager.CONNECTION_SUCCESS:
                            // Update UI for success
                            updateConnectionStatus(true, message);
                            connectionStatus.setText("Connected");
                            Toast.makeText(DeviceList.this, message, Toast.LENGTH_SHORT).show();
                            break;

                        case BluetoothConnectionManager.PERMISSION_DENIED:
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                ActivityCompat.requestPermissions(DeviceList.this,
                                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                                        PERMISSION_REQUEST_CODE);
                            }

                        case BluetoothConnectionManager.IO_EXCEPTION:
                            updateConnectionStatus(false, null);
                            new AlertDialog.Builder(DeviceList.this)
                                    .setTitle("Connection Failed")
                                    .setMessage(message)
                                    .setPositiveButton("OK", null)
                                    .show();
                            // fall through

                        default:
                            // Show error message
                            new AlertDialog.Builder(DeviceList.this)
                                    .setTitle("Connection Failed")
                                    .setMessage(message)
                                    .setPositiveButton("OK", null)
                                    .show();
                            break;
                    }
                }

                @Override
                public void onDataReceived(byte[] data) {

                }

                @Override
                public void onConnectionLost() {

                }
            });*/
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
        }else if (id == R.id.action_disconnect) {
            bluetoothManager.disconnect();
        }

        // Close navigation drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_list, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            int id = item.getItemId();


            /*******************************************************************************
             * Navigation Menu Item
             *******************************************************************************/


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
        }

/* else if (id == R.id.action_about) {
    Intent intent = new Intent(this, AboutActivity.class);
    startActivity(intent);
    return true;
} */
        else {
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
            // e.toString();
        }

    }


    public void exitApplication() {
        final AlertDialog.Builder adb = new AlertDialog.Builder(this);
        // adb.setView(Integer.parseInt("Delete Folder"));
        // adb.setTitle("Exit");
        adb.setMessage("Are you sure you want to exit application?");
        // adb.setIcon(android.R.drawable.ic_dialog_alert);
        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(DeviceList.this, "Cancel",
                        Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                //finish();
            }
        });
        adb.show();

    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        // cameraKitView.onStop();
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
        }else if(viewId == R.id.lightOneBtn){
          switch1Light(v);
        }else if(viewId == R.id.lightTwoBtn){
            switch2Light(v);
        }else if(viewId == R.id.lightThreeBtn){
            switch3Light(v);
        }else if(viewId == R.id.lightFourBtn){
            switch4Light(v);
        }
        // No default case needed since we don't need to handle other cases
    }


    @SuppressLint("NewApi")
    public void switch1(View v) {
        Log.d("switch_button", String.valueOf(v.getStateDescription()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (v.getStateDescription().toString().contains("ticked")) {
                switch1.setThumbColorRes(R.color.red);
                bluetoothManager.DigitalOUT[1] &= 0xFE;

                editor.putBoolean("switch1", true);
                editor.apply();
                //Toast.makeText(getApplicationContext(), "" + v.getStateDescription(), Toast.LENGTH_SHORT).show();
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (v.getStateDescription().toString().contains("not ticked")) {

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
                // turn OFF
                lightOneBtn.setImageResource(R.drawable.ic_bulb_off);

                bluetoothManager.DigitalOUT[1] &= 0xFB;
                isLightOneOn = false;
                editor.putBoolean("light1", false);
            } else {
                // turn ON
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
            if (v.getStateDescription().toString().contains("ticked")) {
                switch2.setThumbColorRes(R.color.red);
                bluetoothManager.DigitalOUT[1] &= 0xFD;

                editor.putBoolean("switch2", true);
                editor.apply();
                // Toast.makeText(getApplicationContext(), "" + v.getStateDescription(), Toast.LENGTH_SHORT).show();
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (v.getStateDescription().toString().contains("not ticked")) {

                switch2.setThumbColorRes(R.color.limeGreen);
                bluetoothManager.DigitalOUT[1] |= 0x02;

                editor.putBoolean("switch2", false);
                editor.apply();
                //Toast.makeText(getApplicationContext(), "" + v.getStateDescription(), Toast.LENGTH_SHORT).show();
            }
        }

    }


    private void restoreStates() {
        // restore switches
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

        // restore bulbs
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
                //connectionStatus.setText(deviceName + " connected");
                //connectionStatus.setTextColor(ContextCompat.getColor(DeviceList.this, R.color.limeGreen));
            } else {
                connectionStatusIcon.setImageResource(R.drawable.ic_bluetooth_disconnected);
                //connectionStatus.setText("No device connected");
                // connectionStatus.setTextColor(ContextCompat.getColor(DeviceList.this, R.color.redColor));
            }
        });
    }





  /*  public class BluetoothConnectionManager {
        private static final String TAG = "BluetoothConnection";
        private static final int CONNECTION_TIMEOUT_MS = 10000; // 10 seconds

        // Connection status constants
        public static final int CONNECTION_SUCCESS = 0;
        public static final int CONNECTION_FAILED = 1;
        public static final int PERMISSION_DENIED = 2;
        public static final int SECURITY_EXCEPTION = 3;
        public static final int IO_EXCEPTION = 4;

        private final Context context;
        private final Handler mainHandler;
        private final ExecutorService executorService;
        private BluetoothSocket btSocket;
        private InputStream mmInputStream;
        private OutputStream mmOutputStream;
        private boolean isBtConnected = false;

        public interface ConnectionCallback {
            void onConnectionResult(int resultCode, String message);
        }

        public BluetoothConnectionManager(Context context) {
            this.context = context.getApplicationContext();
            this.mainHandler = new Handler(Looper.getMainLooper());
            this.executorService = Executors.newSingleThreadExecutor();
        }

        // Add this missing method
        private void closeSocket(BluetoothSocket socket) {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing socket", e);
            }
        }



        public void connect(String deviceAddress, String deviceInfo, ConnectionCallback callback) {
            executorService.execute(() -> {

                mainHandler.post(() -> {
                    connectionStatusIcon.setImageResource(R.drawable.ic_bluetooth_searching);
                   // connectionStatus.setText("Connecting...");
                    connectionStatus.setTextColor(ContextCompat.getColor(context, R.color.blueColor));
                });


                if (isBtConnected && btSocket != null &&
                        btSocket.getRemoteDevice().getAddress().equals(deviceAddress)) {
                    notifyResult(CONNECTION_FAILED, "Already connected to this device", callback);
                    return;
                }
                // Close existing connection if any
                disconnect();
                if (deviceAddress.isEmpty()) {

                    return;
                } else {


                }
                Log.d("Device_adress 123", deviceAddress);
                // Check permissions first
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        ContextCompat.checkSelfPermission(context,
                                Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    notifyResult(PERMISSION_DENIED, "Bluetooth permission required", callback);
                    return;
                }

                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
                    notifyResult(CONNECTION_FAILED, "Bluetooth is not available", callback);
                    return;
                }

                try {
                    // Get the remote device
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);

                    // Cancel discovery
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (ContextCompat.checkSelfPermission(context,
                                    Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                                bluetoothAdapter.cancelDiscovery();
                            }
                        } else {
                            bluetoothAdapter.cancelDiscovery();
                        }
                    } catch (SecurityException e) {
                        Log.w(TAG, "Couldn't cancel discovery", e);
                    }

                    // Create socket (try standard method first, then fallback)
                    BluetoothSocket socket = null;
                    try {

                        // Standard method
                        // socket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                    } catch (Exception e) {
                        Log.w(TAG, "Standard connection failed, trying fallback", e);
                        try {
                            // Fallback method
                            Method m = device.getClass().getMethod("createInsecureRfcommSocket", int.class);
                            socket = (BluetoothSocket) m.invoke(device, 1);
                        } catch (Exception ex) {
                            notifyResult(IO_EXCEPTION, "Failed to create connection socket", callback);
                            return;
                        }
                    }

                    // Connect with timeout
                    try {
                        UUID[] possibleUuids = {
                                UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"), // Standard SPP
                                UUID.fromString("00001108-0000-1000-8000-00805F9B34FB"), // Headset (HSP)
                                UUID.fromString("00001112-0000-1000-8000-00805F9B34FB"), // Generic (HFP)
                                UUID.fromString("0000111E-0000-1000-8000-00805F9B34FB")  // Hands-Free (HFP)
                        };
                        for (UUID uuid : possibleUuids) {
                            try {
                                socket = device.createInsecureRfcommSocketToServiceRecord(uuid);
                                socket.connect();
                                // Success! Proceed with connection
                                break;
                            } catch (IOException e) {
                                Log.w(TAG, "Failed with UUID: " + uuid, e);
                            }
                        }
                        btSocket = socket;
                        mmInputStream = socket.getInputStream();
                        mmOutputStream = socket.getOutputStream();

                        // Verify streams
                        if (mmInputStream == null || mmOutputStream == null) {
                            throw new IOException("Failed to establish streams");
                        }

                        isBtConnected = true;
                        startKeepAlive();
                        startReconnectionThread(deviceAddress,deviceInfo);


                        notifyResult(CONNECTION_SUCCESS,
                                "Connected to " + (deviceInfo != null ? deviceInfo.replace(deviceAddress, "") : device.getName()),
                                callback);

                        // Start listening for incoming data
                        startListening();

                    } catch (IOException e) {
                        closeSocket(socket); // Use the method we just added
                        notifyResult(IO_EXCEPTION, getConnectionErrorMessage(e), callback);
                    }

                } catch (SecurityException e) {
                    notifyResult(SECURITY_EXCEPTION, "Bluetooth permission denied", callback);
                } catch (IllegalArgumentException e) {
                    notifyResult(CONNECTION_FAILED, "Invalid device address: " + deviceAddress, callback);
                } catch (Exception e) {
                    Log.d("Unexpected error", e.getMessage() + " " + deviceAddress + " " + deviceInfo);
                    notifyResult(CONNECTION_FAILED, "Unexpected error: " + e.getMessage(), callback);
                }
            });
        }

        private void notifyResult(int resultCode, String message, ConnectionCallback callback) {
            mainHandler.post(() -> {
                if (callback != null) {
                    callback.onConnectionResult(resultCode, message);
                }
            });
        }

        private String getConnectionErrorMessage(IOException e) {
            if (e.getMessage().contains("Device not found")) {
                return "Device not found. Please:\n" +
                        "1. Ensure device is powered on\n" +
                        "2. Put device in pairing mode\n" +
                        "3. Keep device nearby";
            } else if (e.getMessage().contains("Connection refused")) {
                return "Connection refused. Please:\n" +
                        "1. Verify this is an SPP Bluetooth device\n" +
                        "2. Check device is not connected to another app\n" +
                        "3. Restart the Bluetooth device";
            } else {
                return "Connection failed. Please:\n" +
                        "1. Check device power and proximity\n" +
                        "2. Verify Bluetooth is enabled\n" +
                        "3. Try again later";
            }
        }

        // Modify your startListening() method
        private void startListening() {
            executorService.execute(() -> {
                byte[] buffer = new byte[1024];

                while (isBtConnected) {
                    try {
                        int bytes = mmInputStream.read(buffer);
                        if (bytes == -1) {
                            throw new IOException("Stream ended");
                        }

                        // Process data...

                    } catch (IOException e) {
                        Log.e(TAG, "Connection lost: " + e.getMessage());
                        mainHandler.post(() -> {
                            if (isBtConnected) {
                                disconnect();
                                // Optionally trigger reconnection here
                            }
                        });
                        break;
                    }
                }
            });
        }

        public void disconnect() {
            executorService.execute(() -> {
                isBtConnected = false;

                if (mmInputStream != null) {
                    try {
                        mmInputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing input stream", e);
                    }
                    mmInputStream = null;
                }

                if (mmOutputStream != null) {
                    try {
                        mmOutputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Error closing output stream", e);
                    }
                    mmOutputStream = null;
                }

                if (btSocket != null) {
                    closeSocket(btSocket); // Use our method
                    btSocket = null;
                }
            });
        }

        public boolean isConnected() {
            return isBtConnected;
        }

        public void sendData(String data) {
            if (!isBtConnected || mmOutputStream == null) {
                Log.w(TAG, "Cannot send data - not connected");
                return;
            }

            executorService.execute(() -> {
                try {
                    mmOutputStream.write(data.getBytes());
                } catch (IOException e) {
                    Log.e(TAG, "Error sending data", e);
                    mainHandler.post(() -> disconnect());
                }
            });
        }

        public void shutdown() {
            disconnect();
            executorService.shutdown();
        }


        // Add this to your BluetoothConnectionManager class
        private final Runnable keepAliveRunnable = new Runnable() {
            @Override
            public void run() {
                if (isBtConnected && btSocket != null) {
                    try {
                        // Send a small keep-alive packet periodically
                        mmOutputStream.write(0); // Null byte
                    } catch (IOException e) {
                        disconnect();
                    }
                    // Repeat every 5 seconds
                    mainHandler.postDelayed(this, 5000);
                }
            }
        };

        // Start this when connection is established
        private void startKeepAlive() {
            mainHandler.post(keepAliveRunnable);
        }

        // Stop when disconnecting
        private void stopKeepAlive() {
            mainHandler.removeCallbacks(keepAliveRunnable);
        }


        // In your BluetoothConnectionManager
        private void startReconnectionThread(String deviceAddress, String deviceInfo) {
            executorService.execute(() -> {
                while (true) {
                    try {
                        Thread.sleep(5000); // Check every 5 seconds

                        if (!isBtConnected) {
                            Log.d(TAG, "Attempting reconnection...");
                            connect(deviceAddress, deviceInfo, new ConnectionCallback() {
                                @Override
                                public void onConnectionResult(int resultCode, String message) {
                                    switch (resultCode) {
                                        case BluetoothConnectionManager.CONNECTION_SUCCESS:
                                            // Update UI for success
                                            updateConnectionStatus(true, message);
                                            connectionStatus.setText("Connected");
                                            Toast.makeText(DeviceList.this, message, Toast.LENGTH_SHORT).show();
                                            break;

                                        case BluetoothConnectionManager.PERMISSION_DENIED:
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                                ActivityCompat.requestPermissions(DeviceList.this,
                                                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                                                        PERMISSION_REQUEST_CODE);
                                            }

                                        case BluetoothConnectionManager.IO_EXCEPTION:
                                            updateConnectionStatus(false, null);
                                            new AlertDialog.Builder(DeviceList.this)
                                                    .setTitle("Connection Failed")
                                                    .setMessage(message)
                                                    .setPositiveButton("OK", null)
                                                    .show();
                                            // fall through

                                        default:
                                            // Show error message
                                            new AlertDialog.Builder(DeviceList.this)
                                                    .setTitle("Connection Failed")
                                                    .setMessage(message)
                                                    .setPositiveButton("OK", null)
                                                    .show();
                                            break;
                                    }
                                }
                            });
                        }
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            });
        }
    }*/

    // Add to your DeviceList activity
    private void disableBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);

            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
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

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);

            // Set list height.
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
        if (btSocket != null) //If the btSocket is busy
        {
            try {
                btSocket.close(); //close connection
                Toast.makeText(DeviceList.this, "Bluetooth device has been disconnected", Toast.LENGTH_LONG).show();
                // getSupportActionBar().setTitle(R.string.app_name);
            } catch (IOException e) {
                msg("Error");
            }
        } else {
            Toast.makeText(DeviceList.this, "No device connected", Toast.LENGTH_LONG).show();
        }
        //    finish(); //return to the first layout

    }


    @Override
    public void onBackPressed() {
// TODO Auto-generated method stub
        AlertDialog.Builder builder = new AlertDialog.Builder(DeviceList.this);
        // builder.setCancelable(false);
        builder.setTitle("Rate Us if u like this");
        builder.setMessage("Do you want to Exit?");
        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Toast.makeText(DeviceList.this, "Yes i wanna exit", Toast.LENGTH_LONG).show();

                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Toast.makeText(DeviceList.this, "i wanna stay on this", Toast.LENGTH_LONG).show();
                dialog.cancel();

            }
        });
        builder.setNeutralButton("Rate", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                } catch (ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                }

            }
        });
        AlertDialog alert = builder.create();
        alert.show();
        //super.onBackPressed();
    }


    // Fetch the stored data in onResume()
    // Because this is what will be called
    // when the app opens again
    @Override
    protected void onResume() {
        super.onResume();


    }

    // Store the data in the SharedPreference
    // in the onPause() method
    // When the user closes the application
    // onPause() will be called
    // and data will be stored
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                String deviceName = device != null ? device.getName() : "Bluetooth device";
                //updateConnectionStatus(true, deviceName);
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //updateConnectionStatus(false, null);
            }

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Toast.makeText(getApplicationContext(),""+"Device found",Toast.LENGTH_LONG).show();
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {

                //Toast.makeText(getApplicationContext(),""+"Device connected",Toast.LENGTH_LONG).show();
               // connectionStatus.setText(name.trim()+" device connected");
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //Done searching
                Toast.makeText(getApplicationContext(),""+"Device Searching",Toast.LENGTH_LONG).show();
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
                //Device is about to disconnect
                Toast.makeText(getApplicationContext(),""+"Device disconnected",Toast.LENGTH_LONG).show();
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //Device has disconnected

               // Toast.makeText(getApplicationContext(),name.trim()+" device disconnected",Toast.LENGTH_LONG).show();
                connectionStatus.setText("No bluetooth device connected");
                connectionStatus.setTextColor(ContextCompat.getColor(context, R.color.redColor));
            }
        }
    };

}



