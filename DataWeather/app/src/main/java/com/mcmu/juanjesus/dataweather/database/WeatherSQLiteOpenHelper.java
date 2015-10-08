package com.mcmu.juanjesus.dataweather.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class WeatherSQLiteOpenHelper extends SQLiteOpenHelper{

    //region Public member variables
    public static final String FIELD_ROW_ID = "_id";
    public static final String FIELD_ROW_USER = "user";
    public static final String FIELD_ROW_LOCATION = "location";
    public static final String FIELD_ROW_LAT = "lat";
    public static final String FIELD_ROW_LON = "lon";
    public static final String FIELD_ROW_WEATHER = "weather";
    //endregion Public member variables


    //region Private member variables
    private static final String DB_NAME = "dataweather";
    private static final int VERSION = 1;
    private static final String WEATHER_TABLE = "weather";
    private static final String WEATHER_TABLE_SQL_CREATE =
            "CREATE TABLE " + WEATHER_TABLE + " ( " +
                    FIELD_ROW_ID + " integer primary key autoincrement, " +
                    FIELD_ROW_USER + " text, " +
                    FIELD_ROW_LOCATION + " text, " +
                    FIELD_ROW_LAT + " double, " +
                    FIELD_ROW_LON + " double, " +
                    FIELD_ROW_WEATHER + " text " +
                    " ) ";

    private SQLiteDatabase _DB;
    //endregion Private member variables


    //region Constructor
    public WeatherSQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
        _DB = getWritableDatabase();
    }
    //endregion Constructor


    //region SQLiteOpenHelper
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(WEATHER_TABLE_SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    //endregion

    //region DB methods

    /**
     * Insterts row in the weather table
     * @param contentValues
     * @return row id
     */
    public long insert(ContentValues contentValues) {
        long rowID = _DB.insert(WEATHER_TABLE, null, contentValues);
        return rowID;
    }

    /**
     * Deletes all entries in the weather table
     * @return deleted rows count
     */
    public int del() {
        int cnt = _DB.delete(WEATHER_TABLE, null, null);
        return cnt;
    }

    /**
     * Gets all weather table rows
     * @return
     */
    public Cursor getAllWeatherData() {
        return _DB.query(WEATHER_TABLE, new String[]{FIELD_ROW_ID, FIELD_ROW_USER, FIELD_ROW_LOCATION, FIELD_ROW_LAT, FIELD_ROW_LON, FIELD_ROW_WEATHER}, null, null, null, null, null);
    }
    //endregion DB methods
}
