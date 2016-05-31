package com.hoqii.fxpc.sales.job;

import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.entity.Retur;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.hoqii.fxpc.sales.util.JsonRequestUtils;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by akm on 30/05/16.
 */
public class ReturnOrderMenuJob extends Job {

    private String returnId;
    private SharedPreferences preferences;
    private String returUrl = "/api/order/returns/";
    private JsonRequestUtils.HttpResponseWrapper<Retur> responseGetReturById;
    private Retur retur;
    private boolean loadMore = false;

    public ReturnOrderMenuJob(String returnId, boolean loadMore) {
        super(new Params(1).requireNetwork().persist());
        this.returnId = returnId;
    }

    @Override
    public void onAdded() {
        EventBus.getDefault().post(new ReturnOrderMenuEvent(ReturnOrderMenuEvent.STARTED, retur));
    }

    @Override
    public void onRun() throws Throwable {
        preferences = SignageApplication.getInstance().getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        String uriRequest = preferences.getString("server_url", "") + returUrl + returnId + "?access_token=" + AuthenticationUtils.getCurrentAuthentication().getAccessToken();
        JsonRequestUtils requestUtils = new JsonRequestUtils(uriRequest);
        responseGetReturById = requestUtils.get(new TypeReference<Retur>() {});

        HttpResponse httpResponse = responseGetReturById.getHttpResponse();
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            retur = responseGetReturById.getContent();

            EventBus.getDefault().post(new ReturnOrderMenuEvent(ReturnOrderMenuEvent.SUCCESS, retur));
        } else {
            EventBus.getDefault().post(new ReturnOrderMenuEvent(ReturnOrderMenuEvent.FAILED, retur));
        }

    }

    @Override
    protected void onCancel() {
        EventBus.getDefault().post(new ReturnOrderMenuEvent(ReturnOrderMenuEvent.FAILED, retur));
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }

    public static class ReturnOrderMenuEvent {
        public static final int SUCCESS = 0;
        public static final int FAILED = 1;
        public static final int ERROR = 2;
        public static final int STARTED = 3;

        private final int status;
        private Retur retur;

        public ReturnOrderMenuEvent(int status, Retur retur) {
            this.retur = retur;
            this.status = status;
        }

        public int getStatus() {
            return status;
        }

        public Retur getRetur() {
            return retur;
        }
    }
}
