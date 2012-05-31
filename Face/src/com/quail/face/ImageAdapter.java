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
import android.widget.Gallery;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter
{
    private List<String> imagePaths = new ArrayList<String>();

    public ImageAdapter()
    {
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
            gridImage = new ImageView(parent.getContext());

        gridImage
                .setLayoutParams(new HorizontalListView.LayoutParams(150, 150));
        gridImage.setScaleType(ImageView.ScaleType.FIT_START);
        gridImage.setAdjustViewBounds(true);
        gridImage.setPadding(10, 10, 10, 10);

        Bitmap bm = BitmapFactory.decodeFile(imagePaths.get(position));
        // bm = Bitmap.createScaledBitmap(bm, 150, 150, true);
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
            imagePaths = ImageFileManager.getIFM().getImagePaths();
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