package com.quail.face.activity;

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
import com.quail.face.FaceApplication;
import com.quail.face.GridItemListener;
import com.quail.face.ImageAdapter;
import com.quail.face.ImageFileManager;
import com.quail.face.R;

public class MainActivity extends SherlockActivity
{
    private ImageAdapter adapter;
    private int curPerson;

    private final int NEW_PERSON_ID = 1000;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        int lastPersonId = getFaceApplication().getPrefsManager()
                .getLastPerson();
        int numPersons = getFaceApplication().getImageFM().numberOfPersons();

        // default to 1 if something has gone wrong
        if (lastPersonId <= 0 || lastPersonId > numPersons)
            lastPersonId = 1;

        adapter = new ImageAdapter(this, lastPersonId);
        curPerson = lastPersonId;

        GridView gv = (GridView) findViewById(R.id.gridView);
        gv.setAdapter(adapter);
        GridItemListener itemListener = new GridItemListener(adapter,
                getFaceApplication().getImageFM(), this);
        gv.setOnItemClickListener(itemListener);
        gv.setOnItemLongClickListener(itemListener);
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

        // Add persons drop-down submenu
        SubMenu submenu = menu.addSubMenu(Menu.NONE, Menu.NONE, 1, "");

        // Add entry for each person, 1-indexed
        int numPersons = getFaceApplication().getImageFM().numberOfPersons();
        for (int i = 1; i <= numPersons; i++)
            submenu.add(Menu.NONE, i, Menu.NONE, "Person " + i);

        // Add 'Add new person' item as last entry
        submenu.add(Menu.NONE, NEW_PERSON_ID, Menu.NONE, "Add new person");

        // Add 'Delete person' item TODO temporary, move to settings
        submenu.add(Menu.NONE, NEW_PERSON_ID + 1, Menu.NONE, "Delete person");

        MenuItem submenuItem = submenu.getItem();
        submenuItem.setIcon(R.drawable.ic_menu_more);
        submenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int numPeople = getFaceApplication().getImageFM().numberOfPersons();

        if (item.getItemId() == R.id.menu_camera)
        {
            Intent i = new Intent(this, TakeActivity.class);
            i.putExtra(TakeActivity.PERSON_ID_KEY, curPerson);
            startActivity(i);
            return true;
        }
        else if (item.getItemId() == R.id.menu_export)
        {
            Intent i = new Intent(this, ExportActivity.class);
            i.putExtra(ExportActivity.PERSON_ID_KEY, curPerson);
            startActivity(i);
        }
        else if (item.getItemId() == R.id.menu_prefs)
        {

        }
        else if (item.getItemId() == NEW_PERSON_ID)
        {
            // add new person
            getFaceApplication().getImageFM().addPerson();
            invalidateOptionsMenu();
            Toast.makeText(this, "New person added", 3).show();
            return true;
        }
        else if (item.getItemId() == NEW_PERSON_ID + 1)
        {
            // delete last person
            ImageFileManager imageFM = getFaceApplication().getImageFM();
            int lastPersonId = imageFM.numberOfPersons();
            if (lastPersonId > 1)
            {
                getFaceApplication().getImageFM().removePerson(lastPersonId);
                invalidateOptionsMenu();
                if (lastPersonId == curPerson)
                    switchToPerson(lastPersonId - 1);
            }
        }
        else if (item.getItemId() > 0 && item.getItemId() <= numPeople)
        {
            switchToPerson(item.getItemId());
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

    private void switchToPerson(int id)
    {
        curPerson = id;

        // Save this id in prefs as the last viewed person id
        getFaceApplication().getPrefsManager().setLastPerson(curPerson);

        // Set the person id in the ImageAdapter for the GridView and refresh
        adapter.setPersonIdAndRefresh(id);
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