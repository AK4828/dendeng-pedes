package com.hoqii.fxpc.sales.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.hoqii.fxpc.sales.SignageVariables;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

public class ImageUtil {
    public static final String PICTURE_DIR = "esales";

	public static Bitmap convertImage(byte[] data) {
		return BitmapFactory.decodeByteArray(data, 0, data.length);
	}

    public static void clearImageCache() {
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.clearDiskCache();
        imageLoader.clearMemoryCache();
    }

    public static Uri savePublic(Context context, String name, Bitmap image) {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + File.separator + SignageVariables.PUBLIC_FOLDER);

        String fileName = SignageVariables.PUBLIC_FOLDER + "_" + name;

        try {
            File mediaFile = new File(mediaStorageDir.getPath()
                    + File.separator + fileName);

            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("Folder", "failed to create directory");
                }
            }

            FileOutputStream fos = new FileOutputStream(mediaFile);
//            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();

            return Uri.fromFile(mediaFile);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Bitmap getImagePublic(Context context, String name) {
        String fileName = SignageVariables.PUBLIC_FOLDER + "_" + name;
        File mediaStorageDir = new File(
                Environment.getExternalStorageDirectory() + File.separator
                        + SignageVariables.PUBLIC_FOLDER);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            File mediaFile = new File(mediaStorageDir.getPath()
                    + File.separator + fileName);
            Bitmap bmp = BitmapFactory.decodeFile(mediaFile.getAbsolutePath());

//            bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            return BitmapFactory
                    .decodeByteArray(byteArray, 0, byteArray.length);
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }


    public static File savePublic(Context context, String name, InputStream image) {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + File.separator + SignageVariables.PUBLIC_FOLDER);

        String fileName = SignageVariables.PUBLIC_FOLDER + "_" + name;

        try {
            File mediaFile = new File(mediaStorageDir.getPath()
                    + File.separator + fileName);

            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("Folder", "failed to create directory");
                }
            }


            FileUtils.copyInputStreamToFile(image, mediaFile);
            IOUtils.closeQuietly(image);

            return mediaFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File save(Context context, String path, final String id, String parameter, String url) throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url + path + "/" + id + parameter);

        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        BufferedHttpEntity bufHttpEntity = new BufferedHttpEntity(entity);

        File targetFile = new File(getImagePath(context, id));
        FileOutputStream target = new FileOutputStream(targetFile);

        IOUtils.copy(bufHttpEntity.getContent(), target);
        IOUtils.closeQuietly(bufHttpEntity.getContent());
        IOUtils.closeQuietly(target);

        Log.d("Image saved to ", targetFile.getPath());

        return targetFile;
    }

	public static InputStream getImage(Context context, String image) {
		try {
			String state = Environment.getExternalStorageState();
	        File mediaStorageDir = null;

            if (Environment.MEDIA_MOUNTED.equals(state)) {
                mediaStorageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), PICTURE_DIR);
            } else {
                mediaStorageDir = new File(context.getFilesDir(), PICTURE_DIR);
            }

//	        if (Environment.MEDIA_MOUNTED.equals(state)) {
//	                mediaStorageDir = new File(context
//	                                .getExternalCacheDir(), "");
//	        } else if (Environment.MEDIA_MOUNTED_READ_ONLY
//	                        .equals(state)) {
//	                mediaStorageDir = new File(context.getCacheDir(),
//	                                "");
//	        } else {
//	                mediaStorageDir = new File(context.getCacheDir(),
//	                                "");
//	        }

	        File mediaFile = new File(mediaStorageDir.getPath()
	                        + File.separator + image);
//	        Bitmap bmp = BitmapFactory.decodeFile(mediaFile
//	                .getAbsolutePath());

            return FileUtils.openInputStream(mediaFile);

//	        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//	        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
//	        byte[] byteArray = stream.toByteArray();
//
//	        return byteArray;
		} catch (Exception e) {
			e.printStackTrace();
			
			return null;
		}
	}

    public static String getImagePath(Context context, String image) {
        return new File(getImageDirectory(context), image).getPath();
    }

    @SuppressLint("LongLogTag")
    public static String getImageDirectory(Context context) {
        String state = Environment.getExternalStorageState();
        File mediaStorageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            mediaStorageDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), PICTURE_DIR);
        } else {
            mediaStorageDir = new File(context.getFilesDir(), PICTURE_DIR);
        }

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdir()) {
                Log.e("Failed creating directory: ", mediaStorageDir.getPath());
            }
        }

        return mediaStorageDir.getPath();
    }
}
