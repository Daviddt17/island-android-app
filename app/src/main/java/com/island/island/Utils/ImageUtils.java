package com.island.island.Utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by poo on 2/10/2016.
 */
public class ImageUtils
{
    private static String TAG = ImageUtils.class.getSimpleName();
    private static String IMAGE_DIR = "images";
    private static String JPG_EXT = ".jpg";

    private static String DEFAULT_HEADER_ASSET = "default_header.jpg";
    private static String DEFAULT_PROFILE_ASSET = "default_profile.jpg";

    private static String SLASH = "/";

    private static int COMPRESSION = 100;
    private static int SAMPLE_SIZE = 2;

    public static void saveBitmapToInternalStorage(Context context, Bitmap bitmap, String filePath)
    {
        File directory = context.getDir(IMAGE_DIR, Context.MODE_PRIVATE);
        File bitmapPath = new File (directory, filePath);
        FileOutputStream outputStream = null;

        try
        {
            outputStream = new FileOutputStream(bitmapPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION, outputStream);
            try
            {
                outputStream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static Uri saveBitmapToInternalStorage(Context context, Bitmap bitmap)
    {
        File directory = context.getDir(IMAGE_DIR, Context.MODE_PRIVATE);
        File bitmapPath = new File (directory, getCurrentTimeJpgString());
        FileOutputStream outputStream = null;

        try
        {
            outputStream = new FileOutputStream(bitmapPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION, outputStream);
            try
            {
                outputStream.close();
                return Uri.fromFile(bitmapPath);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getBitmapFromInternalStorage(Context context, String filePath)
    {
        File directory = context.getDir(IMAGE_DIR, Context.MODE_PRIVATE);
        File bitmapPath = new File (directory, filePath);
        Bitmap bitmap = null;

        try
        {
            bitmap = BitmapFactory.decodeStream(new FileInputStream(bitmapPath));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static Uri saveBitmapToInternalFromUri(Context context, Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
            Log.v(TAG, "Failed to get bitmap from uri.");
        }

        Uri newUri = null;
        if (bitmap != null) {
            newUri = saveBitmapToInternalStorage(context, bitmap);
        }
        return newUri;
    }

    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static Bitmap getSampledBitmapFromUri(Context context, Uri uri) {
        InputStream input = null;
        Bitmap bitmap = null;
        try {
            input = context.getContentResolver().openInputStream(uri);
            BitmapFactory.Options bitmapOptions  = new BitmapFactory.Options();
            bitmapOptions.inSampleSize = SAMPLE_SIZE;
            bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
            input.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private static String getCurrentTimeJpgString() {
        return System.currentTimeMillis() + JPG_EXT;
    }

    public static Bitmap getBitmapFromAssets(Context context, String filePath)
    {
        AssetManager assetManager = context.getAssets();

        InputStream inputStream;
        Bitmap bitmap = null;
        try
        {
            inputStream = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static Uri getDefaultProfileImageUri(Context context) {
        File directory = context.getDir(IMAGE_DIR, Context.MODE_PRIVATE);
        File image = new File (directory, DEFAULT_PROFILE_ASSET);

        if (!image.exists()) {
            saveBitmapToInternalStorage(
                    context,
                    getBitmapFromAssets(context, IMAGE_DIR + SLASH + DEFAULT_PROFILE_ASSET),
                    DEFAULT_PROFILE_ASSET);
        }
        return Uri.fromFile(image);
    }

    public static Uri getDefaultHeaderImageUri(Context context) {
        File directory = context.getDir(IMAGE_DIR, Context.MODE_PRIVATE);
        File image = new File (directory, DEFAULT_HEADER_ASSET);

        if (!image.exists()) {
            saveBitmapToInternalStorage(
                    context,
                    getBitmapFromAssets(context, IMAGE_DIR + SLASH + DEFAULT_HEADER_ASSET),
                    DEFAULT_HEADER_ASSET);
        }
        return Uri.fromFile(image);
    }

    public static byte[] getByteArrayFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION, stream);
        return stream.toByteArray();
    }

    public static Bitmap getBitmapFromByteArray(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public static Uri saveBitmapToInternalFromByteArray(Context context, byte[] byteArray) {
        return saveBitmapToInternalStorage(context, getBitmapFromByteArray(byteArray));
    }
}
