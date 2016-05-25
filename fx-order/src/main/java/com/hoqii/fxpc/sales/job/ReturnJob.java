package com.hoqii.fxpc.sales.job;

import android.content.SharedPreferences;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.core.commons.Site;
import com.hoqii.fxpc.sales.entity.Retur;
import com.hoqii.fxpc.sales.entity.OrderMenuSerial;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.hoqii.fxpc.sales.util.JsonRequestUtils;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import de.greenrobot.event.EventBus;

/**
 * Created by akm on 23/05/16.
 */
public class ReturnJob extends Job {

    private JsonRequestUtils.HttpResponseWrapper<Retur> postReturn;
    private String siteTo;
    private String description;
    private String serial;
    private SharedPreferences preferences;
    private String returUrl = "/api/order/returns";
    private Retur retur;
    private OrderMenuSerial orderMenuSerial;
    private Site siteFrom, siteDestination;

    public ReturnJob(String siteTo, String description, String serial) {
        super(new Params(1).requireNetwork().persist());
        this.siteTo = siteTo;
        this.description = description;
        this.serial = serial;
    }

    @Override
    public void onAdded() {
        EventBus.getDefault().post(new ReturnEvent(ReturnEvent.STARTED, 0));
    }

    @Override
    public void onRun() throws Throwable {
        preferences = SignageApplication.getInstance().getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        String urlRetur = preferences.getString("server_url", "") + returUrl + "?access_token=" + AuthenticationUtils.getCurrentAuthentication().getAccessToken();
        JsonRequestUtils requestUtils = new JsonRequestUtils(urlRetur);
        retur = new Retur();
        orderMenuSerial = new OrderMenuSerial();
        siteFrom = new Site();
        siteDestination = new Site();

        siteDestination.setId(siteTo);
        siteFrom.setId(AuthenticationUtils.getCurrentAuthentication().getSite().getId());
        orderMenuSerial.setId(serial);
        retur.setStatus(Retur.ReturnStatus.RETURNED);
        retur.setSiteTo(siteDestination);
        retur.setSiteFrom(siteFrom);
        retur.setDescription(description);
        retur.setOrderMenuSerial(orderMenuSerial);
        postReturn = requestUtils.post(retur, new TypeReference<Retur>() {
        });
        HttpResponse r = postReturn.getHttpResponse();
        if (r.getStatusLine().getStatusCode() == HttpStatus.SC_OK || r.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
            Log.d("CODE", String.valueOf(r.getStatusLine().getStatusCode()));
            EventBus.getDefault().post(new ReturnEvent(ReturnEvent.SUCCESS, r.getStatusLine().getStatusCode()));
        } else {
            EventBus.getDefault().post(new ReturnEvent(ReturnEvent.FAILED, r.getStatusLine().getStatusCode()));
        }

    }

    @Override
    protected void onCancel() {
        new ReturnEvent(ReturnEvent.ERROR, 0);
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }

    public static class ReturnEvent {
        public static final int SUCCESS = 0;
        public static final int FAILED = 1;
        public static final int ERROR = 2;
        public static final int STARTED = 3;

        private final int status;
        private final int statusCode;

        public ReturnEvent(int status, int statusCode) {
            this.status = status;
            this.statusCode = statusCode;
        }

        public int getStatus() {
            return status;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
