package com.quail.face;

import android.os.Bundle;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class TakeActivity extends SherlockFragmentActivity
{
    private CameraPreview cameraPreview;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.take);

        cameraPreview = (CameraPreview) findViewById(R.id.cameraPreview);
        ((Button) findViewById(R.id.takeButton))
                .setOnClickListener(new ShutterHandler(this, cameraPreview
                        .getCamera()));
    }
    /*
     * private void log(String msg) { Log.d(this.getClass().toString(), msg); }
     */
}
