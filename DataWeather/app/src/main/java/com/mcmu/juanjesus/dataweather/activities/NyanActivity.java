package com.mcmu.juanjesus.dataweather.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mcmu.juanjesus.dataweather.R;

import butterknife.ButterKnife;

public class NyanActivity extends AppCompatActivity {

    //region Activity lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nyan);

        // Butterknife injection
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    // -------------------------------------------------------------------> Activity running

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    //endregion Activity lifecycle
}
