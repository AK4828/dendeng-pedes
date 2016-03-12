package com.hoqii.fxpc.sales.service;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

/**
 * Created by miftakhul on 3/12/16.
 */
public class MyGcmListenerService extends GcmListenerService{

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        Log.d(getClass().getSimpleName(), "FROM "+from);
        Log.d(getClass().getSimpleName(), "Message "+message);
    }
}
