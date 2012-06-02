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
import com.actionbarsherlock.view.SubMenu;

public class MainActivity extends SherlockActivity
{
    private ImageAdapter adapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        adapter = new ImageAdapter(this, 0);
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

        SubMenu submenu = menu.addSubMenu(Menu.NONE, 38, 1, "");
        submenu.add("Person 1");
        submenu.add("Add new person");

        MenuItem submenuItem = submenu.getItem();
        submenu.setIcon(R.drawable.ic_menu_more);
        submenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return super.onCreateOptionsMenu(menu);
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