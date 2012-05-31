package com.quail.face;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class RowsAdapter extends BaseAdapter
{
    public RowsAdapter()
    {
    }

    @Override
    public int getCount()
    {
        return 3;
    }

    @Override
    public Object getItem(int position)
    {
        return null;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) parent.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        HorizontalListView hlv = (HorizontalListView) inflater.inflate(
                R.layout.horizontal_list_row, null);
        hlv.setAdapter(new ImageAdapter());
        return hlv;
    }
}