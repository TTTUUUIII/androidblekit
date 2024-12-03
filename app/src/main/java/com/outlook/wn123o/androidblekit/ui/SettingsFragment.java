package com.outlook.wn123o.androidblekit.ui;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.outlook.wn123o.androidblekit.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }
}