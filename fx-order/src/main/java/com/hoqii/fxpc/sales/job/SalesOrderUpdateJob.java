package com.hoqii.fxpc.sales.job;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hoqii.fxpc.sales.SignageVariables;
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
public class SalesOrderUpdateJob extends Job{
    private JsonRequestUtils.HttpResponseWrapper<SalesOrder> responsePost;

    private String url;
    private SalesOrder salesOrder;


    protected SalesOrderUpdateJob() {
        super(new Params(1).requireNetwork());
    }

    public SalesOrderUpdateJob(String url, SalesOrder salesOrder){
        super(new Params(1).requireNetwork());
        this.url = url;
        this.salesOrder = salesOrder;
    }

    @Override
    public void onAdded() {
        Log.d(getClass().getSimpleName(), "so job added");
        EventBus.getDefault().post(new GenericEvent.RequestInProgress(SignageVariables.SALES_POST_TASK));
    }

    @Override
    public void onRun() throws Throwable {
        Log.d(getClass().getSimpleName(), "so job run");
        JsonRequestUtils request = new JsonRequestUtils(url + "/api/salesorders/"+salesOrder.getId());

        responsePost = request.put(salesOrder, new TypeReference<SalesOrder>() {
        });

        HttpResponse httpResponse = responsePost.getHttpResponse();
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
            SalesOrder salesOrder = responsePost.getContent();
            Log.d(getClass().getSimpleName(), "so job success, "+salesOrder.getName());
            EventBus.getDefault().post(new GenericEvent.RequestSuccess(SignageVariables.SALES_POST_TASK, responsePost, salesOrder.getId(), null));
        }else {
            Log.d(getClass().getSimpleName(), "so job failed");
            EventBus.getDefault().post(new GenericEvent.RequestFailed(SignageVariables.SALES_POST_TASK, responsePost));
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
