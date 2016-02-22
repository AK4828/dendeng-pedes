package com.hoqii.fxpc.sales.job;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hoqii.fxpc.sales.SignageAppication;
import com.hoqii.fxpc.sales.content.database.adapter.OrderMenuDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.OrderMenu;
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
public class OrderMenuJob extends Job {
    public static final int PROCESS_ID = 34;
//    private JsonRequestUtils.HttpResponseWrapper<PageEntity<OrderMenu>> response;
    private JsonRequestUtils.HttpResponseWrapper<OrderMenu> response;
    private String orderMenuId;
    private String orderRefId;
    private String url;

    public OrderMenuJob(String orderRefId, String orderMenuId, String url) {
        super(new Params(Priority.HIGH).requireNetwork().persist());
        this.orderMenuId = orderMenuId;
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
        JsonRequestUtils request = new JsonRequestUtils(new Formatter().format(url + ESalesUri.ORDER_MENU, orderRefId).toString());

        OrderMenuDatabaseAdapter orderMenuDatabaseAdapter = new OrderMenuDatabaseAdapter(SignageAppication.getInstance());
        OrderMenu orderMenu = orderMenuDatabaseAdapter.findOrderMenuById(orderMenuId);
        orderMenu.setId(null);
        orderMenu.getOrder().setId(orderRefId);

        Log.d(getClass().getSimpleName(), "Order Menu Create By : " + orderMenu.getLogInformation().getCreateBy());
        Log.d(getClass().getSimpleName(), "Order Menu Site By : " + orderMenu.getLogInformation().getSite());
        Log.d(getClass().getSimpleName(), "Order Menu order qty : " + orderMenu.getQty());
        Log.d(getClass().getSimpleName(), "Order Menu order qty order : " + orderMenu.getQtyOrder());

        Log.d(getClass().getSimpleName(), "OrderMenu Id:" + orderMenu.getId());
        response = request.post(orderMenu, new TypeReference<OrderMenu>() {});
        HttpResponse r = response.getHttpResponse();

        if (r.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            OrderMenu om = response.getContent();
            orderMenuDatabaseAdapter.updateSyncStatusById(orderMenuId);

            Log.d(getClass().getSimpleName(), "Response Code :" + r.getStatusLine().getStatusCode() + "Entity ID :" + orderMenuId + " Ref Id: " + om.getId());
            EventBus.getDefault().post(new GenericEvent.RequestSuccess(PROCESS_ID, response, om.getId(),orderMenuId));
        } else {
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
