package com.csci448.agwa.mapit;

/**
 * Created by amosgwa on 4/10/17.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_PINS = "pins";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_LAT = "lat";
    public static final String COLUMN_LNG = "lng";


    public static final String COLUMN_COMMENT = "comment";

    private static final String DATABASE_NAME = "pins.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_PINS + "( "
            + COLUMN_TIME + " INTEGER NOT NULL, "
            + COLUMN_LAT + " REAL NOT NULL, "
            + COLUMN_LNG + " REAL NOT NULL "
            + ");";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PINS);
        onCreate(db);
    }

}