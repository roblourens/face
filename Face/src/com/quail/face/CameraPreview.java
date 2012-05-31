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
import android.widget.FrameLayout;

class CameraPreview extends SurfaceView implements SurfaceHolder.Callback
{
    private SurfaceHolder holder;
    private Camera camera;
    private int camId;

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

        camera = getFrontFacingCameraIfAvailable();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder)
    {
        if (camera == null)
            camera = getFrontFacingCameraIfAvailable();

        try
        {
            camera.setPreviewDisplay(holder);
        }
        catch (IOException exception)
        {
            camera.release();
            camera = null;
            log("setPreviewDisplay failed");
        }
    }

    // Destroyed when View/Activity is destroyed, so onPause implementation
    // shouldn't be needed
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        camera.stopPreview();
        camera.release();
        camera = null;
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
            log("Measuring without camera");
            setMeasuredDimension(maxW, maxH);
        }
        else
        {
            Camera.Parameters parameters = camera.getParameters();

            List<Size> supportedSizes = rotateSizes(parameters
                    .getSupportedPreviewSizes());
            Size optimalSize = getSizeWithClosestPriceIsRightWidth(
                    supportedSizes, maxW);
            log("Using preview size: " + optimalSize.width + ", "
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
        log("surfaceChanged");
        Camera.Parameters parameters = camera.getParameters();

        // Should return the same size found in onMeasure
        Size optimalSize = getSizeWithClosestPriceIsRightWidth(
                rotateSizes(parameters.getSupportedPreviewSizes()), w);

        // rotated from portrait w and h
        parameters.setPreviewSize(optimalSize.height, optimalSize.width);

        // force portrait orientation
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(camId, info);
        log("Cam orientation: " + info.orientation);
        parameters.setRotation(info.orientation);

        camera.setParameters(parameters);

        // Rotate preview display to portrait
        camera.setDisplayOrientation(360 - info.orientation);

        camera.startPreview();
    }

    private Camera getFrontFacingCameraIfAvailable()
    {
        // Find front-facing camera id
        for (int camId = 0; camId < Camera.getNumberOfCameras(); camId++)
        {
            // wtf language is this again?
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(camId, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
            {
                this.camId = camId;
                return Camera.open(camId);
            }
        }

        return Camera.open();
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

    private void log(String msg)
    {
        Log.d(this.getClass().toString(), msg);
    }
}