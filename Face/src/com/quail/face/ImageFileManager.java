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
import java.util.Arrays;
import java.util.Collections;
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
    private final File extAppDir;
    private final File tmpDir;
    private final File peopleDir;

    private Context c;

    // See notes.txt
    public ImageFileManager(Context c)
    {
        this.c = c;

        // init app root
        extAppDir = c.getExternalFilesDir(null);
        if (extAppDir.mkdirs())
        {
            // root didn't already exist, so make the nomedia file.
            // prevents Android media scanner from adding media to the gallery
            File noMediaFile = new File(extAppDir, ".nomedia");
            try
            {
                noMediaFile.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        // init private tmp dir
        tmpDir = new File(extAppDir, "tmp/");
        tmpDir.mkdirs();

        // init people dir
        peopleDir = new File(extAppDir, "people/");
        peopleDir.mkdirs();
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
        return peopleDir.list().length;
    }

    // Returns new person id
    public int addPerson()
    {
        int newId = numberOfPersons() + 1;

        // create all dirs needed for person
        getPersonImagesDir(newId).mkdirs();
        getPersonThumbsDir(newId).mkdirs();
        getPersonVideoDir(newId).mkdirs();

        return newId;
    }

    public boolean removePerson(int id)
    {
        // delete existing person dirs
        File personDir = getPersonDir(id);
        deleteFile(personDir);

        if (personDir.exists())
        {
            log("Problem: " + personDir.getAbsolutePath() + " was not deleted");
            return false;
        }

        // reassign person #s
        for (int i = id + 1; i <= numberOfPersons(); i++)
        {
            personDir = getPersonDir(i);
            File targetDir = getPersonDir(i - 1);
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

    public void deleteImage(String path)
    {
        if (!new File(path).delete())
            log("image " + path + " was not deleted");
    }

    /**
     * Checks all image storage locations, regardless of preferences
     * 
     * @return A list of absolute paths to all found images
     */
    public List<String> getImagePathsForPerson(int id)
    {
        List<String> imgPaths = new ArrayList<String>();

        for (File f : getPersonImagesDir(id).listFiles())
        {
            String fileName = f.getName().toLowerCase();
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))
                imgPaths.add(f.getAbsolutePath());
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

    /**
     * Determine the path of the last generated video
     * 
     * @param id
     *            person id
     * @return path to the last generated video or null if there is none
     */
    public String getLastVideoPath(int id)
    {
        String[] videoPaths = getPersonVideoDir(id).list();
        Arrays.sort(videoPaths);

        if (videoPaths.length == 0)
            return null;
        else
            return new File(getPersonVideoDir(id),
                    videoPaths[videoPaths.length - 1]).getAbsolutePath();
    }

    private File getPersonDir(int id)
    {
        return new File(peopleDir, "" + id);
    }

    public File getPersonImagesDir(int id)
    {
        return new File(getPersonDir(id), "images");
    }

    public File getPersonVideoDir(int id)
    {
        return new File(getPersonDir(id), "videos");
    }

    private File getPersonThumbsDir(int id)
    {
        return new File(getPersonDir(id), "thumbs");
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

        String imageName = nextImageFileName(id);
        File imagesFile = getPersonImagesDir(id);
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
                MediaStore.Images.Media.insertImage(c.getContentResolver(),
                        destFile.getAbsolutePath(), destFile.getName(), "face");
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
                log("File not found by insertImage: "
                        + destFile.getAbsolutePath());
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

    /**
     * Sorts the images for this person id alphanumerically then renames them so
     * that there are no gaps in the number assignments
     */
    public void reorderImages(int id)
    {
        List<String> imagePaths = getImagePathsForPerson(id);

        // sort in alphanumeric order
        Collections.sort(imagePaths);

        for (int i = 1; i <= imagePaths.size(); i++)
        {
            File image = new File(imagePaths.get(i - 1));

            boolean namedCorrectly = false;
            try
            {
                int imageFileNumber = Integer.parseInt(image.getName().split(
                        "\\.")[0]);
                namedCorrectly = imageFileNumber == i;
            }
            catch (NumberFormatException nfe)
            {
                log(image.getAbsolutePath() + " not named as a number");
            }
            catch (Exception e)
            {
                e.printStackTrace();
                log("failed on " + image.getAbsolutePath());
            }

            if (!namedCorrectly)
            {
                String newFileName = imageFileNameForNumber(i);
                File newFile = new File(image.getParent(), newFileName);
                if (!image.renameTo(newFile))
                    log("renaming to " + newFile.getAbsolutePath() + " failed");
            }
        }
    }

    private int nextImageNumber(int id)
    {
        List<String> imagePaths = getImagePathsForPerson(id);

        // sort in alphanumeric order
        Collections.sort(imagePaths);

        for (int i = imagePaths.size() - 1; i >= 0; i++)
        {
            File last = new File(imagePaths.get(i));

            try
            {
                return Integer.parseInt(last.getName().split("\\.")[0]) + 1;
            }
            catch (NumberFormatException nfe)
            {
                log("Image not named as a number: " + last.getAbsolutePath());
            }
        }

        return 1;
    }

    private File saveToExternalTmpFile(byte[] data)
    {
        if (sdCardIsAvailable())
        {
            // We can read and write the media
            String randomName = new Random().nextInt() + "_"
                    + System.currentTimeMillis() + ".jpg";
            File tmpFile = new File(tmpDir, randomName);

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

    private String imageFileNameForNumber(int imageNumber)
    {
        return String.format("%04d.jpg", imageNumber);
    }

    private String nextImageFileName(int id)
    {
        return imageFileNameForNumber(nextImageNumber(id));
    }

    private void log(String msg)
    {
        Log.d(this.getClass().toString(), msg);
    }
}
