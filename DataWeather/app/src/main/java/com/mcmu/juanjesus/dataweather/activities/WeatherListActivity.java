package com.mcmu.juanjesus.dataweather.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import com.mcmu.juanjesus.dataweather.R;

import java.util.Vector;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnItemSelected;

public class WeatherListActivity extends AppCompatActivity {

    @Bind(R.id.weatherListAddButton)protected ImageButton addWeatherButton;
    @Bind(R.id.weatherList)protected ListView weatherList;

    //region Activity lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("WeatherListActivity", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weatherlist);

        // Butterknife injection
        ButterKnife.bind(this);

        Vector<String> sth = new Vector<>();
        sth.add("Alcoy");sth.add("Elche");sth.add("Midgar");sth.add("Alcoy");sth.add("Elche");sth.add("Midgar");sth.add("Alcoy");sth.add("Elche");sth.add("Midgar");sth.add("Alcoy");sth.add("Elche");sth.add("Midgar");
        final ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this, R.layout.activity_weatherlist_item, R.id.weatherListItemCityText, sth);
        weatherList.setAdapter(listAdapter);
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
        Log.d("addWeatherButtonClicked", "addWeatherButtonClicked");
    }

    @SuppressWarnings("unused")
    @OnItemClick(R.id.weatherList)
    public void itemSelected(int position) {
        Log.d("itemSelected", "" + position);
    }
    //endregion UI events

    //region Private methods
    private void changeUser() {

        // Erase username from preferences
        logOutUser();

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
}
