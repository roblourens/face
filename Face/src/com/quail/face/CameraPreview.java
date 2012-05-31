package com.quail.face;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

class CameraPreview extends SurfaceView implements SurfaceHolder.Callback
{
    private SurfaceHolder holder;
    private Camera camera;
    private int camId;
    private boolean previewIsRunning = false;

    CameraPreview(Context context)
    {
        super(context);
        init();
    }

    public CameraPreview(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    @SuppressWarnings("deprecation")
    // setType really is apparently needed
    private void init()
    {
        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void setCamera(Camera camera, int camId)
    {
        this.camera = camera;
        this.camId = camId;
    }

    public void releaseCamera()
    {
        if (camera != null)
        {
            stopPreview();
            camera.release();
            camera = null;
        }
    }

    public void surfaceCreated(SurfaceHolder holder)
    {
        if (camera == null)
            loge("Camera is null!");

        try
        {
            camera.setPreviewDisplay(holder);
        }
        catch (IOException exception)
        {
            camera.release();
            camera = null;
            logd("setPreviewDisplay failed");
        }
    }

    // Destroyed when View/Activity is destroyed, so onPause implementation
    // shouldn't be needed
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        releaseCamera();
    }

    public Camera getCamera()
    {
        return camera;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int maxW = View.MeasureSpec.getSize(widthMeasureSpec);
        int maxH = View.MeasureSpec.getSize(heightMeasureSpec);

        if (camera == null)
        {
            loge("Measuring without camera");
            setMeasuredDimension(maxW, maxH);
        }
        else
        {
            Camera.Parameters parameters = camera.getParameters();

            List<Size> supportedSizes = rotateSizes(parameters
                    .getSupportedPreviewSizes());
            Size optimalSize = getSizeWithClosestPriceIsRightWidth(
                    supportedSizes, maxW);
            logd("Using preview size: " + optimalSize.width + ", "
                    + optimalSize.height);

            // adjust to fill the screen
            double aspectRatio = optimalSize.height / (float) optimalSize.width;
            int chosenH = (int) (aspectRatio * maxW);

            // Set SurfaceView measured size
            setMeasuredDimension(maxW, chosenH);
        }
    }

    private Size getSizeWithClosestPriceIsRightWidth(List<Size> supportedSizes,
            int w)
    {
        if (supportedSizes == null)
            return null;

        Size closestSize = null;
        double minDiff = Double.MAX_VALUE;

        for (Size size : supportedSizes)
        {
            double diff = w - size.width;
            if (diff > 0 && diff < minDiff)
            {
                closestSize = size;
                minDiff = diff;
            }
        }

        return closestSize;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
    {
        logd("surfaceChanged");

        // if preview is running, setDisplayOrientation will fail and the other
        // stuff is unnecessary
        if (!previewIsRunning)
        {
            Camera.Parameters parameters = camera.getParameters();

            // Should return the same size found in onMeasure
            Size optimalSize = getSizeWithClosestPriceIsRightWidth(
                    rotateSizes(parameters.getSupportedPreviewSizes()), w);

            // rotated from portrait w and h
            parameters.setPreviewSize(optimalSize.height, optimalSize.width);

            // force portrait orientation
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(camId, info);
            logd("Cam orientation: " + info.orientation);
            parameters.setRotation(info.orientation);

            camera.setParameters(parameters);

            // Rotate preview display to portrait
            camera.setDisplayOrientation(360 - info.orientation);

            startPreview();
        }
    }

    private List<Size> rotateSizes(List<Size> sizes)
    {
        for (Size s : sizes)
        {
            int tmp = s.width;
            s.width = s.height;
            s.height = tmp;
        }

        return sizes;
    }

    private void startPreview()
    {
        camera.startPreview();
        previewIsRunning = true;
    }

    private void stopPreview()
    {
        camera.stopPreview();
        previewIsRunning = false;
    }

    private void logd(String msg)
    {
        Log.d("CameraPreview", msg);
    }

    private void loge(String msg)
    {
        Log.e("CameraPreview", msg);
    }
}