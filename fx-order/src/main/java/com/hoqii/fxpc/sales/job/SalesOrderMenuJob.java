package com.hoqii.fxpc.sales.job;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.content.database.adapter.SalesOrderMenuDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.SalesOrder;
import com.hoqii.fxpc.sales.entity.SalesOrderMenu;
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
public class SalesOrderMenuJob extends Job{
    private JsonRequestUtils.HttpResponseWrapper<SalesOrderMenu> responsePost;

    private String url, salesOrderId;
    private SalesOrderMenu salesOrderMenu;
    private SalesOrderMenuDatabaseAdapter salesOrderMenuDatabaseAdapter;
    private String salesOrderMenuId;


    protected SalesOrderMenuJob() {
        super(new Params(1).requireNetwork());
    }

    public SalesOrderMenuJob(String url, String salesOrderId, SalesOrderMenu salesOrderMenu){
        super(new Params(1).requireNetwork());
        this.url = url;
        this.salesOrderId = salesOrderId;
        this.salesOrderMenu = salesOrderMenu;
        this.salesOrderMenuId = salesOrderMenu.getId();
        salesOrderMenuDatabaseAdapter = new SalesOrderMenuDatabaseAdapter(SignageApplication.getInstance());
    }

    @Override
    public void onAdded() {
        Log.d(getClass().getSimpleName(), "so menu job added");
        EventBus.getDefault().post(new GenericEvent.RequestInProgress(SignageVariables.SALES_MENU_POST_TASK));
    }

    @Override
    public void onRun() throws Throwable {
        Log.d(getClass().getSimpleName(), "so menu job run");
        JsonRequestUtils request = new JsonRequestUtils(url + "/api/salesorders/"+salesOrderId+"/menu");
        salesOrderMenu.setId(null);
        responsePost = request.post(salesOrderMenu, new TypeReference<SalesOrderMenu>() {
        });

        HttpResponse httpResponse = responsePost.getHttpResponse();
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK){
            SalesOrderMenu salesOrderMenu = responsePost.getContent();
            Log.d(getClass().getSimpleName(), "so menu job run success, "+salesOrderMenu.getProduct().getName());
            salesOrderMenuDatabaseAdapter.updateSyncStatusById(salesOrderMenuId);
            EventBus.getDefault().post(new GenericEvent.RequestSuccess(SignageVariables.SALES_MENU_POST_TASK, responsePost, salesOrderMenu.getId(), null));
        }else {
            Log.d(getClass().getSimpleName(), "so menu job failed");
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
