package com.quail.face;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class ListenerVideoView extends VideoView
{
    private PlayListener playListener;

    public ListenerVideoView(Context context)
    {
        super(context);
    }

    public ListenerVideoView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public ListenerVideoView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public void setPlayListener(PlayListener playListener)
    {
        this.playListener = playListener;
    }

    @Override
    public void start()
    {
        super.start();

        if (playListener != null)
            playListener.onPlay(this);
    }

    public interface PlayListener
    {
        public void onPlay(ListenerVideoView listenerVideoView);
    }
}
