package com.quail.face;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockActivity
{
    private ImageAdapter adapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        adapter = new ImageAdapter(this);
        GridView gv = (GridView) findViewById(R.id.pictureGridView);
        gv.setAdapter(adapter);

        // Set button to launch TakeActivity
        ((Button) findViewById(R.id.takePictureButton))
                .setOnClickListener(new OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        startActivity(new Intent(MainActivity.this,
                                TakeActivity.class));
                    }
                });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        log("onResume");
        refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.menu_refresh)
        {
            refresh();
            return true;
        }

        return false;
    }

    public void refresh()
    {
        if (sdCardCheck())
        {
            adapter.refresh();
        }
    }

    private boolean sdCardCheck()
    {
        boolean available = getFaceApplication().getImageFM()
                .sdCardIsAvailable();
        if (!available)
            Toast.makeText(this, "SD card is not available", 4).show();

        return available;
    }

    private FaceApplication getFaceApplication()
    {
        return (FaceApplication) getApplication();
    }
    
    private void log(String msg)
    {
        Log.d("MainActivity", msg);
    }
}