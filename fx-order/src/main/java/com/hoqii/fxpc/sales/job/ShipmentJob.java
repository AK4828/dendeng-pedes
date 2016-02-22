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

import java.util.Date;

import de.greenrobot.event.EventBus;

/**
 * Created by miftakhul on 12/19/15.
 */
public class ShipmentJob extends Job {

    public static final int PROCESS_ID = 55;
    private JsonRequestUtils.HttpResponseWrapper<Shipment> response, responseReceipt;
    private String url, orderId;
    private Shipment shipment;

    public ShipmentJob(String url, String orderId) {
        super(new Params(Priority.HIGH).requireNetwork().persist());

        this.url = url;
        this.orderId = orderId;
        Log.d(getClass().getSimpleName(), "url : " + url + " order id " + orderId);

    }

    @Override
    public void onAdded() {
        EventBus.getDefault().post(new GenericEvent.RequestInProgress(PROCESS_ID));
    }

    @Override
    public void onRun() throws Throwable {
        Log.d(getClass().getSimpleName(), "Shipment running");
        JsonRequestUtils requestReceipt = new JsonRequestUtils(url + ESalesUri.SHIPMENT_RECEIPT);
        responseReceipt = requestReceipt.get(new TypeReference<Shipment>() {
        });

        HttpResponse rc = responseReceipt.getHttpResponse();
        if (rc.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
            Shipment shipmentReceipt = responseReceipt.getContent();

            JsonRequestUtils request = new JsonRequestUtils(url + ESalesUri.SHIPMENT);
            shipment = new Shipment();
            shipment.getOrder().setId(orderId);
            shipment.setReceiptNumber(shipmentReceipt.getReceiptNumber());
            shipment.getLogInformation().setCreateDate(new Date(System.currentTimeMillis()));

            response = request.post(shipment, new TypeReference<Shipment>() {});

            HttpResponse r = response.getHttpResponse();
            Log.d(getClass().getSimpleName(), "response : " + r.getStatusLine().getStatusCode());
            Log.d(getClass().getSimpleName(), "response : " + r.getStatusLine().getReasonPhrase());

            if (r.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                Log.d(getClass().getSimpleName(), "Shipment Response Code :" + r.getStatusLine().getStatusCode());
                Shipment shipment = response.getContent();
                Log.d(getClass().getSimpleName(), "Shipment Response ref id :" + shipment.getRefId());
                Log.d(getClass().getSimpleName(), "Shipment Response id :" + shipment.getId());
                EventBus.getDefault().post(new GenericEvent.RequestSuccess(PROCESS_ID, response, shipment.getRefId(), shipment.getId()));
            } else {
                Log.d(getClass().getSimpleName(), "Shipment Response Code :" + r.getStatusLine().getStatusCode());
                EventBus.getDefault().post(new GenericEvent.RequestFailed(PROCESS_ID, response));
            }
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
