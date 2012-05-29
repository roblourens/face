package com.quail.face;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class ImageFileManager
{
    private final String extGalleryPath;
    private final String extNonGalleryPath;
    private final String extTmpPath;

    private Context c;

    public ImageFileManager(Context c)
    {
        this.c = c;

        File picturesDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File extGalleryPathFile = new File(picturesDir, "face/");
        extGalleryPathFile.mkdirs();
        extGalleryPath = extGalleryPathFile.getAbsolutePath();

        File extNonGalleryPathFile = new File(c.getExternalFilesDir(null),
                "images/");
        if (extNonGalleryPathFile.mkdirs())
        {
            // dirs didn't already exist, so make the nomedia file.
            // prevents Android media scanner from adding this to the gallery
            File noMediaFile = new File(extNonGalleryPathFile, ".nomedia");
            try
            {
                noMediaFile.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        extNonGalleryPath = extNonGalleryPathFile.getAbsolutePath();

        File extTmpPathFile = new File(c.getExternalFilesDir(null), "tmp/");
        extTmpPathFile.mkdirs();
        extTmpPath = extTmpPathFile.getAbsolutePath();
    }

    /**
     * Checks the mounting state of the SD card
     * 
     * @return True if the card can be written to and read from. False
     *         otherwise.
     */
    public boolean sdCardIsAvailable()
    {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    /**
     * Checks all image storage locations, regardless of preferences
     * 
     * @return A list of absolute paths to all found images
     */
    public List<String> getImagePaths()
    {
        List<String> imgPaths = new ArrayList<String>();

        // find images in external storage, in the gallery
        File extNonGalleryDir = new File(extNonGalleryPath);
        for (File f : extNonGalleryDir.listFiles())
            imgPaths.add(f.getAbsolutePath());

        // find images in external storage, not in the gallery
        File extGalleryDir = new File(extGalleryPath);
        for (File f : extGalleryDir.listFiles())
            imgPaths.add(f.getAbsolutePath());

        // remove paths that are not jpgs
        Iterator<String> it = imgPaths.iterator();
        while (it.hasNext())
        {
            String path = it.next();
            if (!path.toLowerCase().endsWith(".jpg")
                    && !path.toLowerCase().endsWith(".jpeg"))
                it.remove();
        }

        return imgPaths;
    }

    public boolean saveImage(byte[] data)
    {
        // TODO prefs
        if (saveImageToExternalStorageNonGallery(data))
            log("Saved to external storage, non gallery");
        else
            return false;

        return true;
    }

    /**
     * Saves the given jpg image data to the external storage location, in the
     * gallery
     * 
     * @param data
     *            jpg image data
     * @return true if successful, false otherwise
     */
    private boolean saveImageToExternalStorageGallery(byte[] data)
    {
        File tmpFile = saveToExternalTmpFile(data);
        if (tmpFile == null)
            return false;

        String imageName = newImageFileName();
        return tmpFile.renameTo(new File(extNonGalleryPath, imageName));
    }

    /**
     * Saves the given jpg image data to the external storage location, not in
     * the gallery
     * 
     * @param data
     *            jpg image data
     * @return true if successful, false otherwise
     */
    private boolean saveImageToExternalStorageNonGallery(byte[] data)
    {
        File tmpFile = saveToExternalTmpFile(data);
        if (tmpFile == null)
            return false;

        String imageName = newImageFileName();
        return tmpFile.renameTo(new File(extNonGalleryPath, imageName));
    }

    private File saveToExternalTmpFile(byte[] data)
    {
        if (sdCardIsAvailable())
        {
            // We can read and write the media
            String randomName = new Random().nextInt() + "_"
                    + System.currentTimeMillis() + ".jpg";
            File tmpFile = new File(extTmpPath, randomName);

            OutputStream outStream;
            try
            {
                outStream = new FileOutputStream(tmpFile);
                outStream.write(data);
            }
            catch (FileNotFoundException e)
            {
                // file exists but is a directory rather than a regular file,
                // does not exist but cannot be created, or cannot be opened for
                // any other reason
                e.printStackTrace();
                return null;
            }
            catch (IOException e)
            {
                // File could not be written
                e.printStackTrace();
                return null;
            }

            try
            {
                outStream.close();
            }
            catch (IOException e)
            {
                // file has been written, ignore this problem
                e.printStackTrace();
            }
            return tmpFile;
        }
        else
            // external storage can't be written to
            return null;
    }

    private String newImageFileName()
    {
        return "face_person1_" + System.currentTimeMillis() + ".jpg";
    }

    private void log(String msg)
    {
        Log.d(this.getClass().toString(), msg);
    }
}
