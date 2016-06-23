package com.hoqii.fxpc.sales.job;

import android.util.Log;

/**
 * Created by miftakhul on 23/06/16.
 */
public class JobCounting {
    int count = 0;
    int size = 0;

    public boolean isJobFinish(int size) {
        count ++;
        Log.d(getClass().getSimpleName(), "job count "+Integer.toString(count)+" and size "+Integer.toString(size));
        if (size >= count){
            return true;
        }else {
            return false;
        }
    }
}
