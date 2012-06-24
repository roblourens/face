package com.quail.face.activity;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.quail.face.FaceApplication;
import com.quail.face.PrefsManager;
import com.quail.face.R;

public class SettingsActivity extends SherlockPreferenceActivity
{
    private PrefsManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);

        pm = ((FaceApplication) getApplication()).getPrefsManager();
        log("shutter? " + pm.makeShutterSound());
    }

    private void log(String msg)
    {
        Log.d("SettingsActivity", msg);
    }
}