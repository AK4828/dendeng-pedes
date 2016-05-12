package com.hoqii.fxpc.sales.util;

import android.os.Environment;
import android.util.Log;

import com.hoqii.fxpc.sales.SignageVariables;

import java.io.File;

/**
 * Created by meruvian on 04/05/16.
 */
public class DirUtils {

    public static String getDirectory(){
        File folder = new File(Environment.getExternalStorageDirectory()+File.separator+ SignageVariables.PUBLIC_FOLDER);
        if (!folder.exists()){
            if (!folder.mkdir()){
                Log.d("creating directory", "failed");
            }
        }

        Log.d("file path ",folder.getPath());

        return folder.getPath();
    }

}
