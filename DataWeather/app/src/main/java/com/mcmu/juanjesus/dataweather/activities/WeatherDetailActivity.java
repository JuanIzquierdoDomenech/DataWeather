package com.mcmu.juanjesus.dataweather.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.mcmu.juanjesus.dataweather.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WeatherDetailActivity extends AppCompatActivity {

    @Bind(R.id.detailLocation) TextView txtLocation;
    @Bind(R.id.detailLatitude) TextView txtLat;
    @Bind(R.id.detailLongitude) TextView txtLong;
    @Bind(R.id.detailDate) TextView txtDate;
    @Bind(R.id.detailWeather) TextView txtWeather;
    @Bind(R.id.detailUsername) TextView txtUser;

    //region Activity lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("WeatherListActivity", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_detail);

        // Butterknife injection
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        Log.d("WeatherListActivity", "onStart");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d("WeatherListActivity", "onResume");
        super.onResume();
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
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d("WeatherListActivity", "onDestroy");
        super.onDestroy();
    }
    //endregion Activity lifecycle


}
