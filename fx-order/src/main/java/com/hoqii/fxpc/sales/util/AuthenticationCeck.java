package com.hoqii.fxpc.sales.util;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.event.LoginEvent;
import com.hoqii.fxpc.sales.job.LoginManualJob;
import com.hoqii.fxpc.sales.job.RefreshTokenJob;
import com.path.android.jobqueue.JobManager;

import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

/**
 * Created by miftakhul on 1/18/16.
 */
public class AuthenticationCeck {
    private final String TAG = getClass().getSimpleName();
    private ProgressDialog dialogRefresh;
    private JobManager jobManager;
    private int currentProcess = 0;
    private int failedRefreshCount = 0;
    private Context context;


    public AuthenticationCeck() {
        jobManager = SignageApplication.getInstance().getJobManager();
    }


    /**
     * cek network access
     **/
    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) SignageApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }


    /**
     * cek access authentication
     **/
    public boolean isAccess() {
        boolean access = false;

        long expiresIn = AuthenticationUtils.getCurrentAuthentication().getExpiresIn();
        Log.d(getClass().getSimpleName(), "site expiresIn : " + expiresIn);
        Log.d(getClass().getSimpleName(), "login time default : " + AuthenticationUtils.getCurrentAuthentication().getLoginTime());


        long loginTime = AuthenticationUtils.getCurrentAuthentication().getLoginTime();
        Log.d(getClass().getSimpleName(), "login time : " + loginTime);

        long curentTime = System.currentTimeMillis();
        Log.d(getClass().getSimpleName(), "curent time : " + curentTime);

        long realDuration = curentTime - loginTime;
        Log.d(getClass().getSimpleName(), "real duration: " + realDuration);

        long realDurationInSecon = TimeUnit.MILLISECONDS.toSeconds(realDuration);
        Log.d(getClass().getSimpleName(), "real duration insecon: " + realDurationInSecon);


        /**
         * set access
         * **/
        if (expiresIn > realDurationInSecon) {
            access = true;
            Log.d(getClass().getSimpleName(), "access status : granted");
        } else {
            access = false;
            Log.d(getClass().getSimpleName(), "access status : not granted");
        }


//testing
//        if (expiresIn > (realDurationInSecon + 43139)) {
//            access = true;
//            Log.d(getClass().getSimpleName(), "access status : granted");
//        } else {
//            access = false;
//        }
        Log.d(getClass().getSimpleName(), "access status : " + String.valueOf(access));
        return access;
    }


    /**
     * // try to refresh token \\
     * setting request
     **/
    public void refreshToken(Context context, int processId) {
        this.context = context;
        this.currentProcess = processId;

        dialogRefresh = new ProgressDialog(context);
        dialogRefresh.setMessage("Please wait ...");
        dialogRefresh.setCancelable(false);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        if (EventBus.getDefault().isRegistered(this)) {
        }

        dialogRefresh.show();
        jobManager.addJobInBackground(new RefreshTokenJob());
    }

    private void reloadRefreshToken() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Refresh Token");
        builder.setMessage("Process failed\nRepeat process ?");
        builder.setCancelable(false);
        builder.setPositiveButton(SignageApplication.getInstance().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialogRefresh.show();
                jobManager.addJobInBackground(new RefreshTokenJob());
            }
        });
        builder.setNegativeButton(SignageApplication.getInstance().getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void requestNewToken() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Refresh Token");
        builder.setMessage("Process failed\nCreate new token");
        builder.setCancelable(false);
        builder.setPositiveButton(SignageApplication.getInstance().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

                dialogRefresh.show();
                String username = pref.getString("username", null);
                String password = pref.getString("password", null);
                LoginManualJob login = new LoginManualJob(username, password);
                jobManager.addJobInBackground(login);
            }
        });
        builder.setNegativeButton(SignageApplication.getInstance().getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }


    /**
     * handle event from refresh
     ***/
    public void onEventMainThread(GenericEvent.RequestInProgress requestInProgress) {
        Log.d(TAG, "current process : " + String.valueOf(requestInProgress));
    }

    public void onEventMainThread(GenericEvent.RequestSuccess requestSuccess) {
        Log.d(TAG, "process : " + String.valueOf(requestSuccess) + " success");
    }

    public void onEventMainThread(GenericEvent.RequestFailed failed) {
        Log.d(TAG, "process : " + String.valueOf(failed) + " failed");
        dialogRefresh.dismiss();
        switch (failed.getProcessId()) {
            case RefreshTokenJob.PROCESS_ID:
                failedRefreshCount++;
                if (failedRefreshCount >= 2) {
                    requestNewToken();
                } else {
                    reloadRefreshToken();
                }
                break;
        }
    }

    public void onEventMainThread(LoginEvent.LoginSuccess loginSuccess) {
        dialogRefresh.dismiss();
        EventBus.getDefault().post(new GenericEvent.RequestSuccess(currentProcess, null, null, null));
        EventBus.getDefault().unregister(this);
        currentProcess = 0;
        failedRefreshCount = 0;
    }

    public void onEventMainThread(LoginEvent.LoginFailed loginFailed) {
        dialogRefresh.dismiss();
        failedRefreshCount++;
        if (failedRefreshCount >= 2) {
            requestNewToken();
        } else {
            reloadRefreshToken();
        }
    }


}
