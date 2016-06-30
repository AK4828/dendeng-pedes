package com.hoqii.fxpc.sales.job;

import android.content.SharedPreferences;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.entity.PageEntity;
import com.hoqii.fxpc.sales.entity.SalesOrder;
import com.hoqii.fxpc.sales.entity.SalesOrderMenu;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.hoqii.fxpc.sales.util.JsonRequestUtils;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.meruvian.midas.core.event.Event;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by akm on 30/06/16.
 */
public class SalesReportJob extends Job {

    private String productId;
    private long startDate;
    private long endDate;
    private List<SalesOrderMenu> salesOrderMenus = new ArrayList<>();
    private SharedPreferences preferences;
    private String salesReportUrl = "/api/salesorders/report/";
    private JsonRequestUtils.HttpResponseWrapper<PageEntity<SalesOrderMenu>> responseGetReport;

    public SalesReportJob(String productId, long startDate, long endDate) {
        super(new Params(1).requireNetwork().persist());
        this.productId = productId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public void onAdded() {
        salesOrderMenus.clear();
        EventBus.getDefault().post(new SalesReportEvent(SalesReportEvent.STARTED, salesOrderMenus));
    }

    @Override
    public void onRun() throws Throwable {
        preferences = SignageApplication.getInstance().getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        String requestUrl = preferences.getString("server_url", "") + salesReportUrl + productId +"/"+ startDate + "/to/" + endDate + "?access_token="+ AuthenticationUtils.getCurrentAuthentication().getAccessToken();
        JsonRequestUtils requestUtils = new JsonRequestUtils(requestUrl);
        responseGetReport = requestUtils.get(new TypeReference<PageEntity<SalesOrderMenu>>() {});

        HttpResponse httpResponse = responseGetReport.getHttpResponse();
        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            salesOrderMenus = responseGetReport.getContent().getContent();

            EventBus.getDefault().post(new SalesReportEvent(SalesReportEvent.SUCCESS, salesOrderMenus));
        } else {
            EventBus.getDefault().post(new SalesReportEvent(SalesReportEvent.FAILED, salesOrderMenus));
        }
    }

    @Override
    protected void onCancel() {
        EventBus.getDefault().post(new SalesReportEvent(SalesReportEvent.FAILED, salesOrderMenus));
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        EventBus.getDefault().post(new SalesReportEvent(SalesReportEvent.ERROR, salesOrderMenus));
        return false;
    }

    public static class SalesReportEvent {
        public static final int SUCCESS = 0;
        public static final int FAILED = 1;
        public static final int ERROR = 2;
        public static final int STARTED = 3;

        private final int status;
        private final List<SalesOrderMenu> salesOrderMenus;

        public SalesReportEvent(int status, List<SalesOrderMenu> salesOrderMenus) {
            this.status = status;
            this.salesOrderMenus = salesOrderMenus;
        }

        public int getStatus() {
            return status;
        }

        public List<SalesOrderMenu> getSalesOrderMenus() {
            return salesOrderMenus;
        }
    }
}
