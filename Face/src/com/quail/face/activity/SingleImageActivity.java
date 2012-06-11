package com.quail.face.activity;

import java.util.Date;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.quail.face.FaceApplication;
import com.quail.face.ImageFileManager;
import com.quail.face.R;

public class SingleImageActivity extends SherlockActivity
{
    public static final String IMAGE_PATH_KEY = "image_path";

    private String imagePath;
    private ImageFileManager imageFM;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_image);

        imagePath = getIntent().getStringExtra(IMAGE_PATH_KEY);
        ImageView imageView = (ImageView) findViewById(R.id.single_image);
        imageView.setImageBitmap(BitmapFactory.decodeFile(imagePath));

        imageFM = ((FaceApplication) getApplication()).getImageFM();

        Date imageDate = new Date(imageFM.getDateForImage(imagePath));
        CharSequence formattedDate = DateFormat.format("MMM dd, yyyy, h:mm aa",
                imageDate);
        ((TextView) findViewById(R.id.image_date)).setText(formattedDate);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getSupportMenuInflater().inflate(R.menu.single_image_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.delete_image)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Delete this picture?")
                    .setTitle("Delete")
                    .setCancelable(true)
                    .setPositiveButton("Yes",
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog,
                                        int id)
                                {
                                    imageFM.deleteImage(imagePath);
                                    dialog.dismiss();
                                    finish();
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog,
                                        int id)
                                {
                                    dialog.dismiss();
                                }
                            });
            AlertDialog alert = builder.create();
            alert.show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}