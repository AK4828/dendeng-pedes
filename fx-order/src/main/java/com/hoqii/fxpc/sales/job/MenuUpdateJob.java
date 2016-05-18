package com.hoqii.fxpc.sales.job;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.content.database.adapter.SerialNumberDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.entity.SerialNumber;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.util.JsonRequestUtils;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.meruvian.midas.core.job.Priority;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by miftakhul on 12/19/15.
 */
public class MenuUpdateJob extends Job {

    public static final int PROCESS_ID = 57;
    private JsonRequestUtils.HttpResponseWrapper<OrderMenu> responseUpdate;
    private JsonRequestUtils.HttpResponseWrapper<OrderMenu> responseGetOrderMenu;
    private String url, orderId, orderMenuId;

    public MenuUpdateJob(String url, String orderId, String orderMenuId) {
        super(new Params(Priority.HIGH).requireNetwork().persist());

        this.url = url;
        this.orderId = orderId;
        this.orderMenuId = orderMenuId;

        Log.d(getClass().getSimpleName(), "url : " + url + " order id " + orderId + " order menu id : " + orderMenuId);

    }

    @Override
    public void onAdded() {
        EventBus.getDefault().post(new GenericEvent.RequestInProgress(PROCESS_ID));
    }

    @Override
    public void onRun() throws Throwable {
        Log.d(getClass().getSimpleName(), "Update orde menu running");
        JsonRequestUtils request = new JsonRequestUtils(url + "/api/orders/" + orderId + "/menu/" + orderMenuId );

        responseGetOrderMenu = request.get(new TypeReference<OrderMenu>() {});

        HttpResponse rOm = responseGetOrderMenu.getHttpResponse();

        if (rOm.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            OrderMenu orderMenu = responseGetOrderMenu.getContent();

            JsonRequestUtils requestUpdate = new JsonRequestUtils(url + "/api/orders/" + orderId + "/menu/" + orderMenuId );

            SerialNumberDatabaseAdapter snAdapter = new SerialNumberDatabaseAdapter(SignageApplication.getInstance());
            List<SerialNumber> sn = snAdapter.getSerialNumberListByOrderIdAndOrderMenuIdAndHasSync(orderId, orderMenuId);

            Log.d(getClass().getSimpleName(), "menu quantity =========================: "+orderMenu.getQty());
            Log.d(getClass().getSimpleName(), "serial quantity =========================: "+sn.size());

            if (orderMenu.getQty() == sn.size()) {
                orderMenu.setQty(orderMenu.getQty() - sn.size());
                orderMenu.setStatus(OrderMenu.OrderMenuStatus.SHIPPED);
                Log.d(getClass().getSimpleName(), "Updating order menu status shipped");
            }else {
                orderMenu.setQty(orderMenu.getQty() - sn.size());
                Log.d(getClass().getSimpleName(), "Updating order menu size");
            }

            Log.d(getClass().getSimpleName(), "data order menu " + orderMenu.toString());
            Log.d(getClass().getSimpleName(), "data order menu id " + orderMenu.getId());
            Log.d(getClass().getSimpleName(), "data order menu status " + orderMenu.getStatus().name().toString());

            responseUpdate = requestUpdate.put(orderMenu, new TypeReference<OrderMenu>() {
            });

            HttpResponse rUm = responseUpdate.getHttpResponse();
            Log.d(getClass().getSimpleName(), "response update : " + rUm.getStatusLine().getStatusCode());
            Log.d(getClass().getSimpleName(), "response update : " + rUm.getStatusLine().getReasonPhrase());


            if (rUm.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                Log.d(getClass().getSimpleName(), "update Response Code :" + rUm.getStatusLine().getStatusCode());
                OrderMenu om = responseUpdate.getContent();

                Log.d(getClass().getSimpleName(), "order menu id : " + om.getId().toString());

                snAdapter.updateStatusFlag(orderMenuId);
                EventBus.getDefault().post(new GenericEvent.RequestSuccess(PROCESS_ID, responseUpdate, om.getRefId(), om.getId()));
            } else {
                Log.d(getClass().getSimpleName(), "update Response Code :" + rUm.getStatusLine().getStatusCode());
                EventBus.getDefault().post(new GenericEvent.RequestFailed(PROCESS_ID, responseUpdate));
            }

            EventBus.getDefault().post(new GenericEvent.RequestSuccess(PROCESS_ID, responseUpdate, orderMenu.getRefId(), orderMenu.getId()));
        } else {
            Log.d(getClass().getSimpleName(), "get order menu Response Code :" + rOm.getStatusLine().getStatusCode());
            EventBus.getDefault().post(new GenericEvent.RequestFailed(PROCESS_ID, responseUpdate));
        }

    }

    @Override
    protected void onCancel() {
        EventBus.getDefault().post(new GenericEvent.RequestFailed(PROCESS_ID, responseUpdate));
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }


}

