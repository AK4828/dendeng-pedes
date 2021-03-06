package com.hoqii.fxpc.sales.job;

import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.entity.Authentication;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.hoqii.fxpc.sales.util.JsonRequestUtils;
import com.path.android.jobqueue.Params;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.meruvian.midas.core.job.Priority;

import de.greenrobot.event.EventBus;

/**
 * Created by meruvian on 29/07/15.
 */
public class RefreshTokenJob extends LoginJob {
    private SharedPreferences preferences;
    public static final int PROCESS_ID = 70;

    public RefreshTokenJob() {
        super(new Params(Priority.HIGH).requireNetwork().persist());
    }

    @Override
    public void onAdded() {
        EventBus.getDefault().post(new GenericEvent.RequestInProgress(PROCESS_ID));
    }

    @Override
    public void onRun() throws Throwable {
        Log.d(getClass().getSimpleName(), "refresh token running");

        preferences = SignageApplication.getInstance().getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        Log.d(getClass().getSimpleName(), preferences.getString("server_url", ""));

        JsonRequestUtils requestUtils = new JsonRequestUtils(preferences.getString("server_url", "") + SignageVariables.PGA_REQUEST_TOKEN);
        requestUtils.addQueryParam("grant_type", "refresh_token");
        requestUtils.addQueryParam("client_id", SignageVariables.PGA_APP_ID);
        requestUtils.addQueryParam("client_secret", SignageVariables.PGA_API_SECRET);
        requestUtils.addQueryParam("refresh_token", AuthenticationUtils.getCurrentAuthentication().getRefreshToken());

        String authorization = SignageVariables.PGA_APP_ID + ":" + SignageVariables.PGA_API_SECRET;
        authorization = Base64.encodeToString(authorization.getBytes(), Base64.DEFAULT);

        requestUtils.addHeader("Authorization", "Basic " + authorization);

        JsonRequestUtils.HttpResponseWrapper<Authentication> responseWrapper =
                requestUtils.post(null, new TypeReference<Authentication>() {});
        HttpResponse response = responseWrapper.getHttpResponse();

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            EventBus.getDefault().post(new GenericEvent.RequestSuccess(PROCESS_ID, responseWrapper, "", ""));
            registerAuthentication(responseWrapper);
        } else {
            Log.e(getClass().getSimpleName(), "Access Code: " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
            EventBus.getDefault().post(new GenericEvent.RequestFailed(PROCESS_ID, responseWrapper));
        }
    }
}
