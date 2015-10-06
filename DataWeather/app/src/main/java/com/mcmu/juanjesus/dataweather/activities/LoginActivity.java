package com.mcmu.juanjesus.dataweather.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mcmu.juanjesus.dataweather.utilities.HTTPWeatherFetch;
import com.mcmu.juanjesus.dataweather.R;
import com.mcmu.juanjesus.dataweather.utilities.WeatherUtilities;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity implements LocationListener {

    @Bind(R.id.loginUsernameEditText)protected EditText userNameText;
    @Bind(R.id.loginLetsGoButton)protected Button letsGoBtn;
    @Bind(R.id.loginYourLocationTextView) protected TextView yourLocationTextView;
    @Bind(R.id.loginLoadingIndicator) protected ImageView loadingIndicator;

    private LocationManager locationManager;
    private String provider;

    private SharedPreferences userSharedPreferences;

    //private static final int ONE_MINUTES = 1000 * 60;
    //private static final int FIVE_SECONDS = 1000 * 5;
    //private static final int THIRTY_SECONDS = 1000 * 30;
    private static final int ONE_SECOND = 1000;
    private static final int FIVE_METERS = 5;

    private static Handler mainThreadHandler;

    private JSONObject lastJsonWeatherData;

    private boolean externalSendIntentReceived = false;


    //region Activity lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Login", "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // Butterknife injection
        ButterKnife.bind(this);

        if(savedInstanceState != null) {
            externalSendIntentReceived = savedInstanceState.getBoolean("externalSendIntentReceived");
            Log.d("onCreate", "savedInstanceState != null -> " + externalSendIntentReceived);
        }

        // Get SEND data
        Intent sendIntent = getIntent();
        String intentAction = sendIntent.getAction();
        String intentType = sendIntent.getType();
        if(intentAction != null) {
            if(intentAction.equals(Intent.ACTION_SEND) && intentType != null) {
                if(intentType.equals("text/plain")) {

                    // Only one time
                    if(!externalSendIntentReceived) {
                        externalSendIntentReceived = true;
                        Log.d("SEND INTENT RECEIVED", "SEND INTENT RECEIVED");
                        handleSendIntent(sendIntent);
                    }
                }
            }
        }

        // Get preferences from preferences fragment
        userSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences myPrefs = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        String possibleUserName = myPrefs.getString(getString(R.string.share_prefs_user_logged), "");

        // Thread initialization
        mainThreadHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };

        // Get location service ref for first location
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        // Is the user already logged? => Redirect
        if (!possibleUserName.equals("")) {

            // Redirect to weather list activity
            Intent weatherListActivityIntent = new Intent(this, WeatherListActivity.class);
            startActivity(weatherListActivityIntent);

            // Disable going back to this activity
            //finish();

            return;
        }

        // Start loading indicator animation
        Animation loadingIndicatorAnimation;
        loadingIndicatorAnimation = new RotateAnimation(0.0f, 360.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        loadingIndicatorAnimation.setRepeatCount(Animation.INFINITE);
        loadingIndicatorAnimation.setRepeatMode(Animation.INFINITE);
        loadingIndicatorAnimation.setDuration(1000);
        loadingIndicatorAnimation.setInterpolator(new LinearInterpolator());
        loadingIndicator.startAnimation(loadingIndicatorAnimation);
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStart() {
        Log.d("Login", "onStart");
        super.onStart();

        // Check if any location service is enabled
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            showNoLocationSettingsEnabled();
        }

        Criteria criteria = new Criteria();
        criteria.setCostAllowed(false);
        criteria.setAltitudeRequired(false);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        provider = locationManager.getBestProvider(criteria, false);

        registerLocationListener();

        Location lastLocation = new Location(provider);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            lastLocation = locationManager.getLastKnownLocation(provider);
        }

        if (lastLocation != null) {
            String yourLocation = getString(R.string.your_location) + ": " + lastLocation.getLatitude() + ", " + lastLocation.getLongitude();
            yourLocationTextView.setText(yourLocation);
            String city = getLocationName(lastLocation);
            yourLocationTextView.append("(" + city + ")");

            // Store the current city in preferences
            storeCity(city);
            showWeatherData(city);

            // Pause spinner animation if any location was found
            loadingIndicator.clearAnimation();
            loadingIndicator.setVisibility(View.INVISIBLE);

            // Notify location found
            locationFound();
        } else {

            // Notify location lost
            locationLost();
        }
    }

    @Override
    protected void onResume() {
        Log.d("Login", "onResume");
        super.onResume();
    }

    // -------------------------------------------------------------------> Activity running

    @Override
    protected void onPause() {
        Log.d("Login", "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d("Login", "onStop");
        unregisterLocationListener();

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("Login", "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d("Login", "onSaveInstanceState");

        outState.putBoolean("externalSendIntentReceived", externalSendIntentReceived);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("Login", "onRestoreInstanceState");
    }
    //endregion Activity lifecycle


    //region Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login_activity, menu);
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
            case R.id.action_share_current_weather:
                shareCurrentWeather();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showPreferences() {
        Intent preferencesActivityIntent = new Intent(this, PreferencesActivity.class);
        startActivity(preferencesActivityIntent);
    }

    private void shareCurrentWeather() {
        if(lastJsonWeatherData == null) {
            Toast.makeText(this, getString(R.string.no_weather_data_to_share), Toast.LENGTH_SHORT).show();
            return;
        }

        // Try to get json data from last time
        StringBuilder weatherString = new StringBuilder();
        try {
            JSONObject currentWeather = lastJsonWeatherData.getJSONArray("weather").getJSONObject(0);
            weatherString.append(currentWeather.getString("description"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (weatherString.length() > 0) {

            weatherString.append(" ").append(getString(R.string.in)).append(" ").append(getStoredCity());

            Intent sendWeatherIntent = new Intent();
            sendWeatherIntent.setAction(Intent.ACTION_SEND);
            sendWeatherIntent.putExtra(Intent.EXTRA_TEXT, weatherString.toString());
            sendWeatherIntent.setType("text/plain");
            startActivity(Intent.createChooser(sendWeatherIntent, getString(R.string.share_with)));
        }
    }
    //endregion Menu


    //region UI events
    @SuppressWarnings("unused")
    @OnClick(R.id.loginLetsGoButton)
    public void LetsGoBtnClicked(Button target) {

        // Check if any location service is enabled
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            showNoLocationSettingsEnabled();
            return;
        }

        if (userNameText.length() == 0) {
            Toast.makeText(this, getString(R.string.cannot_empty_name), Toast.LENGTH_SHORT).show();
        } else {
            String userName = userNameText.getText().toString();

            // Store the user name in preferences
            SharedPreferences myPrefs = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
            SharedPreferences.Editor prefEditor = myPrefs.edit();
            prefEditor.putString(getString(R.string.share_prefs_user_logged), userName);
            prefEditor.apply();

            Toast.makeText(this, getString(R.string.welcome) + " " + userName, Toast.LENGTH_SHORT).show();

            Intent weatherListActivityIntent = new Intent(this, WeatherListActivity.class);
            startActivity(weatherListActivityIntent);

            // Disable going back to this activity
            //finish();
        }
    }
    //endregion UI events


    //region LocationListener
    @Override
    public void onLocationChanged(Location location) {

        Log.d("Login:onLocationChanged", location.toString());

        String yourLocation = getString(R.string.your_location) + ": " + location.getLatitude() + ", " + location.getLongitude();
        yourLocationTextView.setText(yourLocation);
        String city = getLocationName(location);
        yourLocationTextView.append("(" + city + ")");

        // Store the current city in preferences
        storeCity(city);
        showWeatherData(city);

        // Stop the animation if any is found
        loadingIndicator.clearAnimation();
        loadingIndicator.setVisibility(View.INVISIBLE);

        // Notify location found
        locationFound();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Login:onStatusChanged", provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Login:onStatusChanged", provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Login:onStatusChanged", provider);
    }

    private void registerLocationListener() {
        String updateFrequencyStr = userSharedPreferences.getString(getString(R.string.share_prefs_update_freq), "1");
        int updateFrequencyInt = Integer.parseInt(updateFrequencyStr);

        Log.d("RegisterLocListener", "" + updateFrequencyInt);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(provider, updateFrequencyInt * ONE_SECOND, FIVE_METERS, this);
        }
    }

    private void unregisterLocationListener() {
        Log.d("UnregisterLocListener", "");
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }
    }
    //endregion LocationListener


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

        // Log.d("DW:getLocationName", result);
        return result;
    }
    //endregion Geocoder


    //region Alerts
    private void showNoLocationSettingsEnabled() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage(getString(R.string.location_service_disabled));
        alert.setPositiveButton(getString(R.string.enable), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent locationSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(locationSettingsIntent);
            }
        });
        alert.setNegativeButton(getString(R.string.cancel), null);
        alert.show();
    }
    //endregion Alerts


    //region Location and weather methods
    private void locationFound() {
        letsGoBtn.setEnabled(true);
    }

    private void locationLost() {
        letsGoBtn.setEnabled(false);
    }

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

    private void showWeatherData(final String city) {
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
                            showWeatherData(json);
                        }
                    });
                }
            }
        }.start();
    }

    private void showWeatherData(JSONObject json) {
        try {

            lastJsonWeatherData = json;
            JSONObject currentWeather = json.getJSONArray("weather").getJSONObject(0);
            WeatherUtilities.WeatherType weatherType = WeatherUtilities.getWeatherType(currentWeather.getInt("id"));

            String description = currentWeather.getString("description") + " (" + weatherType.toString() + ")";

            Toast.makeText(this, description, Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //endregion Location and weather methods


    //region Send intent handlers
    private void handleSendIntent(Intent sendIntent) {
        String sharedText = sendIntent.getStringExtra(Intent.EXTRA_TEXT);

        Intent nyanActivityIntent = new Intent(this, NyanActivity.class);
        nyanActivityIntent.putExtra("nyanText", sharedText);
        startActivity(nyanActivityIntent);
    }
    //endregion Send intent handlers
}
