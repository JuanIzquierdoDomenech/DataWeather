package com.mcmu.juanjesus.dataweather.activities;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.mcmu.juanjesus.dataweather.R;

public class PreferencesFragment extends PreferenceFragment {

    //region Activity lifecycle

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    //endregion Activity lifecycle
}
