package com.mcmu.juanjesus.dataweather.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.AnimationDrawable;
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


import com.google.android.gms.*;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.mcmu.juanjesus.dataweather.database.WeatherSQLiteOpenHelper;
import com.mcmu.juanjesus.dataweather.utilities.AlertDialogUtilities;
import com.mcmu.juanjesus.dataweather.utilities.DateUtilities;
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

import static com.mcmu.juanjesus.dataweather.R.drawable.clear_weather_anim;
import static com.mcmu.juanjesus.dataweather.R.drawable.cloudy_weather_anim;
import static com.mcmu.juanjesus.dataweather.R.drawable.drizzle_weather_anim;
import static com.mcmu.juanjesus.dataweather.R.drawable.foggy_weather_anim;
import static com.mcmu.juanjesus.dataweather.R.drawable.rainy_weather_anim;
import static com.mcmu.juanjesus.dataweather.R.drawable.snowy_weather_anim;
import static com.mcmu.juanjesus.dataweather.R.drawable.thunderstorm_weather_anim;

public class LoginActivity extends AppCompatActivity
        implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    @Bind(R.id.loginUsernameEditText)protected EditText userNameText;
    @Bind(R.id.loginLetsGoButton)protected Button letsGoBtn;
    @Bind(R.id.loginYourLocationTextView) protected TextView yourLocationTextView;
    @Bind(R.id.loginLoadingIndicator) protected ImageView loadingIndicator;
    @Bind(R.id.loginCurrentWeatherImage) protected ImageView currentWeatherImage;

    @Bind(R.id.signInButton) protected com.google.android.gms.common.SignInButton signInButton;
    @Bind(R.id.signOutButton) protected Button signOutButton;

    private LocationManager mLocationManager;
    private String mProvider;

    private SharedPreferences mDefaultSharedPreferences;

    private static Handler mMainThreadHandler;

    private JSONObject mLastJsonWeatherData;
    private Location mLastLocationData;

    private WeatherSQLiteOpenHelper mWeatherSQLiteOpenHelper;

    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 9001;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;

    private boolean mExternalSendIntentReceived = false;

    private static final int ONE_SECOND = 1000;

    //region Activity lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("LoginActivity", "onCreate");
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // Butterknife injection
        ButterKnife.bind(this);

        if(savedInstanceState != null) {
            mExternalSendIntentReceived = savedInstanceState.getBoolean("externalSendIntentReceived");
            Log.d("onCreate", "savedInstanceState != null -> " + mExternalSendIntentReceived);
        }

        // Build google api client
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API)
                .addScope(Games.SCOPE_GAMES)
                .build();

        // Get SEND data
        /*Intent sendIntent = getIntent();
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
        }*/

        // Get preferences from preferences fragment
        mDefaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences myPrefs = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        String possibleUserName = myPrefs.getString(getString(R.string.share_prefs_user_logged), "");

        // Thread initialization
        mMainThreadHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };

        // Get location service ref for first location
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        // Is the user already logged? => Redirect
        if (!possibleUserName.equals("")) {

            // Redirect to weather list activity
            Intent weatherListActivityIntent = new Intent(this, WeatherListActivity.class);
            startActivity(weatherListActivityIntent);

            // Disable going back to this activity
            finish();

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
        Log.d("LoginActivity", "onStart");
        super.onStart();

        // Connect google api client
        // mGoogleApiClient.connect();

        // Check if any location service is enabled
        if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && !mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            AlertDialogUtilities.showNoLocationSettingsEnabledAlert(this);
        }

        Criteria criteria = new Criteria();
        criteria.setCostAllowed(false);
        criteria.setAltitudeRequired(false);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        mProvider = mLocationManager.getBestProvider(criteria, false);

        registerLocationListener();

        Location lastLocation = new Location(mProvider);
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            lastLocation = mLocationManager.getLastKnownLocation(mProvider);
        }

        if (lastLocation != null) {

            mLastLocationData = lastLocation;

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
        Log.d("LoginActivity", "onResume");
        super.onResume();
    }

    // -------------------------------------------------------------------> Activity running

    @Override
    protected void onPause() {
        Log.d("LoginActivity", "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d("LoginActivity", "onStop");

        // Stop requesting connection updates
        unregisterLocationListener();
        mWeatherSQLiteOpenHelper = null;

        // Disconnect google api client
        // mGoogleApiClient.disconnect();

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("LoginActivity", "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d("LoginActivity", "onSaveInstanceState");

        outState.putBoolean("externalSendIntentReceived", mExternalSendIntentReceived);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("LoginActivity", "onRestoreInstanceState");
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
        if(mLastJsonWeatherData == null) {
            Toast.makeText(this, getString(R.string.no_weather_data_to_share), Toast.LENGTH_SHORT).show();
            return;
        }

        // Try to get json data from last time
        StringBuilder weatherString = new StringBuilder();
        try {
            JSONObject currentWeather = mLastJsonWeatherData.getJSONArray("weather").getJSONObject(0);
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
        if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && !mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            AlertDialogUtilities.showNoLocationSettingsEnabledAlert(this);
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

            // Insert first entry into database
            WeatherUtilities.WeatherType weatherType = WeatherUtilities.WeatherType.CLEAR;
            try {
                JSONObject currentWeather = mLastJsonWeatherData.getJSONArray("weather").getJSONObject(0);
                weatherType = WeatherUtilities.getWeatherType(currentWeather.getInt("id"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(mWeatherSQLiteOpenHelper == null) {

                // Create database helper
                mWeatherSQLiteOpenHelper = new WeatherSQLiteOpenHelper(getApplicationContext());
            }

            ContentValues values = new ContentValues();
            values.put(WeatherSQLiteOpenHelper.FIELD_ROW_USER, userName);
            values.put(WeatherSQLiteOpenHelper.FIELD_ROW_LOCATION, getLocationName(mLastLocationData));
            values.put(WeatherSQLiteOpenHelper.FIELD_ROW_LAT, mLastLocationData.getLatitude());
            values.put(WeatherSQLiteOpenHelper.FIELD_ROW_LON, mLastLocationData.getLongitude());
            values.put(WeatherSQLiteOpenHelper.FIELD_ROW_WEATHER, weatherType.toString());
            values.put(WeatherSQLiteOpenHelper.FIELD_ROW_DATE, DateUtilities.milisToDate(System.currentTimeMillis()));

            mWeatherSQLiteOpenHelper.insert(values);

            // Change activity
            Intent weatherListActivityIntent = new Intent(this, WeatherListActivity.class);
            startActivity(weatherListActivityIntent);

            // Disable going back to this activity
            finish();
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.loginCurrentWeatherImage)
    public void weatherAnimImageClicked(ImageView imgv) {

        AnimationDrawable anim = (AnimationDrawable)imgv.getBackground();
        if(anim.isRunning()) {
            anim.stop();
        } else {
            anim.start();
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.signInButton)
    public void signInButtonClicked(com.google.android.gms.common.SignInButton btn) {
        Log.d("LoginActivity", "signInButtonClicked");

        // start the asynchronous sign in flow
        mSignInClicked = true;
        mGoogleApiClient.connect();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.signOutButton)
    public void signOutButtonClicked(Button btn) {
        Log.d("LoginActivity", "signOutButtonClicked");

        mSignInClicked = false;
        Games.signOut(mGoogleApiClient);

        // Show the sign in button, hide the sign out button
        signInButton.setVisibility(View.VISIBLE);
        signOutButton.setVisibility(View.GONE);
    }
    //endregion UI events


    //region LocationListener
    @Override
    public void onLocationChanged(Location location) {

        Log.d("LoginActivity", "onLocationChanged -> " + location.toString());

        // Store last location data
        mLastLocationData = location;

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
        Log.d("LoginActivity", "onStatusChanged -> " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("LoginActivity", "onProviderEnabled -> " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("LoginActivity", "onProviderDisabled -> " + provider);
    }

    private void registerLocationListener() {
        String updateFrequencyStr = mDefaultSharedPreferences.getString(getString(R.string.share_prefs_update_freq), "0");
        String updateMetersStr = mDefaultSharedPreferences.getString(getString(R.string.share_prefs_update_meters), "10");

        int updateFrequencyInt = Integer.parseInt(updateFrequencyStr);
        int updateMetersInt = Integer.parseInt(updateMetersStr);

        Log.d("LoginActivity", "registerLocationListener -> " + updateFrequencyInt);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.requestLocationUpdates(mProvider, updateFrequencyInt * ONE_SECOND, updateMetersInt, this);

            if(mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, updateFrequencyInt * ONE_SECOND, updateMetersInt, this);
            }

            if(mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, updateFrequencyInt * ONE_SECOND, updateMetersInt, this);
            }
        }
    }

    private void unregisterLocationListener() {
        Log.d("LoginActivity", "unregisterLocationListener");
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.removeUpdates(this);
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
                    mMainThreadHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    getString(R.string.weather_data_not_found),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    mMainThreadHandler.post(new Runnable() {
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

            mLastJsonWeatherData = json;
            JSONObject currentWeather = json.getJSONArray("weather").getJSONObject(0);
            WeatherUtilities.WeatherType weatherType = WeatherUtilities.getWeatherType(currentWeather.getInt("id"));

            //String description = currentWeather.getString("description");
            String description = "";
            AnimationDrawable spriteAnim = null;

            switch (weatherType) {
                case CLEAR:
                    currentWeatherImage.setBackgroundResource(clear_weather_anim);
                    spriteAnim = (AnimationDrawable)currentWeatherImage.getBackground();
                    description = getString(R.string.weather_clear);
                    break;
                case CLOUDY:
                    currentWeatherImage.setBackgroundResource(cloudy_weather_anim);
                    spriteAnim = (AnimationDrawable)currentWeatherImage.getBackground();
                    description = getString(R.string.weather_cloudy);
                    break;
                case DRIZZLE:
                    currentWeatherImage.setBackgroundResource(drizzle_weather_anim);
                    spriteAnim = (AnimationDrawable)currentWeatherImage.getBackground();
                    description = getString(R.string.weather_drizzle);
                    break;
                case FOGGY:
                    currentWeatherImage.setBackgroundResource(foggy_weather_anim);
                    spriteAnim = (AnimationDrawable)currentWeatherImage.getBackground();
                    description = getString(R.string.weather_foggy);
                    break;
                case RAINY:
                    currentWeatherImage.setBackgroundResource(rainy_weather_anim);
                    spriteAnim = (AnimationDrawable)currentWeatherImage.getBackground();
                    description = getString(R.string.weather_rainy);
                    break;
                case SNOWY:
                    currentWeatherImage.setBackgroundResource(snowy_weather_anim);
                    spriteAnim = (AnimationDrawable)currentWeatherImage.getBackground();
                    description = getString(R.string.weather_snowy);
                    break;
                case THUNDERSTORM:
                    currentWeatherImage.setBackgroundResource(thunderstorm_weather_anim);
                    spriteAnim = (AnimationDrawable)currentWeatherImage.getBackground();
                    description = getString(R.string.weather_thunderstorm);
                    break;
                default:
                    break;
            }

            if(spriteAnim != null) {
                spriteAnim.start();
            }

            currentWeatherImage.setVisibility(View.VISIBLE);

            Toast.makeText(this, description, Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    //endregion Location and weather methods


    //region GoogleApiClient.ConnectionCallbacks
    @Override
    public void onConnected(Bundle bundle) {
        Log.d("LoginActivity", "onConnected(google api client)");

        // Once connected, show the sign out button and hide the sign in
        signInButton.setVisibility(View.GONE);
        signOutButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("LoginActivity", "onConnectionSuspended(google api client)");

        // Attempt to reconnect
        mGoogleApiClient.connect();
    }
    //endregion GoogleApiClient.ConnectionCallbacks


    //region GoogleApiClient.OnConnectionFailedListener
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d("LoginActivity", "onConnectionFailed(google api client)");

        if (mResolvingConnectionFailure) {
            // already resolving
            return;
        }

        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(this,
                    mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, getString(R.string.error_connect_google_play_services))) {
                mResolvingConnectionFailure = false;
            }
        }

        // Put code here to display the sign-in button
        Log.d("LoginActivity", "onConnectionFailed -> display sign-in button");
    }
    //endregion GoogleApiClient.OnConnectionFailedListener


    //region Send intent handlers
    /*private void handleSendIntent(Intent sendIntent) {
        String sharedText = sendIntent.getStringExtra(Intent.EXTRA_TEXT);

        Intent nyanActivityIntent = new Intent(this, NyanActivity.class);
        nyanActivityIntent.putExtra("nyanText", sharedText);
        startActivity(nyanActivityIntent);
    }*/
    //endregion Send intent handlers
}
