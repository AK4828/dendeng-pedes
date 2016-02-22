package com.hoqii.fxpc.sales.job;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hoqii.fxpc.sales.entity.Shipment;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.util.JsonRequestUtils;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.meruvian.midas.core.job.Priority;

import java.io.InputStream;

import de.greenrobot.event.EventBus;

/**
 * Created by miftakhul on 2/9/16.
 */
public class ImageOrderMenuJob extends Job{

    public static final int PROCESS_ID = 59;
    private JsonRequestUtils.HttpResponseWrapper<InputStream> respon;
    private String url;
    private String productId;


    protected ImageOrderMenuJob(String url, String productId) {
        super(new Params(Priority.HIGH).requireNetwork().persist());

        this.url = url;
        this.productId = productId;
    }


    @Override
    public void onAdded() {
        Log.d(getClass().getSimpleName(), "image order menu added");

    }

    @Override
    public void onRun() throws Throwable {
        Log.d(getClass().getSimpleName(), "image order menu running");

        JsonRequestUtils requestReceipt = new JsonRequestUtils(url + ESalesUri.SHIPMENT_RECEIPT);
        respon = requestReceipt.get(new TypeReference<InputStream>() {
        });

        HttpResponse res = respon.getHttpResponse();
        if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
            InputStream imageStream = respon.getContent();
            EventBus.getDefault().post(imageStream);
        }else {
            EventBus.getDefault().post(new GenericEvent.RequestFailed(PROCESS_ID, respon));
        }
    }

    @Override
    protected void onCancel() {
        EventBus.getDefault().post(new GenericEvent.RequestFailed(PROCESS_ID, respon));
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
