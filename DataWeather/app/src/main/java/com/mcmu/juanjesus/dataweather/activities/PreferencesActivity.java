package com.mcmu.juanjesus.dataweather.activities;

import android.app.Activity;
import android.os.Bundle;

public class PreferencesActivity extends Activity {

    //region Activity lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new PreferencesFragment())
                .commit();
    }
    //endregion Activity lifecycle
}
