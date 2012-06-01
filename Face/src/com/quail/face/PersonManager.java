package com.quail.face;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;

import com.actionbarsherlock.app.ActionBar.OnNavigationListener;

public class PersonManager implements SpinnerAdapter, OnNavigationListener
{
    private String[] persons = new String[] { "Rob", "John" };

    @Override
    public int getCount()
    {
        return persons.length;
    }

    @Override
    public Object getItem(int position)
    {
        return persons[position];
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public int getItemViewType(int arg0)
    {
        return 0;
    }

    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2)
    {
        return null;
    }

    @Override
    public int getViewTypeCount()
    {
        return 1;
    }

    @Override
    public boolean hasStableIds()
    {
        return false;
    }

    @Override
    public boolean isEmpty()
    {
        return persons.length == 0;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer)
    {
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer)
    {
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        return null;
    }

    @Override
    public boolean onNavigationItemSelected(int arg0, long arg1)
    {
        // TODO Auto-generated method stub
        return false;
    }
}
