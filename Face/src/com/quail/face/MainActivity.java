package com.quail.face;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;

import com.actionbarsherlock.app.SherlockListActivity;

public class MainActivity extends SherlockListActivity implements
        OnItemClickListener
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setListAdapter(new SimpleAdapter(this, getData(),
                android.R.layout.simple_list_item_1,
                new String[] { "filename" }, new int[] { android.R.id.text1 }));
        getListView().setOnItemClickListener(this);
    }

    private List<Map<String, Object>> getData()
    {
        FaceApplication fa = (FaceApplication) getApplication();
        List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        Map<String, Object> takeRow = new HashMap<String, Object>();
        takeRow.put("filename", "Take new");
        data.add(takeRow);
        
        for (String name : fa.getImageFM().getImagePaths())
        {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("filename", name);

            data.add(map);
        }

        return data;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id)
    {
        if (position == 0)
            startActivity(new Intent(this, TakeActivity.class));
    }
}