package com.hoqii.fxpc.sales.job;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.util.JsonRequestUtils;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.meruvian.midas.core.job.Priority;

import de.greenrobot.event.EventBus;

/**
 * Created by miftakhul on 1/7/16.
 */
public class OrderStatusUpdateJob extends Job {
    public static final int PROCESS_ID = 40;
    private JsonRequestUtils.HttpResponseWrapper<Order> responseGet, responseUpdate;
    private String orderId;
    private String url;
    private String orderStatus;

    public OrderStatusUpdateJob(String orderId, String url, String orderStatus) {
        super(new Params(Priority.HIGH).requireNetwork().persist());
        this.orderId = orderId;
        this.url = url;
        this.orderStatus = orderStatus;
    }

    @Override
    public void onAdded() {
        Log.d(getClass().getSimpleName(), "onAdded");
        EventBus.getDefault().post(new GenericEvent.RequestInProgress(PROCESS_ID));
    }

    @Override
    public void onRun() throws Throwable {
        Log.d(getClass().getSimpleName(), "Update order running");
        JsonRequestUtils request = new JsonRequestUtils(url + "/api/orders/" + orderId);

        responseGet = request.get(new TypeReference<Order>() {
        });

        HttpResponse rGet = responseGet.getHttpResponse();
        Log.d(getClass().getSimpleName(), "response : " + rGet.getStatusLine().getStatusCode());
        Log.d(getClass().getSimpleName(), "response : " + rGet.getStatusLine().getReasonPhrase());


        if (rGet.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            Log.d(getClass().getSimpleName(), "order Response Code :" + rGet.getStatusLine().getStatusCode());
            Order order = responseGet.getContent();

            Log.d(getClass().getSimpleName(), "order id : " + order.getId().toString() + "=================");


            //====================
            Log.d(getClass().getSimpleName(), "Updating order menu running");
            JsonRequestUtils requestUpdate = new JsonRequestUtils(url + "/api/orders/" + orderId + "/status");

//            order.setStatus(Order.OrderStatus.SENDING);
            order.setStatus(Order.OrderStatus.valueOf(orderStatus));

            Log.d(getClass().getSimpleName(), "data order " + order.toString());
            Log.d(getClass().getSimpleName(), "data order id " + order.getId());
            Log.d(getClass().getSimpleName(), "data order status " + order.getStatus().name().toString());

            responseUpdate = requestUpdate.put(order, new TypeReference<Order>() {
            });

            HttpResponse rUpdate = responseUpdate.getHttpResponse();
            Log.d(getClass().getSimpleName(), "response update : " + rUpdate.getStatusLine().getStatusCode());
            Log.d(getClass().getSimpleName(), "response update : " + rUpdate.getStatusLine().getReasonPhrase());

            if (rUpdate.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                Log.d(getClass().getSimpleName(), "update Response Code :" + rUpdate.getStatusLine().getStatusCode());
                Order o = responseUpdate.getContent();

                Log.d(getClass().getSimpleName(), "order menu id : " + o.getId().toString());

                EventBus.getDefault().post(new GenericEvent.RequestSuccess(PROCESS_ID, responseUpdate, o.getRefId(), o.getId()));
            } else {
                Log.d(getClass().getSimpleName(), "update Response Code :" + rUpdate.getStatusLine().getStatusCode());
                EventBus.getDefault().post(new GenericEvent.RequestFailed(PROCESS_ID, responseUpdate));
            }

//            EventBus.getDefault().post(new GenericEvent.RequestSuccess(PROCESS_ID, responseUpdate, order.getRefId(), order.getId()));
        } else {
            Log.d(getClass().getSimpleName(), "get order menu Response Code :" + rGet.getStatusLine().getStatusCode());
            EventBus.getDefault().post(new GenericEvent.RequestFailed(PROCESS_ID, responseUpdate));
        }
    }

    @Override
    protected void onCancel() {
        EventBus.getDefault().post(new GenericEvent.RequestFailed(PROCESS_ID, responseGet));
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }

}




