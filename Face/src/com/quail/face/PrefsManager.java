package com.quail.face;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PrefsManager
{
    private static final String PREFS_NAME = "com.quail.face.Prefs";
    private static final String LAST_PERSON_KEY = "last_person";
    private static final String LAST_RATE_KEY = "last_rate";

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
        SharedPreferences settings = getPrefs();
        Editor editor = settings.edit();
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
        SharedPreferences settings = getPrefs();
        Editor editor = settings.edit();
        editor.putInt(LAST_RATE_KEY, lastRate);
        return editor.commit();
    }

    private SharedPreferences getPrefs()
    {
        return c.getSharedPreferences(PREFS_NAME, 1);
    }
}