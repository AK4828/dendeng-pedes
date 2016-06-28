package com.hoqii.fxpc.sales.job;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.content.database.adapter.SalesOrderSerialDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.SalesOrderMenu;
import com.hoqii.fxpc.sales.entity.SalesOrderMenuSerial;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.util.JsonRequestUtils;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import de.greenrobot.event.EventBus;

/**
 * Created by miftakhul on 23/06/16.
 */
public class SalesOrderMenuSerialJob extends Job{
    private JsonRequestUtils.HttpResponseWrapper<SalesOrderMenuSerial> responsePost;

    private String url;
    private SalesOrderMenuSerial orderMenuSerial;
    private SalesOrderSerialDatabaseAdapter databaseAdapter;


    protected SalesOrderMenuSerialJob() {
        super(new Params(1).requireNetwork());
    }

    public SalesOrderMenuSerialJob(String url, SalesOrderMenuSerial salesOrderMenuSerial){
        super(new Params(1).requireNetwork());
        this.url = url;
        this.orderMenuSerial = salesOrderMenuSerial;
    }

    @Override
    public void onAdded() {
        Log.d(getClass().getSimpleName(), "so menu serial job added");
        EventBus.getDefault().post(new GenericEvent.RequestInProgress(SignageVariables.SALES_MENU_SERIAL_POST_TASK));
    }

    @Override
    public void onRun() throws Throwable {
        databaseAdapter = new SalesOrderSerialDatabaseAdapter(SignageApplication.getInstance());
        SalesOrderMenuSerial salesSerial = orderMenuSerial;
        salesSerial.setId(null);
        Log.d(getClass().getSimpleName(), "so menu serial job run");
        JsonRequestUtils request = new JsonRequestUtils(url + "api/order/menu/serialnumbers");
        responsePost = request.post(salesSerial, new TypeReference<SalesOrderMenuSerial>() {
        });

        HttpResponse httpResponse = responsePost.getHttpResponse();
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
            SalesOrderMenuSerial salesOrderMenuSerial = responsePost.getContent();
            Log.d(getClass().getSimpleName(), "so MENU SERIAL job success, "+salesOrderMenuSerial.getSerialNumber());
            EventBus.getDefault().post(new GenericEvent.RequestSuccess(SignageVariables.SALES_MENU_POST_TASK, responsePost, salesOrderMenuSerial.getId(), null));

            databaseAdapter.updateStatusFlag(orderMenuSerial.getId());
        }else {
            Log.d(getClass().getSimpleName(), "so menu serial job failed");
            EventBus.getDefault().post(new GenericEvent.RequestFailed(SignageVariables.SALES_MENU_POST_TASK, responsePost));
        }
    }

    @Override
    protected void onCancel() {

    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        return false;
    }
}
