package com.quail.face.activity;

import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Window;
import com.quail.face.CameraPreview;
import com.quail.face.R;
import com.quail.face.ShutterHandler;

public class TakeActivity extends SherlockActivity
{
    private CameraPreview cameraPreview;
    private ShutterHandler shutterHandler;

    public static final String PERSON_ID_KEY = "person_id";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // make fullscreen, hide crap
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.take);

        // get person id from Intent
        int id = getIntent().getIntExtra(PERSON_ID_KEY, 0);

        cameraPreview = (CameraPreview) findViewById(R.id.cameraPreview);
        cameraPreview.setBackgroundResource(R.drawable.bg);
        shutterHandler = new ShutterHandler(this, id);
        ((Button) findViewById(R.id.takeButton))
                .setOnClickListener(shutterHandler);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        logd("onResume");

        int camId = getFrontFacingCameraIdIfAvailable();
        Camera camera = camId != -1 ? Camera.open(camId) : Camera.open();
        cameraPreview.setCamera(camera, camId);
        shutterHandler.setCamera(camera);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        logd("onPause");

        cameraPreview.releaseCamera();
        shutterHandler.setCamera(null);
    }

    private int getFrontFacingCameraIdIfAvailable()
    {
        // Find front-facing camera id
        for (int camId = 0; camId < Camera.getNumberOfCameras(); camId++)
        {
            // wtf language is this again?
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(camId, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
            {
                return camId;
            }
        }

        return -1;
    }

    private void logd(String msg)
    {
        Log.d("TakeActivity", msg);
    }
}