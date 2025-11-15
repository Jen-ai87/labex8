package com.example.labex8;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "FavoritePlaces.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_PLACES = "places";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";

    private static final String CREATE_TABLE_PLACES = "CREATE TABLE " + TABLE_PLACES + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_NAME + " TEXT NOT NULL, "
            + COLUMN_LATITUDE + " REAL NOT NULL, "
            + COLUMN_LONGITUDE + " REAL NOT NULL"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PLACES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLACES);
        onCreate(db);
    }

    public long addPlace(Place place) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, place.getName());
        values.put(COLUMN_LATITUDE, place.getLatitude());
        values.put(COLUMN_LONGITUDE, place.getLongitude());

        long id = db.insert(TABLE_PLACES, null, values);
        db.close();

        return id;
    }

    public ArrayList<Place> getAllPlaces() {
        ArrayList<Place> placesList = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_PLACES + " ORDER BY " + COLUMN_ID + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE));

                Place place = new Place(id, name, latitude, longitude);
                placesList.add(place);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return placesList;
    }

    public Place getPlace(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_PLACES,
                new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_LATITUDE, COLUMN_LONGITUDE},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);

        Place place = null;
        if (cursor != null && cursor.moveToFirst()) {
            place = new Place(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE))
            );
            cursor.close();
        }

        db.close();
        return place;
    }

    public int updatePlace(Place place) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, place.getName());
        values.put(COLUMN_LATITUDE, place.getLatitude());
        values.put(COLUMN_LONGITUDE, place.getLongitude());

        int rowsAffected = db.update(TABLE_PLACES, values,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(place.getId())});

        db.close();
        return rowsAffected;
    }

    public void deletePlace(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLACES, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteAllPlaces() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PLACES, null, null);
        db.close();
    }

    public int getPlacesCount() {
        String countQuery = "SELECT * FROM " + TABLE_PLACES;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

}
