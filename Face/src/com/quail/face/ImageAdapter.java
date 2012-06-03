package com.quail.face;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter
{
    private Activity a;
    private ImageFileManager imageFM;
    private List<String> imagePaths = new ArrayList<String>();
    private int personId;

    public ImageAdapter(Activity a, int personId)
    {
        this.a = a;
        this.personId = personId;
        this.imageFM = ((FaceApplication) a.getApplication()).getImageFM();

        refresh();
    }
    
    public void setPersonIdAndRefresh(int personId)
    {
        this.personId = personId;
        refresh();
    }

    public void refresh()
    {
        new RefreshTask().execute();
    }

    @Override
    public int getCount()
    {
        return imagePaths.size();
    }

    @Override
    public Object getItem(int position)
    {
        return imagePaths.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ImageView gridImage;
        // reuse old view if able
        if (convertView != null && convertView instanceof ImageView)
            gridImage = (ImageView) convertView;
        else
            gridImage = new ImageView(a);

        // can't get the 'normal' way of doing this to work right, so need
        // to
        // just figure out the exact sizes manually
        // gotta hate Android
        @SuppressWarnings("deprecation")
        int screenW = a.getWindowManager().getDefaultDisplay().getWidth(); // px

        // DPI/dp
        double scale = a.getResources().getDisplayMetrics().density;

        // these are in px, converted from dp by * scale
        int n = 4; // # columns
        double margin = 6 * scale; // outer, comes from listSelector width
        double spacing = 6 * scale; // inner
        int side = (int) ((screenW - margin * 2 - spacing * (n - 1)) / n);
        gridImage.setLayoutParams(new GridView.LayoutParams(side, side));
        gridImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Bitmap bm = imageFM.getThumbnailForImage(personId,
                imagePaths.get(position));
        gridImage.setImageBitmap(bm);

        return gridImage;
    }

    private void log(String msg)
    {
        Log.d("ImageAdapter", msg);
    }

    private class RefreshTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params)
        {
            imagePaths = imageFM.getImagePathsForPerson(personId);
            log("Refreshed and found " + imagePaths.size() + " images");

            // Sort them backwards (new/large numbers -> old/small numbers)
            Collections.sort(imagePaths, new Comparator<String>()
            {
                @Override
                public int compare(String str1, String str2)
                {
                    return -1 * str1.compareTo(str2);
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            ImageAdapter.this.notifyDataSetChanged();
        }
    }
}