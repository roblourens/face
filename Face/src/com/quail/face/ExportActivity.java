package com.quail.face;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;

public class ExportActivity extends SherlockActivity implements
        OnSeekBarChangeListener
{
    private int personId;
    private int rate;

    private PrefsManager pm;
    private SeekBar rateSelector;
    private TextView selectedRateValue;

    public static final String PERSON_ID_KEY = "person_id";

    // faces/sec
    private static final int MIN_RATE = 3;
    private static final int MAX_RATE = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        personId = getIntent().getIntExtra(PERSON_ID_KEY, 1);

        // load export.xml and find UI components
        setContentView(R.layout.export);
        rateSelector = (SeekBar) findViewById(R.id.rateSelector);
        rateSelector.setOnSeekBarChangeListener(this);
        rateSelector.setMax(MAX_RATE - MIN_RATE);
        selectedRateValue = (TextView) findViewById(R.id.selectedRateValue);

        // use whichever rate the user used last
        pm = ((FaceApplication) getApplication()).getPrefsManager();
        rate = pm.getLastRate();
        setRate(rate, false);
    }

    public void exportButtonClicked(View view)
    {
        log("exportButtonClicked");
    }

    private void setRate(int newRate, boolean fromUser)
    {
        int curProgress = rateSelector.getProgress();

        // move progress bar if needed
        if (!fromUser && newRate != curProgress)
            rateSelector.setProgress(newRate - MIN_RATE);

        ((TextView) findViewById(R.id.selectedRateValue)).setText(newRate + "");

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
}
