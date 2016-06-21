package com.hoqii.fxpc.sales.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.hoqii.fxpc.sales.util.JsonRequestUtils;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by akm on 13/05/16.
 */

public class NotificationOrderTask extends Job {
    private String orderUrl = "/api/orders/";
    private SharedPreferences preferences;
    private String orderId;
    private JsonRequestUtils.HttpResponseWrapper<Order> responseGetOrderById;
    private Order order;
    private List<Order> orders = new ArrayList<Order>();
    private String TAG = getClass().getSimpleName();

    public NotificationOrderTask(String orderId) {
        super(new Params(1).requireNetwork().persist());
        this.orderId = orderId;
    }

    @Override
    public void onAdded() {
        EventBus.getDefault().post(new NotificationOrderEvent(NotificationOrderEvent.NOTIF_STARTED, orders));
    }

    @Override
    public void onRun() throws Throwable {

        preferences = SignageApplication.getInstance().getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        String uriRequest = preferences.getString("server_url", "") + orderUrl + orderId + "?access_token=" + AuthenticationUtils.getCurrentAuthentication().getAccessToken();

        JsonRequestUtils requestUtils = new JsonRequestUtils(uriRequest);
        responseGetOrderById = requestUtils.get(new TypeReference<Order>() {});

        HttpResponse httpResponse = responseGetOrderById.getHttpResponse();
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            order = responseGetOrderById.getContent();
            orders.add(order);

            Log.d("SIZE ORD", String.valueOf(orders.size()));
            EventBus.getDefault().post(new NotificationOrderEvent(NotificationOrderEvent.NOTIF_SUCCESS, orders));
        } else {
            EventBus.getDefault().post(new NotificationOrderEvent(NotificationOrderEvent.NOTIF_FAILED, orders));
        }

    }

    @Override
    protected void onCancel() {
        EventBus.getDefault().post(new NotificationOrderEvent(NotificationOrderEvent.NOTIF_FAILED, orders));
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        EventBus.getDefault().post(new NotificationOrderEvent(NotificationOrderEvent.NOTIF_ERROR, orders));
        return false;
    }

    public static class NotificationOrderEvent {
        public static final int NOTIF_SUCCESS = 0;
        public static final int NOTIF_FAILED = 1;
        public static final int NOTIF_ERROR = 2;
        public static final int NOTIF_STARTED = 3;

        private final int status;
        private List<Order> orders = new ArrayList<Order>();

        public NotificationOrderEvent(int status, List<Order> orders) {
            this.orders = orders;
            this.status = status;
        }

        public int getStatus() {
            return status;
        }

        public List<Order> getOrders() {
            return orders;
        }
    }
}
