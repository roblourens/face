package com.quail.face;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager
{
    public static final String PREFS_NAME = "com.quail.face.Prefs";
    public static final String LAST_PERSON_KEY = "last_person";

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
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(LAST_PERSON_KEY, lastId);
        return editor.commit();
    }

    private SharedPreferences getPrefs()
    {
        return c.getSharedPreferences(PREFS_NAME, 0);
    }
}
