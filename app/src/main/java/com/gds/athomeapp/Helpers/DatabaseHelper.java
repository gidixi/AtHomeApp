package com.gds.athomeapp.Helpers;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "LocationDatabase";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_LOCATIONS = "locations";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_TIMESTAMP = "timestamp";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_LOCATIONS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_LATITUDE + " REAL, " +
                    COLUMN_LONGITUDE + " REAL, " +
                    COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Gestire l'aggiornamento del database se necessario
    }

    public void saveLocation(double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);
        db.insert(TABLE_LOCATIONS, null, values);
        db.close();
    }

    public List<Location> getAllLocations() {
        List<Location> locations = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_LOCATIONS, new String[]{COLUMN_LATITUDE, COLUMN_LONGITUDE, COLUMN_TIMESTAMP}, null, null, null, null, COLUMN_TIMESTAMP + " DESC");

        if (cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") double latitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE));
                @SuppressLint("Range") double longitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE));
                // String timestamp = cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP)); // Se vuoi usare il timestamp

                Location location = new Location(""); // Il provider è una stringa vuota perché non lo usiamo qui
                location.setLatitude(latitude);
                location.setLongitude(longitude);

                locations.add(location);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return locations;
    }

    public void updateLocation(int id, double newLatitude, double newLongitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LATITUDE, newLatitude);
        values.put(COLUMN_LONGITUDE, newLongitude);

        db.update(TABLE_LOCATIONS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteLocation(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOCATIONS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }


}