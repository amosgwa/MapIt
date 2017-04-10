package com.csci448.agwa.mapit;

/**
 * Created by amosgwa on 4/10/17.
 */
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Comment;

public class PinsDataSource {
    private static String TAG = "<MAP>";
    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;

    private String[] allColumns = {
            MySQLiteHelper.COLUMN_TIME,
            MySQLiteHelper.COLUMN_LAT,
            MySQLiteHelper.COLUMN_LNG };

    public PinsDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    private static ContentValues getContentValues(Pin pin) {
        Log.d(TAG, pin.getPos().toString());
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_TIME, pin.getTime().getTime());
        values.put(MySQLiteHelper.COLUMN_LAT, pin.getPos().latitude);
        values.put(MySQLiteHelper.COLUMN_LNG, pin.getPos().longitude);

        return values;
    }

    public void addPin(Pin pin) {
        ContentValues values = getContentValues(pin);
        database.insert(MySQLiteHelper.TABLE_PINS, null, values);
    }

    public void deletePins() {
        database.delete(MySQLiteHelper.TABLE_PINS, null, null);
    }

    public ArrayList<Pin> getAllPins() {
        ArrayList<Pin> pins = new ArrayList<Pin>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_PINS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            Pin pin = cursorToPin(cursor);
            pins.add(pin);
            cursor.moveToNext();
        }
        // Close the cursor
        cursor.close();
        return pins;
    }

    private Pin cursorToPin(Cursor cursor) {
        Date date = new Date(cursor.getLong(0));
        LatLng lat_lng = new LatLng(cursor.getDouble(1), cursor.getDouble(2));

        Pin pin = new Pin(date, lat_lng);

        return pin;
    }
}
