package com.quail.face;

import android.app.Application;
import android.util.Log;

public class FaceApplication extends Application
{
    private ImageFileManager imageFM;
    private PrefsManager prefsManager;

    @Override
    public void onCreate()
    {
        super.onCreate();
        log("onCreate");

        imageFM = new ImageFileManager(this);
        prefsManager = new PrefsManager(this);
    }

    public ImageFileManager getImageFM()
    {
        return imageFM;
    }

    public PrefsManager getPrefsManager()
    {
        return prefsManager;
    }

    private void log(String msg)
    {
        Log.d(getClass().toString(), msg);
    }
}
