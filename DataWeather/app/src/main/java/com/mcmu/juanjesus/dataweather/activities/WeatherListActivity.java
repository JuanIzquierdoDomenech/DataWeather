package com.mcmu.juanjesus.dataweather.activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mcmu.juanjesus.dataweather.R;
import com.mcmu.juanjesus.dataweather.database.WeatherSQLiteOpenHelper;
import com.mcmu.juanjesus.dataweather.listadapters.WeatherListItemAdapter;
import com.mcmu.juanjesus.dataweather.models.WeatherData;
import com.mcmu.juanjesus.dataweather.utilities.AlertDialogUtilities;
import com.mcmu.juanjesus.dataweather.utilities.DateUtilities;
import com.mcmu.juanjesus.dataweather.utilities.HTTPWeatherFetch;
import com.mcmu.juanjesus.dataweather.utilities.WeatherUtilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class WeatherListActivity extends AppCompatActivity implements LocationListener {

    @Bind(R.id.weatherListAddButton)protected ImageButton addWeatherButton;
    @Bind(R.id.weatherList)protected ListView weatherList;
    @Bind(R.id.weatherListUsernameTextView)protected TextView usernameTextView;

    private WeatherSQLiteOpenHelper weatherDB;

    private static final int ONE_SECOND = 1000;

    private LocationManager locationManager;
    private String provider;

    private static Handler mainThreadHandler;

    private JSONObject lastJsonWeatherData;
    private Location lastLocationData;

    private SharedPreferences defaultSharedPreferences;

    private WeatherListItemAdapter weatherListItemAdapter;
    private Vector<WeatherData> weatherDataVector;

    private Timer timer;
    private TimerTask timerTask;


    //region Activity lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("WeatherListActivity", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weatherlist);

        // Butterknife injection
        ButterKnife.bind(this);

        // Get preferences from preferences fragment
        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences myPrefs = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);

        // Thread initialization
        mainThreadHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        // Disable add button until location found
        //addWeatherButton.setEnabled(false);
    }

    @Override
    protected void onStart() {
        Log.d("WeatherListActivity", "onStart");
        super.onStart();

        Criteria criteria = new Criteria();
        criteria.setCostAllowed(false);
        criteria.setAltitudeRequired(false);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        provider = locationManager.getBestProvider(criteria, false);

        registerLocationListener();

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            lastLocationData = locationManager.getLastKnownLocation(provider);
            //addWeatherButton.setEnabled(false);
        }

        // Check if any location service is enabled
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            AlertDialogUtilities.showNoLocationSettingsEnabled(this);
        }
    }

    @Override
    protected void onResume() {
        Log.d("WeatherListActivity", "onResume");
        super.onResume();

        if(weatherDB == null) {
            weatherDB = new WeatherSQLiteOpenHelper(this);
        }

        // Get DB data for current user
        SharedPreferences myPrefs = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        String currentUser = myPrefs.getString(getString(R.string.share_prefs_user_logged), "");
        weatherDataVector = weatherDB.getUserWeatherDataVector(currentUser);

        // Create the adapter for the list and update it
        weatherListItemAdapter = new WeatherListItemAdapter(this, weatherDataVector);
        weatherList.setAdapter(weatherListItemAdapter);

        // Show user name in textview
        usernameTextView.setText(currentUser);

        Log.d("WeatherListActivity", "onResume -> showing data for this user -> " + currentUser + ", entries " + weatherDataVector.size());

        // Insert into database every X minutes
        String insertFrequencyMinutesStr = defaultSharedPreferences.getString(getString(R.string.share_prefs_insert_freq), "60");
        int insertFrequencyMinutesInt = Integer.parseInt(insertFrequencyMinutesStr);
        insertFrequencyMinutesInt *= 60;    // Translate to minutes

        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.d("WeatherListActivity", "onResume -> Timer schedule executing");
                if(lastJsonWeatherData != null) {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            insertIntoDB();
                        }
                    });
                }
            }
        };
        Log.d("WeatherListActivity", "onResume -> scheculing timer every " + (insertFrequencyMinutesInt/60) + " minutes");
        timer.scheduleAtFixedRate(timerTask, insertFrequencyMinutesInt*1000, insertFrequencyMinutesInt*1000); // Delay X minutes since activity onResume
    }

    // -------------------------------------------------------------------> Activity running

    @Override
    protected void onPause() {
        Log.d("WeatherListActivity", "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d("WeatherListActivity", "onStop");
        //unregisterLocationListener(); We will listen for location changes even when the app is not visible
        //weatherDB = null;

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("WeatherListActivity", "onDestroy");
        super.onDestroy();
    }
    //endregion Activity lifecycle


    //region Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weather_list_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                showPreferences();
                return true;
            case R.id.action_change_user:
                changeUser();
                return true;
            case R.id.action_sort_by_location:
                weatherListItemAdapter.sort("location");
                break;
            case R.id.action_sort_by_date:
                weatherListItemAdapter.sort("date");
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    //endregion Menu


    //region UI events
    @SuppressWarnings("unused")
    @OnClick(R.id.weatherListAddButton)
    public void addWeatherButtonClicked(ImageButton imgBtn) {
        Log.d("WeatherListActivity", "addWeatherButtonClicked");

        if(lastJsonWeatherData == null) {
            Toast.makeText(this, getString(R.string.no_location_data_available), Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, getString(R.string.inserting_entry_db), Toast.LENGTH_SHORT).show();

        // Insert current weather data into db
        insertIntoDB();
    }

    @SuppressWarnings("unused")
    @OnItemClick(R.id.weatherList)
    public void itemSelected(int position) {
        Log.d("WeatherListActivity", "itemSelected at" + position);

        WeatherData weatherRowData = (WeatherData) weatherListItemAdapter.getItem(position);
        Log.d("WeatherListActivity", "itemSelected -> " + weatherRowData);
    }
    //endregion UI events

    //region Private methods
    private void changeUser() {

        // Erase username from preferences
        logOutUser();

        // Stop timer task
        timerTask.cancel();
        timer.cancel();
        timer.purge();

        // Go back to login activity
        Intent loginActivityIntent = new Intent(this, LoginActivity.class);
        startActivity(loginActivityIntent);
    }

    private void logOutUser() {

        // Override username value
        SharedPreferences myPrefs = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = myPrefs.edit();
        prefEditor.putString(getString(R.string.share_prefs_user_logged), "");
        prefEditor.apply();
    }

    private void showPreferences() {
        Intent preferencesActivityIntent = new Intent(this, PreferencesActivity.class);
        startActivity(preferencesActivityIntent);
    }
    //endregion Private methods


    //region LocationListener
    @Override
    public void onLocationChanged(Location location) {
        Log.d("WeatherListActivity", "onLocationChanged");

        // Store last location data
        lastLocationData = location;

        String yourLocation = getString(R.string.your_location) + ": " + location.getLatitude() + ", " + location.getLongitude();
        String city = getLocationName(location);

        // Store the current city in preferences
        storeCity(city);
        storeWeatherData(city);

        //addWeatherButton.setEnabled(true);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("WeatherListActivity", "onStatusChanged " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("WeatherListActivity", "onProviderEnabled " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("WeatherListActivity", "onProviderDisabled " + provider);
    }

    private void registerLocationListener() {
        String updateFrequencyStr = defaultSharedPreferences.getString(getString(R.string.share_prefs_update_freq), "0");
        String updateMetersStr = defaultSharedPreferences.getString(getString(R.string.share_prefs_update_meters), "10");

        int updateFrequencyInt = Integer.parseInt(updateFrequencyStr);
        int updateMetersInt = Integer.parseInt(updateMetersStr);

        Log.d("WeatherListActivity", "registerLocationListener " + updateFrequencyInt + " --- " + updateMetersInt);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(provider, updateFrequencyInt * ONE_SECOND, updateMetersInt, this);

            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateFrequencyInt * ONE_SECOND, updateMetersInt, this);
            }

            if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, updateFrequencyInt * ONE_SECOND, updateMetersInt, this);
            }
        }
    }

    private void unregisterLocationListener() {
        Log.d("WeatherListActivity", "unregisterLocationListener");
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }
    }
    //endregion LocationListener


    //region Location and weather methods
    private void storeCity(String city) {
        SharedPreferences myPrefs = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = myPrefs.edit();
        prefEditor.putString(getString(R.string.share_prefs_current_city), city);
        prefEditor.apply();
    }

    private String getStoredCity() {
        SharedPreferences myPrefs = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        return myPrefs.getString(getString(R.string.share_prefs_current_city), "");
    }

    private void storeWeatherData(final String city) {
        new Thread() {
            @Override
            public void run() {
                final JSONObject json = HTTPWeatherFetch.getJSON(getApplicationContext(), city);
                if(json == null) {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.weather_data_not_found),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    mainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            lastJsonWeatherData = json;
                            Log.d("WeatherListActivity", "showWeatherData weather data found");
                        }
                    });
                }
            }
        }.start();
    }
    //endregion Location and weather methods


    //region Geocoder
    private String getLocationName(Location location) {

        String result = "";

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses.size() > 0) {
                result = addresses.get(0).getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
    //endregion Geocoder


    //region DB
    private void insertIntoDB() {

        WeatherUtilities.WeatherType weatherType = WeatherUtilities.WeatherType.CLEAR;
        try {
            JSONObject currentWeather = lastJsonWeatherData.getJSONArray("weather").getJSONObject(0);
            weatherType = WeatherUtilities.getWeatherType(currentWeather.getInt("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(weatherDB == null) {

            // Create database helper
            weatherDB = new WeatherSQLiteOpenHelper(getApplicationContext());
        }

        // Insert into database
        SharedPreferences myPrefs = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        String currentUser = myPrefs.getString(getString(R.string.share_prefs_user_logged), "");

        ContentValues values = new ContentValues();
        values.put(WeatherSQLiteOpenHelper.FIELD_ROW_USER, currentUser);
        values.put(WeatherSQLiteOpenHelper.FIELD_ROW_LOCATION, getLocationName(lastLocationData));
        // values.put(WeatherSQLiteOpenHelper.FIELD_ROW_LOCATION, "Cocentaina");
        values.put(WeatherSQLiteOpenHelper.FIELD_ROW_LAT, lastLocationData.getLatitude());
        values.put(WeatherSQLiteOpenHelper.FIELD_ROW_LON, lastLocationData.getLongitude());
        values.put(WeatherSQLiteOpenHelper.FIELD_ROW_WEATHER, weatherType.toString());
        values.put(WeatherSQLiteOpenHelper.FIELD_ROW_DATE, DateUtilities.milisToDate(System.currentTimeMillis()));

        Log.d("WeatherListActivity", "insertIntoDB inserting " + values.toString());
        weatherDB.insert(values);

        // Notify the adapter for vector data changes
        weatherDataVector = weatherDB.getUserWeatherDataVector(currentUser);
        weatherListItemAdapter.updateItems(weatherDataVector);
    }
    //endregion DB
}
