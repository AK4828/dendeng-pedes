package com.hoqii.fxpc.sales.service;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.hoqii.fxpc.sales.R;

/**
 * Created by miftakhul on 3/12/16.
 */
public class MyGcmListenerService extends GcmListenerService{

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");

        Log.d(getClass().getSimpleName(), "FROM "+from);
        Log.d(getClass().getSimpleName(), "Message "+message);

        NotificationManager notif = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.cyrcle_fx).setContentTitle("test notofy").setContentText("testing notif "+message);
        notif.notify(1, builder.build());
    }

}
