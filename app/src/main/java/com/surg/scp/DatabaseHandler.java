package com.surg.scp;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    // Database Name
    private static final String DATABASE_NAME = "DisplayTokenDB";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Table Name
    private static final String TABLE_DISPLAY_TOKEN = "displaytoken";

    // Table Columns Name
    private static final String KEY_ID = "id";
    private static final String KEY_DEVICE_ID = "devId";
    private static final String KEY_NO_OF_DIGIT = "digitNo";
    private static final String KEY_SOUND = "soundType";
    private static final String KEY_SOUND_ID = "snd_id";
    private static final String KEY_TYPE = "typeNo";


    Context context;

    private final ArrayList<String> qms_list = new ArrayList<>();

    public DatabaseHandler(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;

    }

    public DatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_DISPLAY_TOKEN + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_DEVICE_ID + " TEXT,"
                + KEY_NO_OF_DIGIT + " TEXT ,"+ KEY_SOUND + " TEXT ,"+ KEY_TYPE + " TEXT,"+ KEY_SOUND_ID + " TEXT"+")";
        db.execSQL(CREATE_CONTACTS_TABLE);
        db.execSQL("INSERT INTO " + TABLE_DISPLAY_TOKEN+ "("+KEY_DEVICE_ID+", "+KEY_NO_OF_DIGIT+", "+KEY_SOUND+", "+KEY_TYPE+","+KEY_SOUND_ID+" ) VALUES ('1', 2, 'English',4,2)");
        //db.execSQL("INSERT INTO "+TABLE_DISPLAY_TOKEN+"("+KEY_DEVICE_ID+","+KEY_NO_OF_DIGIT+","+KEY_SOUND,KEY_TYPE+")"+" VALUES(?,?,?,?)", new Object[]{"1", "2","3","4"}");");
        //  db.execSQL("INSERT INTO TABLE_DISPLAY_TOKEN(name, amount) VALUES(?, ?)", new Object[]{"Jerry", moneyOfJerry});


        /*db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_DISPLAY_TOKEN + " ("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + KEY_DEVICE_ID + " TEXT DEFAULT '1', "
                + KEY_NO_OF_DIGIT + " TEXT DEFAULT '1', "
                + KEY_TYPE + " TEXT DEFAULT '1', "
                + KEY_SOUND + " TEXT DEFAULT 'English');");*/
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older Table if already Exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DISPLAY_TOKEN);
        // Create tables again
        onCreate(db);
    }

    // Adding new contact
    public void Add_displayToken() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_DEVICE_ID, "1");
        values.put(KEY_NO_OF_DIGIT, "2");
        values.put(KEY_SOUND, "English");

        values.put(KEY_TYPE, "1");


        // Inserting Row
        long rowInserted = db.insert(TABLE_DISPLAY_TOKEN, null, values);

        if(rowInserted != -1)
            Toast.makeText(context, "New row added, row id: " + rowInserted, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(context, "Something wrong", Toast.LENGTH_SHORT).show();
        db.close(); // Close Database Connection
    }


    // Adding new contact
    public void Add_QmsUtility(DataModel dataModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_DEVICE_ID, dataModel.getDevId()); // Name
        values.put(KEY_NO_OF_DIGIT, dataModel.getDigitNo()); //  Email
        values.put(KEY_SOUND, dataModel.getSoundType()); // Name
        values.put(KEY_TYPE, dataModel.getTypeNo()); // Name



        // Inserting Row
        long rowInserted = db.insert(TABLE_DISPLAY_TOKEN, null, values);

        if(rowInserted != -1)
            Toast.makeText(context, "New row added, row id: " + rowInserted, Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(context, "Something wrong", Toast.LENGTH_SHORT).show();
        db.close(); // Close Database Connection
    }


    // Getting All QmsUtility
    public ArrayList<String> Get_QmsUtility() {
        try {
            // https://stackoverflow.com/questions/14331175/load-from-spinner-sqlite-with-text-and-value
            qms_list.clear();
            qms_list.add("New Record");
            // Select All Query
            String selectQuery = "SELECT  * FROM " + TABLE_DISPLAY_TOKEN;

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                   // DataModel contact = new DataModel();
                   //  contact.setID(Integer.parseInt(cursor.getString(0)));
                   // Toast.makeText(context, "Something: "+cursor.getString(1),
                   //      Toast.LENGTH_SHORT).show();
                   // contact.setInstName(cursor.getString(1));
                   // contact.setEmail(cursor.getString(2));
                   // contact.setImage(cursor.getBlob(3));
                   // Adding contact to list
                    qms_list.add(cursor.getString(cursor.getColumnIndex("recordName")));
                    // qms_list.add(cursor.getString(cursor.getColumnIndex("recordName")));
                    // qms_list.add(cursor.getString(1));
                } while (cursor.moveToNext());
            }

            // return contact list
            cursor.close();
            db.close();
            return qms_list;
        } catch (Exception e) {
            // TODO: handle exception
            Log.e("all_qmsUtility", "" + e);
        }

        return qms_list;
    }


    public List < SpinnerObject> getAllLabels(){
        List< SpinnerObject > labels = new ArrayList < SpinnerObject > ();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_DISPLAY_TOKEN;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if ( cursor.moveToFirst () ) {
            do {
                labels.add ( new SpinnerObject ( cursor.getString(0) , cursor.getString(1) ) );
            } while (cursor.moveToNext());
        }

        // closing connection
        cursor.close();
        db.close();

        // returning labels
        return labels;
    }

    // Getting All QmsUtility
    public void getQmsUtilityById(String id, DataModel dataModel) {
        try {

            // Select All Query
            String selectQuery = "SELECT  * FROM " + TABLE_DISPLAY_TOKEN+" WHERE id = ?";

            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, new String[] {id});

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    // DataModel contact = new DataModel();
                    //  contact.setID(Integer.parseInt(cursor.getString(0)));
                    // Toast.makeText(context, cursor.getString(cursor.getColumnIndex("devId")), Toast.LENGTH_LONG).show();
                    dataModel.setDevId(cursor.getString(cursor.getColumnIndex("devId")));
                    dataModel.setDigitNo(cursor.getString(cursor.getColumnIndex("digitNo")));
                    dataModel.setSoundType(cursor.getString(cursor.getColumnIndex("soundType")));
                    dataModel.setTypeNo(cursor.getString(cursor.getColumnIndex("typeNo")));
                    dataModel.setID(Integer.parseInt(cursor.getString(cursor.getColumnIndex("id"))));
                    dataModel.setSound_id(cursor.getString(cursor.getColumnIndex("snd_id")));

                } while (cursor.moveToNext());
            }

            // return contact list
            cursor.close();
            db.close();

        } catch (Exception e) {
            // TODO: handle exception
            Log.e("all_qmsUtility", "" + e);
        }


    }


    public int updateSound(DataModel dataModel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //values.put(KEY_DEVICE_ID, dataModel.getDevId());
        //values.put(KEY_NO_OF_DIGIT, dataModel.getDigitNo());
        values.put(KEY_SOUND, dataModel.getSoundType());
        // values.put(KEY_TYPE, dataModel.getTypeNo());
       // Toast.makeText(context, "Row ID: " + dataModel.getID(), Toast.LENGTH_SHORT).show();
        // updating row

        return db.update(TABLE_DISPLAY_TOKEN, values, KEY_ID + " = ?",
                new String[] { String.valueOf(dataModel.getID()) });

    }



    public int updateDigitNo(DataModel dataModel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //values.put(KEY_DEVICE_ID, dataModel.getDevId());
        values.put(KEY_NO_OF_DIGIT, dataModel.getDigitNo());
        // values.put(KEY_SOUND, dataModel.getSoundType());
        // values.put(KEY_TYPE, dataModel.getTypeNo());
        // Toast.makeText(context, "Label Eleven: " + dataModel.getCntLabelEleven(), Toast.LENGTH_SHORT).show();
        // updating row

        return db.update(TABLE_DISPLAY_TOKEN, values, KEY_ID + " = ?",
                new String[] { String.valueOf(dataModel.getID()) });

    }

    public int up_nav_id(DataModel dataModel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DEVICE_ID, dataModel.getDevId());
        //values.put(KEY_NO_OF_DIGIT, dataModel.getDigitNo());
       // values.put(KEY_SOUND, dataModel.getSoundType());
        // values.put(KEY_TYPE, dataModel.getTypeNo());
        // Toast.makeText(context, "Label Eleven: " + dataModel.getCntLabelEleven(), Toast.LENGTH_SHORT).show();
        // updating row

        return db.update(TABLE_DISPLAY_TOKEN, values, KEY_ID + " = ?",
                new String[] { String.valueOf(dataModel.getID()) });



    }


    public int updateTypeNo(DataModel dataModel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        //values.put(KEY_DEVICE_ID, dataModel.getDevId());
        //values.put(KEY_NO_OF_DIGIT, dataModel.getDigitNo());
        values.put(KEY_TYPE, dataModel.getTypeNo());
        // values.put(KEY_TYPE, dataModel.getTypeNo());
        // Toast.makeText(context, "Label Eleven: " + dataModel.getCntLabelEleven(), Toast.LENGTH_SHORT).show();
        // updating row

        return db.update(TABLE_DISPLAY_TOKEN, values, KEY_ID + " = ?",
                new String[] { String.valueOf(dataModel.getID()) });

    }


    // Updating single qmsUtility
    public int Update_QmsUtility(DataModel dataModel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_DEVICE_ID, dataModel.getDevId());
        values.put(KEY_NO_OF_DIGIT, dataModel.getDigitNo());
        values.put(KEY_SOUND, dataModel.getSoundType());
        values.put(KEY_TYPE, dataModel.getTypeNo());
        // Toast.makeText(context, "Label Eleven: " + dataModel.getCntLabelEleven(), Toast.LENGTH_SHORT).show();
        // updating row

        return db.update(TABLE_DISPLAY_TOKEN, values, KEY_ID + " = ?",
              new String[] { String.valueOf(dataModel.getID()) });

    }


    // Deleting single qmsUtility
    public void Delete_QmsUtility(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DISPLAY_TOKEN, KEY_ID + " = ?",
                new String[] { String.valueOf(id) });
        db.close();
    }


    // Getting qmsUtility Count
    public int Get_Total_QmsUtility() {
        String countQuery = "SELECT  * FROM " + TABLE_DISPLAY_TOKEN;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

      //  Toast.makeText(context, "Row No: " + cursor.getCount(), Toast.LENGTH_SHORT).show();
        Log.d("Database",""+cursor.getCount());

        // return qms utility
        cursor.close();
        return cursor.getCount();
    }

}