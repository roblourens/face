package com.quail.face;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
        GridView gv = (GridView) findViewById(R.id.gridView);
        gv.setAdapter(adapter);
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
        if (item.getItemId() == R.id.menu_camera)
        {
            startActivity(new Intent(this, TakeActivity.class));
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