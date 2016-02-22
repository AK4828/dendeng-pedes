package com.hoqii.fxpc.sales.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageAppication;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.event.LoginEvent;
import com.hoqii.fxpc.sales.job.RefreshTokenJob;
import com.hoqii.fxpc.sales.job.ShipmentUpdateJob;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.path.android.jobqueue.JobManager;

import org.meruvian.midas.core.defaults.DefaultActivity;

import de.greenrobot.event.EventBus;

/**
 * Created by ludviantoovandi on 15/10/14.
 */
public class SplashActivity extends DefaultActivity {
    private SharedPreferences preferences;

    private ProgressDialog progressDialog;
    private JobManager jobManager;

    @Override
    public int layout() {
        return R.layout.activity_splash;
    }

    @Override
    public void onViewCreated(Bundle bundle) {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);


        jobManager = SignageAppication.getInstance().getJobManager();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Log.d(getClass().getSimpleName(), "HAS Sync : " + preferences.getBoolean("has_sync", false));
                if (preferences.getBoolean("has_sync", false)) {
                    jobManager.addJobInBackground(new RefreshTokenJob());

                    //testing
//                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
//                    finish();
                } else if (!preferences.getBoolean("has_sync", false)) {
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                    finish();
                }

            }
        }, 1000);

    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(GenericEvent.RequestSuccess requestSuccess) {
        Log.d(getClass().getSimpleName(), "Refresh success");

        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    }

    public void onEventMainThread(LoginEvent.LoginSuccess requestSuccess) {
        Log.d(getClass().getSimpleName(), "Refresh success");

        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
    }

    public void onEventMainThread(LoginEvent.LoginFailed loginFailed) {
        Log.d(getClass().getSimpleName(), "Refresh Failed");
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.clear();
        editor.commit();

        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        finish();
    }

}