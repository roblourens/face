package com.quail.face;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class GridItemListener implements OnItemClickListener,
        OnItemLongClickListener
{
    private ImageAdapter adapter;
    private ImageFileManager imageFM;
    private MainActivity mainActivity;

    public GridItemListener(ImageAdapter adapter, ImageFileManager imageFM,
            MainActivity mainActivity)
    {
        this.adapter = adapter;
        this.imageFM = imageFM;
        this.mainActivity = mainActivity;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
            long id)
    {
        String clickedPath = adapter.getItem(position);
        Intent i = new Intent(view.getContext(), SingleImageActivity.class);
        i.putExtra(SingleImageActivity.IMAGE_PATH_KEY, clickedPath);
        view.getContext().startActivity(i);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, final View view,
            int position, long id)
    {
        final String clickedPath = adapter.getItem(position);
        final CharSequence[] items = { "Delete" };

        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setItems(items, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int item)
            {
                dialog.dismiss();
                showConfirmDialog(view.getContext(), clickedPath);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

        return true;
    }

    private void showConfirmDialog(Context context, final String clickedPath)
    {
        // show confirm dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Delete this picture?")
                .setTitle("Delete")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        imageFM.deleteImage(clickedPath);
                        dialog.dismiss();
                        mainActivity.refresh();
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                dialog.dismiss();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}