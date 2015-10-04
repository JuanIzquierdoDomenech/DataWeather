package com.mcmu.juanjesus.dataweather;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    private static final int ONE_MINUTES = 1000 * 60;
    private static final int FIVE_SECONDS = 1000 * 5;
    private static final int THIRTY_SECONDS = 1000 * 30;
    private static final int FIVE_METERS = 5;

    private Animation loadingIndicatorAnimation;


    //region Activity lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Butterknife injection
        ButterKnife.bind(this);

        SharedPreferences myPrefs = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        String possibleUserName = myPrefs.getString(getString(R.string.share_prefs_user_logged), "");

        // Get location service ref for first location
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        // Is the user already logged? => Redirect
        if (!possibleUserName.equals("")) {

            // Redirect to weather list activity
            Intent weatherListActivityIntent = new Intent(this, WeatherListActivity.class);
            startActivity(weatherListActivityIntent);
        }

        // Start loading indicator animation
        loadingIndicatorAnimation = new RotateAnimation(0.0f, 360.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        loadingIndicatorAnimation.setRepeatCount(Animation.INFINITE);
        loadingIndicatorAnimation.setRepeatMode(Animation.INFINITE);
        loadingIndicatorAnimation.setDuration(1000);
        loadingIndicatorAnimation.setInterpolator(new LinearInterpolator());
        loadingIndicator.startAnimation(loadingIndicatorAnimation);
        loadingIndicator.setVisibility(View.VISIBLE);

        // Check if any location service is enabled
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                && !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            showNoLocationSettingsEnabled();
            return;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d("DW_onStart", "onStart");
        Criteria criteria = new Criteria();
        criteria.setCostAllowed(false);
        criteria.setAltitudeRequired(false);
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        provider = locationManager.getBestProvider(criteria, false);

        locationManager.requestLocationUpdates(provider, THIRTY_SECONDS, FIVE_METERS, this);
        Location lastLocation = locationManager.getLastKnownLocation(provider);

        Log.d("LOCATION", "LOCATION MANAGER REGISTERED - " + provider);
        // onLocationChanged will realize about the changes
        if (lastLocation != null) {
            String yourLocation = getString(R.string.your_location) + ": " + lastLocation.getLatitude() + ", " + lastLocation.getLongitude();
            yourLocationTextView.setText(yourLocation);
            String city = getLocationName(lastLocation);
            yourLocationTextView.append("(" + city + ")");

            // Pause spinner animation if any location was found
            loadingIndicator.clearAnimation();
            loadingIndicator.setVisibility(View.INVISIBLE);

            locationFound();
        } else {
            locationLost();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // -------------------------------------------------------------------> Activity running

    @Override
    protected void onPause() {
        Log.d("Login:onPause", "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d("Login:onStop", "onStop");
        locationManager.removeUpdates(this);

        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("Login:onDestroy", "onDestroy");
        super.onDestroy();
    }
    //endregion Activity lifecycle


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
        }
    }
    //endregion UI events


    //region LocationListener
    @Override
    public void onLocationChanged(Location location) {
        Log.d("Login:onLocationChanged", location.toString());
        if (location != null) {
            String yourLocation = getString(R.string.your_location) + ": " + location.getLatitude() + ", " + location.getLongitude();
            yourLocationTextView.setText(yourLocation);
            String city = getLocationName(location);
            yourLocationTextView.append("(" + city + ")");

            // Stop the animation if any is found
            loadingIndicator.clearAnimation();
            loadingIndicator.setVisibility(View.INVISIBLE);

            locationFound();
        } else {
            yourLocationTextView.setText(getString(R.string.location_not_found));

            // Start the animation if the location is lost
            loadingIndicator.startAnimation(loadingIndicatorAnimation);
            loadingIndicator.setVisibility(View.VISIBLE);

            locationLost();
        }
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

        Log.d("DW:getLocationName", result);
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


    //region Private methods
    private void locationFound() {
        letsGoBtn.setEnabled(true);
    }

    private void locationLost() {
        letsGoBtn.setEnabled(false);
    }

    //endregion Private methods
}
