package com.quail.face;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class ImageFileManager
{
    // person dirs are in these, and should exist in both
    private final String extGalleryPath;
    private final String extNonGalleryPath;

    private final String extTmpPath;

    // See notes.txt
    public ImageFileManager(Context c)
    {
        File picturesDir = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File extGalleryPathFile = new File(picturesDir, "face/");
        extGalleryPathFile.mkdirs();
        extGalleryPath = extGalleryPathFile.getAbsolutePath();

        File extNonGalleryPathFile = c.getExternalFilesDir(null);
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

    public int numberOfPersons()
    {
        // should be the same number of person dirs as extNonGalleryPath
        return new File(extGalleryPath).list().length;
    }

    /**
     * Checks all image storage locations, regardless of preferences
     * 
     * @return A list of absolute paths to all found images
     */
    public List<String> getImagePathsForPerson(int id)
    {
        List<String> imgPaths = new ArrayList<String>();

        File[] checkDirs = new File[] { getPersonImagesDir(id, true),
                getPersonImagesDir(id, false) };
        for (File imageDir : checkDirs)
        {
            for (File f : imageDir.listFiles())
            {
                String fileName = f.getName().toLowerCase();
                if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
                    imgPaths.add(f.getAbsolutePath());
            }
        }

        return imgPaths;
    }

    public Bitmap getThumbnailForImage(int id, String imagePath)
    {
        String thumbFileName = "thumb_" + new File(imagePath).getName();
        File thumbFile = new File(getPersonThumbsDir(id), thumbFileName);
        if (!thumbFile.exists())
        {
            log("Thumbnail for existing image " + imagePath
                    + " not found, creating");
            makeThumbnailForImage(id, imagePath);
        }

        return BitmapFactory.decodeFile(thumbFile.getAbsolutePath());
    }

    private File getPersonDirFile(int id, boolean gallery)
    {
        File galleryDir = new File(extGalleryPath, "people/" + id + "");
        File nonGalleryDir = new File(extNonGalleryPath, "people/" + id + "");
        return gallery ? galleryDir : nonGalleryDir;
    }

    private File getPersonImagesDir(int id, boolean gallery)
    {
        File galleryImageDir = new File(getPersonDirFile(id, true), "images");
        File nonGalleryImageDir = new File(getPersonDirFile(id, false),
                "images");
        return gallery ? galleryImageDir : nonGalleryImageDir;
    }

    private File getPersonThumbsDir(int id)
    {
        return new File(getPersonDirFile(id, false), "thumbs");
    }

    public boolean saveImage(byte[] data, int id)
    {
        // TODO prefs
        saveImage(data, id, true);
        return true;
    }

    /**
     * Saves the given jpg image data to the external storage location, in the
     * gallery
     * 
     * @return true if successful, false otherwise
     */
    private boolean saveImage(byte[] data, int id, boolean gallery)
    {
        File tmpFile = saveToExternalTmpFile(data);
        if (tmpFile == null)
        {
            log("Could not save to tmp file");
            return false;
        }

        String imageName = newImageFileName();
        File imagesFile = getPersonImagesDir(id, gallery);
        return tmpFile.renameTo(new File(imagesFile, imageName))
                && saveThumbnail(data, id, imageName);
    }

    private boolean saveThumbnail(byte[] data, int id, String fullSizeName)
    {
        Bitmap fullSize = BitmapFactory.decodeByteArray(data, 0, data.length);
        return saveThumbnail(fullSize, id, fullSizeName);
    }

    private boolean makeThumbnailForImage(int id, String imagePath)
    {
        Bitmap fullSize = BitmapFactory.decodeFile(imagePath);
        String fullSizeName = new File(imagePath).getName();
        return saveThumbnail(fullSize, id, fullSizeName);
    }

    private boolean saveThumbnail(Bitmap fullSize, int id, String fullSizeName)
    {
        // scale to width, maintain aspect ratio
        int width = 150; // px
        int height = width * fullSize.getHeight() / fullSize.getWidth();
        Bitmap scaled = Bitmap
                .createScaledBitmap(fullSize, width, height, true);

        String fileName = "thumb_" + fullSizeName;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scaled.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        File tmpFile = saveToExternalTmpFile(stream.toByteArray());

        if (tmpFile == null)
        {
            log("Could not save thumb to tmp file");
            return false;
        }

        File thumbsDir = getPersonThumbsDir(id);
        return tmpFile.renameTo(new File(thumbsDir, fileName));
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
        return "face_" + System.currentTimeMillis() + ".jpg";
    }

    private void log(String msg)
    {
        Log.d(this.getClass().toString(), msg);
    }
}
