package com.quail.face;

import android.hardware.Camera;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class ShutterHandler implements OnClickListener, Camera.ShutterCallback,
        Camera.PictureCallback
{
    private Camera camera;

    public ShutterHandler(Camera c)
    {
        this.camera = c;
    }

    @Override
    public void onClick(View v)
    {
        camera.takePicture(this, null, this);
    }

    @Override
    public void onShutter()
    {
        log("onShutter");
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera)
    {
        log("onPictureTaken");
        camera.startPreview();
    }
    
    private void log(String msg)
    {
        Log.d(this.getClass().toString(), msg);
    }
}
