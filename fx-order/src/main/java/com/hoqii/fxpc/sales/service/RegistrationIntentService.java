package com.hoqii.fxpc.sales.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.job.GcmJob;
import com.path.android.jobqueue.JobManager;

import java.io.IOException;

/**
 * Created by miftakhul on 3/11/16.
 */
public class RegistrationIntentService extends IntentService {
    JobManager jobManager;
    SharedPreferences preferences;
    public RegistrationIntentService() {
        super("RegistrationIntentService");
        preferences = SignageApplication.getInstance().getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        jobManager = SignageApplication.getInstance().getJobManager();
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId), GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.d(getClass().getSimpleName(), "token : "+token);
            GcmUtils.saveGcmToken(token, false);
            jobManager.addJobInBackground(new GcmJob(preferences.getString("server_url", ""), token));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
