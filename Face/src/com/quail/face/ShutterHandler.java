package com.quail.face;

import android.app.Activity;
import android.hardware.Camera;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class ShutterHandler implements OnClickListener, Camera.ShutterCallback,
        Camera.PictureCallback
{
    private Activity a;
    private Camera camera;
    private int id;

    public ShutterHandler(Activity a, int id)
    {
        this.a = a;
        this.id = id;
    }

    public void setCamera(Camera camera)
    {
        this.camera = camera;
    }

    @Override
    public void onClick(View v)
    {
        if (camera != null)
            camera.takePicture(this, null, this);
        else
            log("null camera");
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

        ((FaceApplication) a.getApplication()).getImageFM().saveImage(data, id);
    }

    private void log(String msg)
    {
        Log.d("ShutterHandler", msg);
    }
}
