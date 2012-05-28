package com.quail.face;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class CameraPreview extends SurfaceView implements SurfaceHolder.Callback
{
    private SurfaceHolder holder;
    private Camera camera;

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
        } catch (IOException exception)
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

    private Size getOptimalPreviewSize(List<Size> supportedSizes, int w, int h)
    {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;
        if (supportedSizes == null)
            return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : supportedSizes)
        {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff)
            {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find one matching the aspect ratio, ignore that
        // requirement
        if (optimalSize == null)
        {
            minDiff = Double.MAX_VALUE;
            for (Size size : supportedSizes)
            {
                if (Math.abs(size.height - targetHeight) < minDiff)
                {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h)
    {
        Camera.Parameters parameters = camera.getParameters();

        List<Size> supportedSizes = parameters.getSupportedPreviewSizes();
        Size optimalSize = getOptimalPreviewSize(supportedSizes, w, h);
        log("Using preview size: " + optimalSize.width + ", "
                + optimalSize.height);
        parameters.setPreviewSize(optimalSize.width, optimalSize.height);

        camera.setParameters(parameters);
        camera.setDisplayOrientation(90);
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
                return Camera.open(camId);
        }

        return Camera.open();
    }

    private void log(String msg)
    {
        Log.d(this.getClass().toString(), msg);
    }
}