package com.quail.face.activity;

import java.io.File;

import uk.co.halfninja.videokit.Videokit;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.quail.face.FaceApplication;
import com.quail.face.ImageFileManager;
import com.quail.face.ListenerVideoView;
import com.quail.face.ListenerVideoView.PlayListener;
import com.quail.face.PrefsManager;
import com.quail.face.R;

public class ExportActivity extends SherlockActivity implements
        OnSeekBarChangeListener
{
    private int personId;
    private int rate;

    private PrefsManager pm;
    private ImageFileManager imageFM;

    private SeekBar rateSelector;
    private TextView selectedRateValue;
    private ListenerVideoView videoView;

    // Whenever the video path is changed, curVideoPath must be changed for the
    // OnPreparedListener
    private String curVideoPath;

    public static final String PERSON_ID_KEY = "person_id";

    // faces/sec
    private static final int MIN_RATE = 3;
    private static final int MAX_RATE = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        personId = getIntent().getIntExtra(PERSON_ID_KEY, 1);

        pm = ((FaceApplication) getApplication()).getPrefsManager();
        imageFM = ((FaceApplication) getApplication()).getImageFM();

        // load export.xml and find UI components
        setContentView(R.layout.export);
        rateSelector = (SeekBar) findViewById(R.id.rateSelector);
        rateSelector.setOnSeekBarChangeListener(this);
        rateSelector.setMax(MAX_RATE - MIN_RATE);
        selectedRateValue = (TextView) findViewById(R.id.selectedRateValue);
        videoView = (ListenerVideoView) findViewById(R.id.faceVideoView);
        videoView.setMediaController(new MediaController(this));

        // Whenever it starts playing, clear the background drawable, which is
        // the thumbnail
        videoView.setPlayListener(new PlayListener()
        {
            @Override
            public void onPlay(ListenerVideoView listenerVideoView)
            {
                listenerVideoView.setBackgroundDrawable(null);
            }
        });

        // set videoView to show the last exported video if available
        String lastVideoPath = imageFM.getLastVideoPath(personId);
        if (lastVideoPath != null)
        {
            log("Setting up VideoView with last video path " + lastVideoPath);
            videoView.setVideoPath(lastVideoPath);
            // makeThumbnail(lastVideoPath);
            curVideoPath = lastVideoPath;
        }

        // If the thumbnail is added before VideoView is prepared, it will
        // change size after appearing
        videoView.setOnPreparedListener(new OnPreparedListener()
        {
            @Override
            public void onPrepared(MediaPlayer mp)
            {
                makeThumbnail(curVideoPath);
            }
        });

        // use whichever rate the user used last
        rate = pm.getLastRate();
        setRate(rate, false);
    }

    public void exportButtonClicked(View view)
    {
        log("exportButtonClicked");
        new MakeMovieTask().execute();
    }

    /**
     * Create a thumbnail for the video, and set it as the background, which
     * will be drawn on top of the VideoView
     * 
     * @param videoPath
     */
    private void makeThumbnail(String videoPath)
    {
        Bitmap videoThumbnail = ThumbnailUtils.createVideoThumbnail(videoPath,
                MediaStore.Images.Thumbnails.MINI_KIND);
        videoView.setBackgroundDrawable(new BitmapDrawable(getResources(),
                videoThumbnail));
    }

    private void setRate(int newRate, boolean fromUser)
    {
        int curProgress = rateSelector.getProgress();

        // move progress bar if needed
        if (!fromUser && newRate != curProgress)
            rateSelector.setProgress(newRate - MIN_RATE);

        selectedRateValue.setText(newRate + "");

        rate = newRate;
        pm.setLastRate(rate);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser)
    {
        // adjust progress value
        setRate(progress + MIN_RATE, fromUser);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar)
    {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar)
    {
    }

    private void log(String msg)
    {
        Log.d("ExportActivity", msg);
    }

    private class MakeMovieTask extends AsyncTask<Void, Void, String>
    {
        private ProgressDialog dialog;
        
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            dialog = ProgressDialog.show(ExportActivity.this,
                    "", "Making movie...", true);
        }

        @Override
        protected String doInBackground(Void... params)
        {
            Videokit vk = new Videokit();
            ImageFileManager imageFM = ((FaceApplication) getApplication())
                    .getImageFM();
            imageFM.reorderImages(personId);
            String input = new File(imageFM.getPersonImagesDir(personId),
                    "%04d.jpg").getAbsolutePath();
            String videoName = "faces_" + System.currentTimeMillis() + ".mp4";
            String output = new File(imageFM.getPersonVideoDir(personId),
                    videoName).getAbsolutePath();

            vk.run(new String[] { "ffmpeg", "-r", rate + "", "-i", input, "-b",
                    "16000", "-vcodec", "mpeg4", "-y", output });

            return output;
        }

        @Override
        protected void onPostExecute(String videoPath)
        {
            super.onPostExecute(videoPath);

            log("Exported with path " + videoPath);
            curVideoPath = videoPath;
            videoView.setVideoPath(videoPath);
            
            dialog.dismiss();
        }
    }
}