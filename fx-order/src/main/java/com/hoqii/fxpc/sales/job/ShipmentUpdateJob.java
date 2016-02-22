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

import de.greenrobot.event.EventBus;

/**
 * Created by miftakhul on 12/19/15.
 */
public class ShipmentUpdateJob extends Job {

    public static final int PROCESS_ID = 58;
    private JsonRequestUtils.HttpResponseWrapper<Shipment> response, responseGet;
    private String url, shipmentId;
    private Shipment shipment;
    private boolean[] status;

    public ShipmentUpdateJob(String url, String shipmentId, boolean[] status) {
        super(new Params(Priority.HIGH).requireNetwork().persist());
        this.url = url;
        this.shipmentId = shipmentId;
        this.status = status;
        Log.d(getClass().getSimpleName(), "url : " + url + " shipmentId " + shipmentId);
    }

    @Override
    public void onAdded() {
        EventBus.getDefault().post(new GenericEvent.RequestInProgress(PROCESS_ID));
    }

    @Override
    public void onRun() throws Throwable {
        Log.d(getClass().getSimpleName(), "Shipment updater running");
        JsonRequestUtils requestGet = new JsonRequestUtils(url + ESalesUri.SHIPMENT + "/" + shipmentId);
        responseGet = requestGet.get(new TypeReference<Shipment>() {
        });

        HttpResponse rg = responseGet.getHttpResponse();
        if (rg.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
            Shipment shipmentGet = responseGet.getContent();

            JsonRequestUtils request = new JsonRequestUtils(url + ESalesUri.SHIPMENT + "/" + shipmentId);
            shipment = new Shipment();
            shipment = shipmentGet;

            if (status[0]){
                shipment.setStatus(Shipment.ShipmentStatus.DELIVERED);
            }else {
                shipment.setStatus(Shipment.ShipmentStatus.WAIT);
            }

            response = request.put(shipment, new TypeReference<Shipment>() {
            });

            HttpResponse r = response.getHttpResponse();
            Log.d(getClass().getSimpleName(), "response update : " + r.getStatusLine().getStatusCode());
            Log.d(getClass().getSimpleName(), "response update : " + r.getStatusLine().getReasonPhrase());

            if (r.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                Log.d(getClass().getSimpleName(), "Shipment update Response Code :" + r.getStatusLine().getStatusCode());
                Shipment shipment = response.getContent();

                EventBus.getDefault().post(new GenericEvent.RequestSuccess(PROCESS_ID, response, shipment.getRefId(), shipmentId));
            } else {
                Log.d(getClass().getSimpleName(), "Shipment update Response Code :" + r.getStatusLine().getStatusCode());
                EventBus.getDefault().post(new GenericEvent.RequestFailed(PROCESS_ID, response));
            }
        } else {
            Log.d(getClass().getSimpleName(), "Shipment get Response Code :" + rg.getStatusLine().getStatusCode());
            EventBus.getDefault().post(new GenericEvent.RequestFailed(PROCESS_ID, responseGet));
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