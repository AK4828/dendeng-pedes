package com.hoqii.fxpc.sales.job;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.content.database.adapter.SalesOrderDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.SalesOrder;
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
public class SalesOrderJob extends Job{
    private JsonRequestUtils.HttpResponseWrapper<SalesOrder> responseGet;

    private String url;
    private SalesOrder salesOrder;
    private SalesOrderDatabaseAdapter databaseAdapter;

    protected SalesOrderJob() {
        super(new Params(1).requireNetwork());
    }

    public SalesOrderJob(String url){
        super(new Params(1).requireNetwork());
        this.url = url;
    }

    @Override
    public void onAdded() {
        Log.d(getClass().getSimpleName(), "so job added");
        EventBus.getDefault().post(new GenericEvent.RequestInProgress(SignageVariables.SALES_RECEIPT_TASK));
    }

    @Override
    public void onRun() throws Throwable {
        databaseAdapter = new SalesOrderDatabaseAdapter(SignageApplication.getInstance());

        Log.d(getClass().getSimpleName(), "so job run");
        JsonRequestUtils request = new JsonRequestUtils(url + "/api/salesorders/receiptnumber");

        responseGet = request.get(new TypeReference<SalesOrder>() {
        });



        HttpResponse httpResponse = responseGet.getHttpResponse();
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
            SalesOrder salesOrder = responseGet.getContent();
            Log.d(getClass().getSimpleName(), "so get job success, "+salesOrder.getName());
            EventBus.getDefault().post(new GenericEvent.RequestSuccess(SignageVariables.SALES_RECEIPT_TASK, responseGet, salesOrder.getId(), null));
            databaseAdapter.saveSalesOrder(salesOrder);
        }else {
            Log.d(getClass().getSimpleName(), "so get job failed");
            EventBus.getDefault().post(new GenericEvent.RequestFailed(SignageVariables.SALES_RECEIPT_TASK, responseGet));
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
