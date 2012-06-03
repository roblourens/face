package com.quail.face;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class ImageFileManager
{
    // person dirs are in these, and should exist in both
    private final String extGalleryPath;
    private final String extNonGalleryPath;
    private final String extTmpPath;

    private Context c;

    // See notes.txt
    public ImageFileManager(Context c)
    {
        this.c = c;
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
        // should be the same number of person dirs as gallery peopleDir
        return getPeopleDir(false).list().length;
    }

    // Returns new person id
    public int addPerson()
    {
        int newId = numberOfPersons() + 1;

        // create all dirs needed for person
        getPersonImagesDir(newId, true).mkdirs();
        getPersonImagesDir(newId, false).mkdirs();
        getPersonThumbsDir(newId).mkdirs();

        return newId;
    }

    public boolean removePerson(int id)
    {
        // delete existing person dirs
        File personDirGallery = getPersonDir(id, true);
        deleteFile(personDirGallery);
        File personDir = getPersonDir(id, false);
        deleteFile(personDir);

        if (personDir.exists() || personDirGallery.exists())
        {
            log("Problem: " + personDir.getAbsolutePath() + " was not deleted");
            return false;
        }

        // reassign person #s
        for (int i = id + 1; i <= numberOfPersons(); i++)
        {
            personDir = getPersonDir(i, true);
            File targetDir = getPersonDir(i - 1, true);
            if (!personDir.renameTo(targetDir))
                log("failed to rename " + personDir.getAbsolutePath() + " to "
                        + targetDir.getAbsolutePath());
        }

        return true;
    }

    private void deleteFile(File f)
    {
        if (!f.exists())
            return;

        // delete file
        if (f.isFile())
        {
            f.delete();
            return;
        }
        // empty and delete dir
        else
        {
            for (File subF : f.listFiles())
                deleteFile(subF);

            // should be empty by this point
            if (f.list().length == 0)
            {
                f.delete();
                return;
            }
            else
                return;
        }
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

    private File getPeopleDir(boolean gallery)
    {
        File galleryDir = new File(extGalleryPath, "people");
        File nonGalleryDir = new File(extNonGalleryPath, "people");
        return gallery ? galleryDir : nonGalleryDir;
    }

    private File getPersonDir(int id, boolean gallery)
    {
        return new File(getPeopleDir(gallery), "" + id);
    }

    private File getPersonImagesDir(int id, boolean gallery)
    {
        return new File(getPersonDir(id, gallery), "images");
    }

    private File getPersonThumbsDir(int id)
    {
        return new File(getPersonDir(id, false), "thumbs");
    }

    /**
     * Saves the given jpg image data to the external storage location, in the
     * gallery
     * 
     * @return true if successful, false otherwise
     */
    public boolean saveImage(byte[] data, int id)
    {
        File tmpFile = saveToExternalTmpFile(data);
        if (tmpFile == null)
        {
            log("Could not save to tmp file");
            return false;
        }

        String imageName = newImageFileName();
        File imagesFile = getPersonImagesDir(id, false);
        File destFile = new File(imagesFile, imageName);
        boolean renameSuccess = tmpFile.renameTo(destFile);
        boolean thumbSuccess = saveThumbnail(data, id, imageName);
        if (!renameSuccess || !thumbSuccess)
        {
            log("Couldn't save image or thumb " + destFile.getAbsolutePath());
            return false;
        }

        // TODO prefs
        // copy to gallery if needed. don't care if it fails
        boolean gallery = false;
        if (gallery)
        {
            try
            {
                File galleryFile = new File(getPersonImagesDir(id, true),
                        imageName);
                InputStream is = new ByteArrayInputStream(data);
                OutputStream fos = new FileOutputStream(galleryFile);
                if (!copyStreams(is, fos))
                    return false;

                is.close();
                fos.close();

                MediaStore.Images.Media.insertImage(c.getContentResolver(),
                        galleryFile.getAbsolutePath(), galleryFile.getName(),
                        "face");
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
                return false;
            }
            catch (IOException e)
            {
                e.printStackTrace();
                // comes from .close(), don't care
            }
        }

        return true;
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

            InputStream inStream = new ByteArrayInputStream(data);
            OutputStream outStream;
            try
            {
                outStream = new FileOutputStream(tmpFile);
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
                return null;
            }

            // do the copy
            if (!copyStreams(inStream, outStream))
                return null;

            try
            {
                inStream.close();
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

    private boolean copyStreams(InputStream inStream, OutputStream outStream)
    {
        try
        {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inStream.read(buffer)) > 0)
                outStream.write(buffer, 0, read);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
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
