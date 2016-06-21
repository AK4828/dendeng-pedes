package com.hoqii.fxpc.sales.job;

import android.content.SharedPreferences;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.hoqii.fxpc.sales.util.JsonRequestUtils;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.meruvian.midas.core.job.Priority;

import de.greenrobot.event.EventBus;

/**
 * Created by akm on 16/05/16.
 */
public class UnregisterGCMJob extends Job {
    public static final int PROCESS_ID = 322;
    private String deviceId;
    private String url;
    private JsonRequestUtils.HttpResponseWrapper<String> response;

    public UnregisterGCMJob(String url, String deviceId) {
        super(new Params(Priority.HIGH).requireNetwork().persist());
        this.deviceId = deviceId;
        this.url = url;
    }

    @Override
    public void onAdded() {
        EventBus.getDefault().post(new GenericEvent.RequestInProgress(PROCESS_ID));
    }

    @Override
    public void onRun() throws Throwable {
        String urlReq = url + "/api/device/unregister";
        JsonRequestUtils request = new JsonRequestUtils(urlReq);
        response = request.post(deviceId, new TypeReference<String>() {});
        Log.d("deviceId", deviceId);
        HttpResponse r = response.getHttpResponse();
        if (r.getStatusLine().getStatusCode() == HttpStatus.SC_OK || r.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
            EventBus.getDefault().post(new GenericEvent.RequestSuccess(PROCESS_ID, response, null, null));
        } else {
            EventBus.getDefault().post(new GenericEvent.RequestFailed(PROCESS_ID, response));
        }
    }

    @Override
    protected void onCancel() {
        EventBus.getDefault().post(new GenericEvent.RequestFailed(PROCESS_ID, response));
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
