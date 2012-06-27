package com.quail.face.activity;

import java.util.Calendar;

import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.android.alarmclock.DigitalClock;
import com.quail.face.FaceApplication;
import com.quail.face.PrefsManager;
import com.quail.face.R;

public class SettingsActivity extends SherlockPreferenceActivity
{
    private PrefsManager pm;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        setContentView(R.layout.pref_layout);

        pm = ((FaceApplication) getApplication()).getPrefsManager();

        DigitalClock digitalClock = (DigitalClock) findViewById(R.id.clock);
        // set the alarm text
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, pm.getReminderHour());
        c.set(Calendar.MINUTE, pm.getReminderMin());
        digitalClock.updateTime(c);

        // manage the reminder check manually, since it isn't a Preference
        CheckBox cb = (CheckBox) findViewById(R.id.reminder_check);
        cb.setChecked(pm.getReminderEnabled());
        cb.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked)
            {
                pm.setReminderEnabled(isChecked);
            }
        });
    }

    private void log(String msg)
    {
        Log.d("SettingsActivity", msg);
    }
}