package com.hoqii.fxpc.sales.job;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hoqii.fxpc.sales.SignageAppication;
import com.hoqii.fxpc.sales.content.database.adapter.SerialNumberDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.SerialNumber;
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
public class SerialJob extends Job {

    public static final int PROCESS_ID = 56;
    private JsonRequestUtils.HttpResponseWrapper<SerialNumber> response;
    private String url, id, shipmentId;

    public SerialJob(String url, String id, String shipmentId) {
        super(new Params(Priority.HIGH).requireNetwork().persist());

        this.url = url;
        this.id = id;
        this.shipmentId = shipmentId;
        Log.d(getClass().getSimpleName(), "url : " + url + " id " + id + " shipment id : "+shipmentId);

    }

    @Override
    public void onAdded() {
        EventBus.getDefault().post(new GenericEvent.RequestInProgress(PROCESS_ID));
    }

    @Override
    public void onRun() throws Throwable {
        Log.d(getClass().getSimpleName(), "Serial running");
        JsonRequestUtils request = new JsonRequestUtils(url + ESalesUri.SERIAL);

        SerialNumberDatabaseAdapter serialNumberDatabaseAdapter = new SerialNumberDatabaseAdapter(SignageAppication.getInstance());

        SerialNumber serialNumber = new SerialNumber();
        serialNumber = serialNumberDatabaseAdapter.findSerialNumber(id);
        serialNumber.setId(null);
        serialNumber.getShipment().setId(shipmentId);


        Log.d(getClass().getSimpleName(), "serial ordermenu id : " + serialNumber.getOrderMenu().getId());
        Log.d(getClass().getSimpleName(), "serial order id : " + serialNumber.getOrderMenu().getOrder().getId());
        Log.d(getClass().getSimpleName(), "serial  : " + serialNumber.getSerialNumber());
        Log.d(getClass().getSimpleName(), "serial shipment id  : " + serialNumber.getShipment().getId());

        response = request.post(serialNumber, new TypeReference<SerialNumber>() {
        });

        HttpResponse r = response.getHttpResponse();
        Log.d(getClass().getSimpleName(), "response : " + r.getStatusLine().getStatusCode());
        Log.d(getClass().getSimpleName(), "response : " + r.getStatusLine().getReasonPhrase());

        if (r.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            Log.d(getClass().getSimpleName(), "Serial Response Code :" + r.getStatusLine().getStatusCode());
            SerialNumber sn = response.getContent();
            Log.d(getClass().getSimpleName(),"response serial " + response.toString());
            Log.d(getClass().getSimpleName(),"response serial Id" + id);
            Log.d(getClass().getSimpleName(),"response serial refRefId" + sn.getId());

            serialNumberDatabaseAdapter.updateStatus(id, sn.getId(), shipmentId);
            EventBus.getDefault().post(new GenericEvent.RequestSuccess(PROCESS_ID, response, sn.getId(), id));
        } else {
            Log.d(getClass().getSimpleName(), "Serial Response Code :" + r.getStatusLine().getStatusCode());
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

