package com.hoqii.fxpc.sales.job;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.service.GcmUtils;
import com.hoqii.fxpc.sales.util.JsonRequestUtils;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.meruvian.midas.core.job.Priority;

import de.greenrobot.event.EventBus;

/**
 * Created by miftakhul on 12/19/15.
 */
public class GcmJob extends Job {
    public static final int PROCESS_ID = 90;
    private JsonRequestUtils.HttpResponseWrapper<String> response;
    private String url, gcmToken;

    public GcmJob(String url, String gcmToken) {
        super(new Params(Priority.HIGH).requireNetwork().persist());

        this.url = url;
        this.gcmToken = gcmToken;
    }

    @Override
    public void onAdded() {
        EventBus.getDefault().post(new GenericEvent.RequestInProgress(PROCESS_ID));
    }

    @Override
    public void onRun() throws Throwable {
        Log.d(getClass().getSimpleName(), "gcm send token running");
        JsonRequestUtils request = new JsonRequestUtils(url + "/api/device/register");

        response = request.post(gcmToken, new TypeReference<String>() {
        });

        HttpResponse r = response.getHttpResponse();
        Log.d(getClass().getSimpleName(), "response : " + r.getStatusLine().getStatusCode());
        Log.d(getClass().getSimpleName(), "response : " + r.getStatusLine().getReasonPhrase());

        if (r.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            Log.d(getClass().getSimpleName(), "Response Code :" + r.getStatusLine().getStatusCode());
            GcmUtils.saveGcmToken(gcmToken, true);
            EventBus.getDefault().post(new GenericEvent.RequestSuccess(PROCESS_ID, response, null, null));
            Log.d(getClass().getSimpleName(), "[ token sended ]");
        } else {
            Log.d(getClass().getSimpleName(), "Response Code :" + r.getStatusLine().getStatusCode());
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

