package com.quail.face;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    private List<String> imagePaths = new ArrayList<String>();

    public ImageAdapter(Activity a)
    {
        this.a = a;
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
        log("getView");
        ImageView gridImage;
        // reuse old view if able
        if (convertView != null && convertView instanceof ImageView)
            gridImage = (ImageView) convertView;
        else
            gridImage = new ImageView(a);

        gridImage.setLayoutParams(new GridView.LayoutParams(85, 85));
        gridImage.setScaleType(ImageView.ScaleType.CENTER_CROP);

        Bitmap bm = BitmapFactory.decodeFile(imagePaths.get(position));
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
            imagePaths = ((FaceApplication) a.getApplication()).getImageFM()
                    .getImagePaths();
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
