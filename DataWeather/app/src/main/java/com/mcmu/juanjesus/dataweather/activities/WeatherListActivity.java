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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weatherlist);

        // Butterknife injection
        ButterKnife.bind(this);

        Vector<String> sth = new Vector<>();
        sth.add("Alcoy");sth.add("Elche");sth.add("Midgar");sth.add("Alcoy");sth.add("Elche");sth.add("Midgar");sth.add("Alcoy");sth.add("Elche");sth.add("Midgar");sth.add("Alcoy");sth.add("Elche");sth.add("Midgar");
        final ArrayAdapter<String> listAdapter = new ArrayAdapter<>(this, R.layout.activity_weatherlist_item, R.id.weatherListItemCityText, sth);
        weatherList.setAdapter(listAdapter);

        /*setContentView(R.layout.activity_login);

        final ListView listView = (ListView)findViewById(R.id.listView);
        final EditText text = (EditText)findViewById(R.id.todoText);
        final Button button = (Button)findViewById(R.id.addButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Firebase("https://dataweather.firebaseio.com/todoItems")
                        .push()
                        .child("text")
                        .setValue(text.getText().toString());
            }
        });

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1);

        listView.setAdapter(adapter);

        Firebase.setAndroidContext(this);

        new Firebase("https://dataweather.firebaseio.com/todoItems")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        adapter.add((String) dataSnapshot.child("text").getValue());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        adapter.remove((String) dataSnapshot.child("text").getValue());
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new Firebase("https://dataweather.firebaseio.com/todoItems")
                        .orderByChild("text")
                        .equalTo((String)listView.getItemAtPosition(position))
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChildren()) {
                                    DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                                    firstChild.getRef().removeValue();
                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {

                            }
                        });
            }
        });*/
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
        // Override username value
        SharedPreferences myPrefs = getSharedPreferences("MyPrefsFile", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = myPrefs.edit();
        prefEditor.putString(getString(R.string.share_prefs_user_logged), "");
        prefEditor.apply();

        // Go back to login activity
        Intent loginActivityIntent = new Intent(this, LoginActivity.class);
        startActivity(loginActivityIntent);
    }

    private void showPreferences() {
        Intent preferencesActivityIntent = new Intent(this, PreferencesActivity.class);
        startActivity(preferencesActivityIntent);
    }
    //endregion Private methods
}
