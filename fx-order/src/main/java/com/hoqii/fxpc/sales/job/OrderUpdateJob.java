package com.hoqii.fxpc.sales.job;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hoqii.fxpc.sales.SignageAppication;
import com.hoqii.fxpc.sales.content.database.adapter.OrderDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.util.JsonRequestUtils;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.meruvian.midas.core.job.Priority;

import java.util.Formatter;

import de.greenrobot.event.EventBus;

/**
 * Created by meruvian on 30/07/15.
 */
public class OrderUpdateJob extends Job {
    public static final int PROCESS_ID = 33;
//    private JsonRequestUtils.HttpResponseWrapper<PageEntity<Order>> response;
    private JsonRequestUtils.HttpResponseWrapper<Order> response;
    private String orderId;
    private String orderRefId;
    private String url;

    public OrderUpdateJob(String orderRefId,String orderId, String url) {
        super(new Params(Priority.HIGH).requireNetwork().persist());
        this.orderId = orderId;
        this.url = url;
        this.orderRefId = orderRefId;
    }

    @Override
    public void onAdded() {
        Log.d(getClass().getSimpleName(), "onAdded");
        EventBus.getDefault().post(new GenericEvent.RequestInProgress(PROCESS_ID));
    }

    @Override
    public void onRun() throws Throwable {
        Log.d(getClass().getSimpleName(), "onRun");
        JsonRequestUtils request = new JsonRequestUtils(new Formatter().format(url + ESalesUri.UPDATE_ORDER, orderRefId).toString());

        OrderDatabaseAdapter orderDatabaseAdapter = new OrderDatabaseAdapter(SignageAppication.getInstance());
        Order order = orderDatabaseAdapter.findOrderById(orderId);

        order.setId(orderRefId);

        Log.d(getClass().getSimpleName(), "Order Id:" + order.getReceiptNumber());
        response = request.put(order, new TypeReference<Order>() {});
        HttpResponse r = response.getHttpResponse();

        if (r.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            Order o = response.getContent();
//            orderDatabaseAdapter.updateSyncStatusById(orderId);

            Log.d(getClass().getSimpleName(), "Response Code :" + r.getStatusLine().getStatusCode() + "Entity ID :" + orderId + "Ref ID :" + o.getId());
            EventBus.getDefault().post(new GenericEvent.RequestSuccess(PROCESS_ID, response, o.getId(), orderId));
        } else {
            EventBus.getDefault().post(new GenericEvent.RequestFailed(PROCESS_ID, response));
            Log.d(getClass().getSimpleName(), "Response Code :" + r.getStatusLine().getStatusCode() + " " + r.getStatusLine().getReasonPhrase());
            throw new RuntimeException();
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
