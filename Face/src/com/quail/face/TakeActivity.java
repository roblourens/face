package com.quail.face;

import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;

public class TakeActivity extends SherlockFragmentActivity
{
    private CameraPreview cameraPreview;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
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
