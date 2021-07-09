package com.surg.scp;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.os.StrictMode;
import android.os.SystemClock;
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
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.exifinterface.media.ExifInterface;

import com.google.android.gms.instantapps.InstantApps;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.kyleduo.switchbutton.SwitchButton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import hearsilent.discreteslider.Dash;
import hearsilent.discreteslider.DiscreteSlider;
import hearsilent.discreteslider.Dot;
import hearsilent.discreteslider.libs.Utils;

import static android.content.ContentValues.TAG;


public class DeviceList extends AppCompatActivity implements  View.OnClickListener  , NavigationView.OnNavigationItemSelectedListener
{
    private static final String MY_PREFS_NAME = "MyTxtFile";

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

    boolean listViewFlag=true;
   
    
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

    final int DisplayValueMin = 0;
    final int DisplayValueMax1 = 99;
    final int DisplayValueMax2 = 999;
    final int DisplayValueMax3 = 9999;
    //+++++++++++++++++++++++++ Auto Repeat increment and decrement ++++++++++++++++++++++++++

    Button On, Off, Discnt, Abt;

    //widgets
    Button btnPaired,scanDevices;
    ListView devicelist;
    //Bluetooth
    private BluetoothAdapter myBluetooth = null;
    private Set<BluetoothDevice> pairedDevices;
    public static String EXTRA_ADDRESS = "device_address";
    public static String EXTRA_INFO = "device_info";

    //screenshott
    private final static String[] requestWritePermission =
            { Manifest.permission.WRITE_EXTERNAL_STORAGE };
    private ImageView imageView;
    private Bitmap bitmap;

    private String str_celcius;
    private String str_fahrenheit;
    private ImageView bmpView;
    //=================================For temperature limit count==================================


    private String double_str_fahrenheit;


    private ImageView iv_your_image;
    private String str_cel,str_fah;

    //=========================================End temperature limit count==========================
    String[] permissions = new String[]{

            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };
    private    FrameLayout   layout;

    private TextView one, two, three, four, five, six, seven, eight, nine, zero, div, multi, sub, plus, dot, equals, display, clear;
    private ImageButton backDelete;

    /***************************************************************************************
     * Navigation Drawer Layout
     *
     ***************************************************************************************/
    public DrawerLayout drawerLayout;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView mNavigationView;
    private ListView idList,sndList,digitList,typeList;
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
    private ImageButton minusButton, plusButton;
    ImageButton[] arrayOfControlButtons;
    private boolean success =  false;
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
    private Button playPause;
    private Button stopButton;
    private Button resetButton;
    private DiscreteSlider mSlider1, mSlider2, mSlider3, mSlider4;
    private SwitchButton switch1;


    /***************************************************************************************
    * End Stop Watch
    ****************************************************************************************/

    //screenshot
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scp);
//----------------------------Grant storage permission--------------------------------------------------
        setAnimation();

        /***************************************************************************************
         *   Play and pause in only one button - Android
         ****************************************************************************************/
        playPause =  findViewById(R.id.startButton);

        playPause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (isPlaying) {
                    pause();
                }else{
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
        dbHandler.getQmsUtilityById("1",dataModel);
        /*********************************************************************************
         * Initialize Database
         *
         * *******************************************************************************/
        /************************************************************************************
        *
         * Discrete Slider
        *
        *************************************************************************************/

       // mSlider1 = findViewById(R.id.discreteSlider1);
       // mSlider2 = findViewById(R.id.discreteSlider2);
       // mSlider3 = findViewById(R.id.discreteSlider3);
       // mSlider4 = findViewById(R.id.discreteSlider4);
        //setUpView(mSlider1);
      //  setUpView(mSlider2);
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
         switch1 =findViewById(R.id.switch1);

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
        this.registerReceiver(mReceiver, filter);
        //------------------------------------------------------------------------------------------------
        //=========================Adding Toolbar in android layout=======================================
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // getSupportActionBar().setDisplayShowHomeEnabled(true);
        //=========================Toolbar End============================================================
        Drawable drawable = myToolbar.getOverflowIcon();
        if(drawable != null) {
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
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close){

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


        mNavigationView =  findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
        // After giving this Navigation menu Item Icon becomes colorful
        mNavigationView.setItemIconTintList(null); // <-- HERE add this code for icon color
        // to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
        if(address != null) {
            new ConnectBT(address, info_address).execute(); //Call the class to
        }
        //-------------------------------------To Receive device address from background==================
        //====================================Camera======================================================

        boolean isInstantApp = InstantApps.getPackageManagerCompat(this).isInstantApp();
        Log.d(LOG_TAG, "are we instant?" + isInstantApp);



        //if the device has bluetooth
        myBluetooth = BluetoothAdapter.getDefaultAdapter();

        if(myBluetooth == null)
        {
            //Show a Mensag. That the device has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();

            //finish apk
            finish();
        }
        else if(!myBluetooth.isEnabled())
        {
                //Ask to the user turn the bluetooth on
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon,1);
        }





        //Camera screenshot
        final boolean hasWritePermission = RuntimePermissionUtil.checkPermissonGranted(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        imageView = findViewById(R.id.imageView);


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
        final Button startButton;



        timerValue = (TextView) findViewById (R.id.timerValue);

        resetButton = (Button) findViewById (R.id.resetButton);
        resetButton.setClickable(false);
        resetButton.setTextColor(Color.parseColor("#a6a6a6"));

        startButton = (Button)  findViewById (R.id.startButton);

          //stopButton = (Button) findViewById (R.id.stopButton);
          //stopButton.setClickable(false);
          //stopButton.setTextColor(Color.parseColor("#a6a6a6"));

        resetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                resetButton.setClickable(false);
                resetButton.setTextColor(Color.parseColor("#8e8e8e"));
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
    }



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



    private void sendData()
    {
        if (btSocket!=null)
        {
            try
            {

                final Handler handler=new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        // myLabel.setText(sendEditText.getText().toString());
                        handler.postDelayed(this,100);
                    }
                },100);
                String digitNo =  dataModel.getDigitNo();
                dataModel.getTypeNo();
                dataModel.getDevId();
                dataModel.getSoundType();
                // String data="$134"+display.getText().toString()+";";
                String displayText = display.getText().toString();
                if(displayText.length()==2){

                    displayText = "0"+displayText;
                    digitNo = "3";
                }
                String displayData  = fixedLengthString(displayText, 4);

                String data="$"+dataModel.getDevId()+digitNo+dataModel.getSound_id()+displayData+";";
                btSocket.getOutputStream().write(data.getBytes());
                Log.d("Display_Digit",data);
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private String fixedLengthString(String textData , int length)
    {
        String stringData = null;
        // String stringData = textData.rightPad(lenght, ' ').Substring(0, length);
        // String stringData = leftpad(textData,28);


        return stringData;
    }


    public void play(){

       // playPause.setClickable(false);
       // playPause.setTextColor(Color.parseColor("#a6a6a6"));
       resetButton.setClickable(false);
       resetButton.setTextColor(Color.parseColor("#a6a6a6"));
        //stopButton.setClickable(true);
        //stopButton.setTextColor(Color.parseColor("#000000"));
        startTime = SystemClock.uptimeMillis();
        customHandler.postDelayed(updateTimerThread, 0);

       // Drawable icon= getApplicationContext().getResources().getDrawable(R.drawable.ic_pause);
        playPause.setBackgroundResource(R.drawable.ic_pause);
   //     icon.setBounds(0, 0, 0, 0); //Left,Top,Right,Bottom
       // playPause.setCompoundDrawablesWithIntrinsicBounds( null, null, icon, null);

    }

    public void pause(){


      resetButton.setClickable(true);
      resetButton.setTextColor(Color.parseColor("#000000"));
        timeSwapBuff += timeInMilliseconds;
        customHandler.removeCallbacks(updateTimerThread);

      //  Drawable icon= getApplicationContext().getResources().getDrawable(R.drawable.ic_play);
        playPause.setBackgroundResource(R.drawable.ic_play);
       // icon.setBounds(0, 0, 0, 0); //Left,Top,Right,Bottom
       // playPause.setCompoundDrawablesWithIntrinsicBounds( null, null, icon, null);
    }


    public void setAnimation()
    {
        if(Build.VERSION.SDK_INT>20) {
            Slide slide = new Slide();
            slide.setSlideEdge(Gravity.LEFT);
            slide.setDuration(400);
            slide.setInterpolator(new AccelerateDecelerateInterpolator());
            getWindow().setExitTransition(slide);
            getWindow().setEnterTransition(slide);
        }
    }



    private void setUpView(DiscreteSlider mSlider) {
        mSlider.setTrackWidth(Utils.convertDpToPixel(4, this));
        mSlider.setTrackColor(0xFFD81B60);
        mSlider.setInactiveTrackColor(0x3DD81B60);

        mSlider.setThumbRadius(Utils.convertDpToPixel(6, this));
        mSlider.setThumbColor(0xFFD81B60);
        mSlider.setThumbPressedColor(0x1FD81B60);

        mSlider.setTickMarkColor(0x3DFFFFFF);
        mSlider.setTickMarkInactiveColor(0x1FD81B60);
        mSlider.setTickMarkPatterns(
                Arrays.asList(new Dot(), new Dash(Utils.convertDpToPixel(1, this))));

        mSlider.setValueLabelTextColor(Color.WHITE);
        mSlider.setValueLabelTextSize(Utils.convertSpToPixel(16, this));
        mSlider.setValueLabelFormatter(new DiscreteSlider.ValueLabelFormatter() {

            @Override
            public String getLabel(int input) {
                return Integer.toString(input);
            }
        });

        mSlider.setCount(101);
        mSlider.setMode(DiscreteSlider.MODE_NORMAL);
        mSlider.setProgressOffset(0);
        mSlider.setMinProgress(0);
        mSlider.setTickMarkStep(10);





        mSlider.setOnValueChangedListener(new DiscreteSlider.OnValueChangedListener() {

            @Override
            public void onValueChanged(int progress, boolean fromUser) {
                super.onValueChanged(progress, fromUser);
                Log.i("DiscreteSlider", "Progress: " + progress + ", fromUser: " + fromUser);
            }

            @Override
            public void onValueChanged(int minProgress, int maxProgress, boolean fromUser) {
                super.onValueChanged(minProgress, maxProgress, fromUser);
                Log.i("DiscreteSlider",
                        "MinProgress: " + minProgress + ", MaxProgress: " + maxProgress +
                                ", fromUser: " + fromUser);
            }
        });

        mSlider.setClickable(true);
    }

    private void ScanDevicesList(){

        Intent intent = new Intent(this, ScanActivity.class);
        // startActivity(intent);
        if(Build.VERSION.SDK_INT>20)
        {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
            startActivity(intent,options.toBundle());
        }
        else {
            startActivity(intent);
        }
        //overridePendingTransition(R.anim.slide_out_bottom, R.anim.slide_in_bottom);
    }

    private void pairedDevicesList()
    {
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices)
            {
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        }
        else
        {
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
        //modeList.setLayoutParams(params);
        //modeList.requestLayout();
        /******************************************************************************************************************
        *
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.height = 20;
        modeList.setLayoutParams(params);
        modeList.requestLayout();

        *
        * */
       // ViewGroup.LayoutParams listViewParams = (ViewGroup.LayoutParams)modeList.getLayoutParams();
        //listViewParams.height = 20;
       // modeList.smoothScrollToPosition(3);
/*
        ListAdapter listadp = modeList.getAdapter();
        if (listadp != null) {
            int totalHeight = 0;
            for (int i = 0; i < listadp.getCount(); i++) {
                View listItem = listadp.getView(i, null, modeList);
                listItem.measure(0, 0);
                totalHeight += listItem.getMeasuredHeight();
            }
            ViewGroup.LayoutParams params = modeList.getLayoutParams();
            params.height = totalHeight + (modeList.getDividerHeight() * (listadp.getCount() - 1));
            modeList.setLayoutParams(params);
            modeList.requestLayout();
        }
*/
        //------------------End of fixed height of listView---------------------------------

        final ArrayAdapter modeAdapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        modeList.setAdapter(modeAdapter);
        modeList.setOnItemClickListener(myListClickListener);
        builder.setView(modeList);
      //  builder.show();
        dialog = builder.create();
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, 600); //Controlling width and height.


        //-------------------------------------------------------------------------------------------------------------



    }




    private void pairedDevicesListOriginal()
    {
        pairedDevices = myBluetooth.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices)
            {
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);
        devicelist.setOnItemClickListener(myListClickListener); //Method called when the device from the list is clicked



    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick (AdapterView<?> av, View v, int arg2, long arg3)
        {



            //Get the device MAC address, the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            //Make an intent to start next activity.
            //Intent i = new Intent(DeviceList.this, DeviceList.class);

            //Change the activity.
            //i.putExtra(EXTRA_ADDRESS, address); //this will be received at DataControl (class) Activity
            //startActivity(i);
            new ConnectBT(address,info).execute(); //Call the class to connect
        }
    };


    @Override
    public boolean onNavigationItemSelected(@NonNull  MenuItem item) {




       switch (item.getItemId()) {

            case R.id.nav_exit:
                exitApplication();
                break;
           case R.id.action_share:
               shareApp();
               break;
           case R.id.action_about:
               Intent intent = new Intent(this, AboutActivity.class);
               startActivity(intent);
               break;
           case R.id.action_searchList:
               ScanDevicesList();
               break;
           case R.id.action_pairedList:
               pairedDevicesList();
               break;

        }
        //close navigation drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_device_list, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        /*******************************************************************************
        *
        * Navigation Menu Item
        * ******************************************************************************/
       if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
           //Toast.makeText(getApplicationContext(), "nav_exit"+item.getItemId(), Toast.LENGTH_LONG).show();
          /*  int n_id= item.getItemId();
            switch (n_id) {
                case R.id.nav_id:
                    pairedDevicesList();
                    return true;
                case R.id.nav_exit:
                    Toast.makeText(getApplicationContext(), "nav_exit", Toast.LENGTH_LONG).show();
                    return true;
                case R.id.nav_digits:
                    finish();
                    return true;
                case R.id.nav_sound:
                    Toast.makeText(getApplicationContext(), "nav_sound", Toast.LENGTH_LONG).show();
                    return true;
                case R.id.nav_type:
                    Toast.makeText(getApplicationContext(), "nav_type", Toast.LENGTH_LONG).show();
                    return true;
                default:
                    return super.onOptionsItemSelected(item);
            }*/
            return  true;
        }

        switch (id) {


            case R.id.action_disconnect:
                Disconnect();

                return true;
            case R.id.action_searchList:
                ScanDevicesList();
                return true;
            case R.id.action_pairedList:
                pairedDevicesList();
                return true;
            /*case R.id.action_about:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;*/

            default:
                return super.onOptionsItemSelected(item);
        }


    }



    public void shareApp(){

        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My application name");
            String shareMessage= "\nLet me recommend you this application\n\n";
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch(Exception e) {
            //e.toString();
        }

    }



    public void exitApplication(){
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
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)&&shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)&&shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AlertDialog confirmationDialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.request_permission)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},
                                    REQUEST_CAMERA_PERMISSION);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                    .create();
            confirmationDialog.show();
        } else {
            requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestStoragePermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)&&shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
            AlertDialog confirmationDialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.request_permission)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},
                                    REQUEST_STORAGE_PERMISSION);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                    .create();
            confirmationDialog.show();
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        }
    }




    @Override
    public void onClick(View v) {


    }





    //=================================To Connect Bluetooth Device====================================
    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {

        private boolean ConnectSuccess = true; //if it's here, it's almost connected
        public ConnectBT(String address,String info) {
            super();
            addressBLE=address;
            infoBLE=info;
            //Do stuff
        }


        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(DeviceList.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {

            if(btSocket!=null){
                try {
                    btSocket.close();
                    btSocket=null;
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(addressBLE);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                    mmOutputStream = btSocket.getOutputStream();
                    mmInputStream = btSocket.getInputStream();

                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);


            if (!ConnectSuccess)
            {
                Intent intent = getIntent();
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                // finish();
              //  startActivity(intent);
               // Disconnect();
                //getSupportActionBar().setTitle(R.string.app_name);
            }
            else
            {

                isBtConnected = true;
                address = infoBLE.substring(infoBLE.length() - 17);
                name = infoBLE.replace(address, "");
                msg("Device connected.");
                // getSupportActionBar().setTitle(name);
                connectionStatus.setText("Device connected");
                connectionStatus.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.limeGreen));



/*
                try {
                    receiveData();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
            progress.dismiss();

            //beginListenForData();

            new Thread(new Runnable() {
                public void run(){
                    try {
                     //   receiveData();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //beginListenForData();
                }
            }).start();

        }
    }





    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
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

    private void Disconnect()
    {
     if (btSocket!=null) //If the btSocket is busy
         {
            try
            {
                btSocket.close(); //close connection
                Toast.makeText(DeviceList.this, "Bluetooth device has been disconnected", Toast.LENGTH_LONG).show();
                // getSupportActionBar().setTitle(R.string.app_name);
            }
            catch (IOException e)
            { msg("Error");}
         }else{
         Toast.makeText(DeviceList.this, "No device connected", Toast.LENGTH_LONG).show();
     }
     //    finish(); //return to the first layout

    }



    @Override
    public void onBackPressed() {
// TODO Auto-generated method stub
        AlertDialog.Builder builder=new AlertDialog.Builder(DeviceList.this);
        // builder.setCancelable(false);
        builder.setTitle("Rate Us if u like this");
        builder.setMessage("Do you want to Exit?");
        builder.setPositiveButton("yes",new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Toast.makeText(DeviceList.this, "Yes i wanna exit", Toast.LENGTH_LONG).show();

                finish();
            }
        });
        builder.setNegativeButton("No",new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Toast.makeText(DeviceList.this, "i wanna stay on this", Toast.LENGTH_LONG).show();
                dialog.cancel();

            }
        });
        builder.setNeutralButton("Rate",new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub

                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
                }

            }
        });
        AlertDialog alert=builder.create();
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



