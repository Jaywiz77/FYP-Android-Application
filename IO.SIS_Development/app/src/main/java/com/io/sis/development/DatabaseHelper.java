package com.io.sis.development;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "Phone_Usage.db";
    public static final String PHONE_USAGE_TABLE_NAME = "phone_usage_table";
    public static final String USAGE_COL_1 = "ID";
    public static final String USAGE_COL_2 = "UID";
    public static final String USAGE_COL_3 = "COMPONENT";
    public static final String USAGE_COL_4 = "START_TIME";
    public static final String USAGE_COL_5 = "END_TIME";
    public static final String USAGE_COL_6 = "EVENT_ID";
    public static final String USAGE_COL_7 = "STATUS";
    public static final String USAGE_COL_8 = "COMMENT";
    public static final String BEACON_TABLE_NAME = "beacon_table";
    public static final String BEACON_COL_1 = "ID";
    public static final String BEACON_COL_2 = "UID";
    public static final String BEACON_COL_3 = "NAMESPACE_ID";
    public static final String BEACON_COL_4 = "INSTANCE_ID";
    public static final String BEACON_COL_5 = "BEACON_TYPE";
    public static final String BEACON_COL_6 = "START_TIME";
    public static final String BEACON_COL_7 = "END_TIME";
    public static final String BEACON_COL_8 = "EVENT_ID";
    public static final String BEACON_COL_9 = "BATTERY";
    public static final String BEACON_COL_10 = "TEMPERATURE";
    public static final String BEACON_COL_11 = "STATUS";
    public static final String BEACON_COL_12 = "COMMENT";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + PHONE_USAGE_TABLE_NAME +
                " (ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                " UID INTEGER NOT NULL," +
                "COMPONENT VARCHAR(64) NOT NULL," +
                "START_TIME VARCHAR(64) DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                "END_TIME VARCHAR(64) DEFAULT CURRENT_TIMESTAMP NOT NULL, " +
                "EVENT_ID INTEGER NOT NULL, " +
                "STATUS VARCHAR(64) NOT NULL, " +
                "COMMENT VARCHAR(64) NOT NULL)");
        db.execSQL("create table " + BEACON_TABLE_NAME +
                " (ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "UID INTEGER NOT NULL," +
                "NAMESPACE_ID VARCHAR(64) NOT NULL," +
                "INSTANCE_ID VARCHAR(64) NOT NULL," +
                "BEACON_TYPE VARCHAR(64) NOT NULL," +
                "START_TIME VARCHAR(64) DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                "END_TIME VARCHAR(64) DEFAULT CURRENT_TIMESTAMP NOT NULL," +
                "EVENT_ID INTEGER NOT NULL, " +
                "BATTERY VARCHAR(64) NOT NULL, " +
                "TEMPERATURE VARCHAR(64) NOT NULL, " +
                "STATUS VARCHAR(64) NOT NULL, " +
                "COMMENT VARCHAR(64) NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+PHONE_USAGE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+BEACON_TABLE_NAME);
        onCreate(db);
    }

    public boolean insertPhoneUsageData(int UID, String COMPONENT, String START_TIME, String END_TIME, int EVENT_ID, String STATUS, String COMMENT) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(USAGE_COL_2, UID);
        contentValues.put(USAGE_COL_3, COMPONENT);
        contentValues.put(USAGE_COL_4, START_TIME);
        contentValues.put(USAGE_COL_5, END_TIME);
        contentValues.put(USAGE_COL_6, EVENT_ID);
        contentValues.put(USAGE_COL_7, STATUS);
        contentValues.put(USAGE_COL_8, COMMENT);
        // returns the row ID of the newly inserted row, or -1 if an error occurred
        long result = db.insert(PHONE_USAGE_TABLE_NAME,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllPhoneUsageData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("select * from "+PHONE_USAGE_TABLE_NAME,null);
        return result;
    }

    //public Integer updateData(int UID, String COMPONENT, String START_TIME, String END_TIME, int EVENT_ID, String STATUS) {
    public Integer updatePhoneUsageData(String END_TIME, int EVENT_ID, String STATUS, String COMMENT) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        //contentValues.put(USAGE_COL_2, UID);
        //contentValues.put(USAGE_COL_3, COMPONENT);
        //contentValues.put(USAGE_COL_4, START_TIME);
        contentValues.put(USAGE_COL_5, END_TIME);
        //contentValues.put(COL_6, EVENT_ID);
        contentValues.put(USAGE_COL_7, STATUS);
        contentValues.put(USAGE_COL_8, COMMENT);
        // returns the number of rows affected
        int result = db.update(PHONE_USAGE_TABLE_NAME, contentValues, "EVENT_ID = ?",new String[] { String.valueOf(EVENT_ID) });
        Log.e("DATABASE", String.valueOf(result));
        return result;
    }

    public Integer deleteAllPhoneUsageData(String UID) {
        SQLiteDatabase db = this.getWritableDatabase();
        // returns the number of rows affected if a whereClause is passed in, 0 otherwise.
        return db.delete(PHONE_USAGE_TABLE_NAME, "UID = ?",new String[] {UID});
    }

    public boolean insertBeaconData(int UID, String NAMESPACE_ID, String INSTANCE_ID, String BEACON_TYPE, String START_TIME, String END_TIME, int EVENT_ID, String BATTERY, String TEMPERATURE, String STATUS, String COMMENT) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(BEACON_COL_2, UID);
        contentValues.put(BEACON_COL_3, NAMESPACE_ID);
        contentValues.put(BEACON_COL_4, INSTANCE_ID);
        contentValues.put(BEACON_COL_5, BEACON_TYPE);
        contentValues.put(BEACON_COL_6, START_TIME);
        contentValues.put(BEACON_COL_7, END_TIME);
        contentValues.put(BEACON_COL_8, EVENT_ID);
        contentValues.put(BEACON_COL_9, BATTERY);
        contentValues.put(BEACON_COL_10, TEMPERATURE);
        contentValues.put(BEACON_COL_11, STATUS);
        contentValues.put(BEACON_COL_12, COMMENT);
        // returns the row ID of the newly inserted row, or -1 if an error occurred
        long result = db.insert(BEACON_TABLE_NAME,null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllBeaconData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("select * from "+BEACON_TABLE_NAME,null);
        return result;
    }

    public Integer updateBeaconData(String END_TIME, int EVENT_ID, String STATUS, String COMMENT) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
//        contentValues.put(BEACON_COL_2, UID);
//        contentValues.put(BEACON_COL_3, NAMESPACE_ID);
//        contentValues.put(BEACON_COL_4, INSTANCE_ID);
//        contentValues.put(BEACON_COL_5, BEACON_TYPE);
//        contentValues.put(BEACON_COL_6, START_TIME);
        contentValues.put(BEACON_COL_7, END_TIME);
        contentValues.put(BEACON_COL_8, EVENT_ID);
//        contentValues.put(BEACON_COL_9, BATTERY);
//        contentValues.put(BEACON_COL_10, TEMPERATURE);
        contentValues.put(BEACON_COL_11, STATUS);
        contentValues.put(BEACON_COL_12, COMMENT);
        // returns the number of rows affected
        int result = db.update(BEACON_TABLE_NAME, contentValues, "EVENT_ID = ?",new String[] { String.valueOf(EVENT_ID) });
        Log.e("DATABASE", String.valueOf(result));
        return result;
    }

    public Integer deleteAllBeaconData(String UID) {
        SQLiteDatabase db = this.getWritableDatabase();
        // returns the number of rows affected if a whereClause is passed in, 0 otherwise.
        return db.delete(BEACON_TABLE_NAME, "UID = ?",new String[] {UID});
    }
}
