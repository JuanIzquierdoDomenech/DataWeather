package com.mcmu.juanjesus.dataweather.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.mcmu.juanjesus.dataweather.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NyanActivity extends AppCompatActivity {

    @Bind(R.id.nyanActivityText)protected TextView textView;

    //region Activity lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nyan);

        // Butterknife injection
        ButterKnife.bind(this);

        Intent callingIntent = getIntent();
        if(callingIntent.hasExtra("nyanText")) {
            StringBuilder builder = new StringBuilder();
            builder.append(textView.getText());
            builder.append("... ").append(callingIntent.getExtras().getString("nyanText"));
            textView.setText(builder.toString());
        }
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
