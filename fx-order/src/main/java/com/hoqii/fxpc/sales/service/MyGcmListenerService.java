package com.hoqii.fxpc.sales.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.activity.MainActivity;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;

/**
 * Created by miftakhul on 3/12/16.
 */
public class MyGcmListenerService extends GcmListenerService {

    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("body");
        String title = data.getString("title");
        String siteRefId = data.getString("id");
        String siteId = AuthenticationUtils.getCurrentAuthentication().getSite().getId();

        if (siteId.equals(siteRefId)) {
            NotificationManager notif = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setSmallIcon(R.drawable.cyrcle_fx).setContentTitle(title).setContentText(message);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
            builder.setContentIntent(contentIntent);
            builder.setAutoCancel(true);
            builder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
            notif.notify(1, builder.build());
        }
    }

}
