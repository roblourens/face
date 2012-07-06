package com.quail.face.activity;

import java.util.Calendar;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TimePicker;

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

        ListView clockPrefListView = ((ListView) findViewById(R.id.clockPrefList));
        clockPrefListView.setAdapter(new ClockPrefAdapter());
        clockPrefListView.setOnItemClickListener(new OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> l, View arg1v, int position,
                    long id)
            {
                log("time clicked");

                OnTimeSetListener timeSetListener = new OnTimeSetListener()
                {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                            int minute)
                    {
                        log("onTimeSet");
                        pm.setReminderHour(hourOfDay);
                        pm.setReminderMin(minute);
                        syncClockText();
                    }
                };
                TimePickerDialog dialog = new TimePickerDialog(
                        SettingsActivity.this, timeSetListener, pm
                                .getReminderHour(), pm.getReminderMin(), false);
                dialog.show();
            }
        });
    }

    private void syncClockText()
    {
        syncClockText(null);
    }

    private void syncClockText(View v)
    {
        DigitalClock digitalClock;
        if (v == null)
            digitalClock = (DigitalClock) findViewById(R.id.clock);
        else
            digitalClock = (DigitalClock) v.findViewById(R.id.clock);
        // set the alarm text
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, pm.getReminderHour());
        c.set(Calendar.MINUTE, pm.getReminderMin());
        digitalClock.updateTime(c);
    }

    private class ClockPrefAdapter extends BaseAdapter
    {
        @Override
        public int getCount()
        {
            return 1;
        }

        @Override
        public Object getItem(int position)
        {
            return null;
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View v = getLayoutInflater().inflate(R.layout.clock_pref, null);
            syncClockText(v);

            // manage the reminder check manually, since it isn't a Preference
            CheckBox cb = (CheckBox) v.findViewById(R.id.reminder_check);
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

            return v;
        }
    }

    private void log(String msg)
    {
        Log.d("SettingsActivity", msg);
    }
}