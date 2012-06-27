package com.quail.face;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PrefsManager
{
    private static final String LAST_PERSON_KEY = "last_person";
    private static final String LAST_RATE_KEY = "last_rate";
    private static final String REMINDER_HOUR_KEY = "reminder_hour";
    private static final String REMINDER_MIN_KEY = "reminder_min";
    private static final String REMINDER_KEY = "reminder";

    // same as used in xml.prefs
    public static final String SHUTTER_SOUND_KEY = "shutter_sound";
    public static final String ADD_TO_CAMERA_ROLL_KEY = "camera_roll";

    private Context c;

    public PrefsManager(Context c)
    {
        this.c = c;
    }

    public int getLastPerson()
    {
        SharedPreferences settings = getPrefs();
        return settings.getInt(LAST_PERSON_KEY, 0);
    }

    /**
     * Set the id of the last person viewed, so the page can be restored next
     * time
     * 
     * @param lastId
     *            The id of the last person viewed
     * @return returns what editor.commit() returns
     */
    public boolean setLastPerson(int lastId)
    {
        Editor editor = getEditor();
        editor.putInt(LAST_PERSON_KEY, lastId);
        return editor.commit();
    }

    // faces/sec
    public int getLastRate()
    {
        SharedPreferences settings = getPrefs();
        return settings.getInt(LAST_RATE_KEY, 4);
    }

    public boolean setLastRate(int lastRate)
    {
        Editor editor = getEditor();
        editor.putInt(LAST_RATE_KEY, lastRate);
        return editor.commit();
    }

    public boolean makeShutterSound()
    {
        return getPrefs().getBoolean(SHUTTER_SOUND_KEY, false);
    }

    public boolean setShutterSound(boolean makeSound)
    {
        Editor editor = getEditor();
        editor.putBoolean(LAST_RATE_KEY, makeSound);
        return editor.commit();
    }
    
    public boolean getReminderEnabled()
    {
        return getPrefs().getBoolean(REMINDER_KEY, false);
    }
    
    public boolean setReminderEnabled(boolean reminderEnabled)
    {
        Editor editor = getEditor();
        editor.putBoolean(REMINDER_KEY, reminderEnabled);
        return editor.commit();
    }

    public int getReminderHour()
    {
        return getPrefs().getInt(REMINDER_HOUR_KEY, 12);
    }

    public boolean setReminderHour(int hour)
    {
        Editor editor = getEditor();
        editor.putInt(REMINDER_HOUR_KEY, hour);
        return editor.commit();
    }

    public int getReminderMin()
    {
        return getPrefs().getInt(REMINDER_MIN_KEY, 30);
    }

    public boolean setReminderMin(int min)
    {
        Editor editor = getEditor();
        editor.putInt(REMINDER_MIN_KEY, min);
        return editor.commit();
    }

    private SharedPreferences getPrefs()
    {
        return PreferenceManager.getDefaultSharedPreferences(c);
    }

    private Editor getEditor()
    {
        return getPrefs().edit();
    }
}