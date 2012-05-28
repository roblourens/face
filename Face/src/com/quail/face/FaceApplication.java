package com.quail.face;

import android.app.Application;
import android.util.Log;

public class FaceApplication extends Application
{
    private ImageFileManager imageFM;

    @Override
    public void onCreate()
    {
        super.onCreate();
        log("onCreate");
        
        imageFM = new ImageFileManager(this);
    }

    public ImageFileManager getImageFM()
    {
        return imageFM;
    }

    private void log(String msg)
    {
        Log.d(getClass().toString(), msg);
    }
}
