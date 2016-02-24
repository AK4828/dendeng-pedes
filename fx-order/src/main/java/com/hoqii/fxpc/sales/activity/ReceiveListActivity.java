package com.hoqii.fxpc.sales.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Fade;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageAppication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.adapter.ReceiveAdapter;
import com.hoqii.fxpc.sales.adapter.SellerOrderAdapter;
import com.hoqii.fxpc.sales.core.LogInformation;
import com.hoqii.fxpc.sales.core.commons.Site;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.entity.Receive;
import com.hoqii.fxpc.sales.entity.Shipment;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.event.LoginEvent;
import com.hoqii.fxpc.sales.job.RefreshTokenJob;
import com.hoqii.fxpc.sales.util.AuthenticationCeck;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.EntypoModule;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.joanzapata.iconify.fonts.IoniconsModule;
import com.joanzapata.iconify.fonts.MaterialCommunityModule;
import com.joanzapata.iconify.fonts.MaterialModule;
import com.joanzapata.iconify.fonts.MeteoconsModule;
import com.joanzapata.iconify.fonts.SimpleLineIconsModule;
import com.joanzapata.iconify.fonts.TypiconsIcons;
import com.joanzapata.iconify.fonts.TypiconsModule;
import com.joanzapata.iconify.fonts.WeathericonsModule;
import com.path.android.jobqueue.JobManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.meruvian.midas.core.service.TaskService;
import org.meruvian.midas.core.util.ConnectionUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by miftakhul on 12/8/15.
 */
public class ReceiveListActivity extends AppCompatActivity implements TaskService {
    private int requestDetailCode = 100;

    private List<Receive> receiveList = new ArrayList<Receive>();
    private SharedPreferences preferences;
    private RecyclerView recyclerView;
    private ReceiveAdapter receiveAdapter;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout dataNull, dataFailed;
    private ProgressDialog loadProgress;
    private int page = 1, totalPage;
    private JobManager jobManager;
    private AuthenticationCeck authenticationCeck = new AuthenticationCeck();
    private ProgressDialog dialogRefresh;

    private String receiveUrl = "/api/order/receives";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_list);
        EventBus.getDefault().register(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new Explode());
            getWindow().setExitTransition(new Explode());
        }

        preferences = getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        jobManager = SignageAppication.getInstance().getJobManager();

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.nav_order_receiving);
        actionBar.setHomeAsUpIndicator(new IconDrawable(this, TypiconsIcons.typcn_chevron_left).colorRes(R.color.white).actionBarSize());

        receiveAdapter = new ReceiveAdapter(this);

        recyclerView = (RecyclerView) findViewById(R.id.receive_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(receiveAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiptRefress);
        swipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.yellow, R.color.blue, R.color.red);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });

        dataNull = (LinearLayout) findViewById(R.id.dataNull);
        dataFailed = (LinearLayout) findViewById(R.id.dataFailed);

        loadProgress = new ProgressDialog(this);
        loadProgress.setMessage("Fetching data...");
        loadProgress.setCancelable(false);

        dialogRefresh = new ProgressDialog(this);
        dialogRefresh.setMessage("Pleace wait ...");
        dialogRefresh.setCancelable(false);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (authenticationCeck.isAccess()) {
                    ReceiveSync receiveSync = new ReceiveSync(ReceiveListActivity.this, ReceiveListActivity.this, false);
                    receiveSync.execute("0");
                    Log.d(getClass().getSimpleName(), "[ acces true / refreshing token not needed]");
                } else {
                    Log.d(getClass().getSimpleName(), "[ acces false / refreshing token]");
                    jobManager.addJobInBackground(new RefreshTokenJob());
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onExecute(int code) {
//        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void onSuccess(int code, Object result) {
        swipeRefreshLayout.setRefreshing(false);
//        receiveAdapter = new ReceiveAdapter(this, receiveList);
//        recyclerView.setAdapter(receiveAdapter);

        receiveAdapter.addItems(receiveList);

        dataFailed.setVisibility(View.GONE);

        if (receiveList.size() > 0) {
            dataNull.setVisibility(View.GONE);
        } else {
            dataNull.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCancel(int code, String message) {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onError(int code, String message) {
        swipeRefreshLayout.setRefreshing(false);
        dataFailed.setVisibility(View.VISIBLE);
    }


    class ReceiveSync extends AsyncTask<String, Void, JSONObject> {

        private Context context;
        private TaskService taskService;
        private boolean isLoadMore = false;

        public ReceiveSync(Context context, TaskService taskService, boolean isLoadMore) {
            this.context = context;
            this.taskService = taskService;
            this.isLoadMore = isLoadMore;
        }


        @Override
        protected JSONObject doInBackground(String... JsonObject) {
            Log.d(getClass().getSimpleName(), "?acces_token= " + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
            return ConnectionUtil.get(preferences.getString("server_url", "") + receiveUrl + "?access_token="
                    + AuthenticationUtils.getCurrentAuthentication().getAccessToken() + "&page=" + JsonObject[0]);
        }

        @Override
        protected void onCancelled() {
            taskService.onCancel(SignageVariables.RECEIVE_GET_TASK, "Batal");
        }

        @Override
        protected void onPreExecute() {
            if (!isLoadMore) {
                swipeRefreshLayout.setRefreshing(true);
            }
            taskService.onExecute(SignageVariables.RECEIVE_GET_TASK);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {

                    List<Receive> receives = new ArrayList<Receive>();

                    totalPage = result.getInt("totalPages");
                    Log.d("result order =====", result.toString());
                    JSONArray jsonArray = result.getJSONArray("content");
                    for (int a = 0; a < jsonArray.length(); a++) {
                        JSONObject object = jsonArray.getJSONObject(a);

                        Receive receive = new Receive();
                        receive.setId(object.getString("id"));

                        JSONObject logInformationObject = new JSONObject();
                        if (!object.isNull("logInformation")) {
                            logInformationObject = object.getJSONObject("logInformation");

                            LogInformation logInformation = new LogInformation();
                            logInformation.setCreateDate(new Date(logInformationObject.getLong("createDate")));

                            receive.setLogInformation(logInformation);
                        }

                        JSONObject shipmentObject = new JSONObject();
                        if (!object.isNull("shipment")) {
                            shipmentObject = object.getJSONObject("shipment");
                            Shipment shipment = new Shipment();
                            shipment.setId(shipmentObject.getString("id"));
                            shipment.setReceiptNumber(shipmentObject.getString("receiptNumber"));
                            shipment.setDeliveryServiceName(shipmentObject.getString("deliveryServiceName"));
                            if (shipmentObject.getString("status").equalsIgnoreCase("WAIT")) {
                                shipment.setStatus(Shipment.ShipmentStatus.WAIT);
                            } else if (shipmentObject.getString("status").equalsIgnoreCase("DELIVERED")) {
                                shipment.setStatus(Shipment.ShipmentStatus.DELIVERED);
                            } else if (shipmentObject.getString("status").equalsIgnoreCase("FAILED")) {
                                shipment.setStatus(Shipment.ShipmentStatus.FAILED);
                            }

                            JSONObject shipmentLogInformationObject = new JSONObject();
                            if (!shipmentObject.isNull("logInformation")) {
                                shipmentLogInformationObject = shipmentObject.getJSONObject("logInformation");

                                LogInformation logInformation = new LogInformation();
                                logInformation.setCreateDate(new Date(shipmentLogInformationObject.getLong("createDate")));

                                shipment.setLogInformation(logInformation);
                            }

                            receive.setShipment(shipment);
                        }

                        if (object.getString("status").equalsIgnoreCase("WAIT")) {
                            receive.setStatus(Receive.ReceiveStatus.WAIT);
                        } else if (object.getString("status").equalsIgnoreCase("RECEIVED")) {
                            receive.setStatus(Receive.ReceiveStatus.RECEIVED);
                        } else if (object.getString("status").equalsIgnoreCase("FAILED")) {
                            receive.setStatus(Receive.ReceiveStatus.FAILED);
                        }

                        receive.setRecipient(object.getString("recipient"));

                        JSONObject orderObject = new JSONObject();
                        if (!object.isNull("order")) {
                            orderObject = object.getJSONObject("order");

                            Order order = new Order();
                            order.setId(orderObject.getString("id"));
                            order.setReceiptNumber(orderObject.getString("receiptNumber"));

                            JSONObject orderLogInformationObject = new JSONObject();
                            if (!orderObject.isNull("logInformation")) {
                                orderLogInformationObject = orderObject.getJSONObject("logInformation");

                                LogInformation logInformation = new LogInformation();
                                logInformation.setCreateDate(new Date(orderLogInformationObject.getLong("createDate")));

                                order.setLogInformation(logInformation);
                            }

                            JSONObject siteObject = new JSONObject();
                            if (!orderObject.isNull("site")) {
                                siteObject = orderObject.getJSONObject("site");

                                Site site = new Site();
                                site.setId(siteObject.getString("id"));
                                site.setName(siteObject.getString("name"));
                                site.setDescription(siteObject.getString("description"));
                                order.setSite(site);
                            }
                            receive.setOrder(order);
                        }
                        receives.add(receive);
                    }

                    if (isLoadMore) {
                        page++;
                        loadProgress.dismiss();
                    }

                    receiveList = receives;
                    taskService.onSuccess(SignageVariables.RECEIVE_GET_TASK, true);
                } else {
                    taskService.onError(SignageVariables.RECEIVE_GET_TASK, "Error");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                taskService.onError(SignageVariables.RECEIVE_GET_TASK, "Error");
            }


        }
    }

    private void refreshContent() {
        receiveAdapter = new ReceiveAdapter(this);
        recyclerView.setAdapter(receiveAdapter);

        for (int x = 0; x < page; x++) {
            final int finalX = x;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ReceiveSync receiveSync = new ReceiveSync(ReceiveListActivity.this, ReceiveListActivity.this, false);
                    receiveSync.execute(Integer.toString(finalX));
                }
            }, 500);
        }
    }

    public void loadMoreContent() {
        if (page < totalPage) {
            loadProgress.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ReceiveSync receiveSync = new ReceiveSync(ReceiveListActivity.this, ReceiveListActivity.this, true);
                    receiveSync.execute(Integer.toString(page));
                }
            }, 500);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == requestDetailCode) {
            Log.d(getClass().getSimpleName(), "result ok");
            if (data != null) {
                String receiveId = data.getStringExtra("receiveId");
                Log.d(getClass().getSimpleName(), "receive id " + receiveId);
                receiveAdapter.updateStatusDelivered(receiveId);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void openReceiveDetail(Intent intent) {
        startActivityForResult(intent, requestDetailCode);
    }

    private void AlertMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ReceiveListActivity.this);
        builder.setTitle("Refresh Token");
        builder.setMessage(message);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    public void onEventMainThread(GenericEvent.RequestInProgress requestInProgress) {
        Log.d(getClass().getSimpleName(), "RequestInProgress: " + requestInProgress.getProcessId());
        switch (requestInProgress.getProcessId()) {
            case RefreshTokenJob.PROCESS_ID:
                dialogRefresh.show();
                break;
        }
    }

    public void onEventMainThread(GenericEvent.RequestSuccess requestSuccess) {
        Log.d(getClass().getSimpleName(), "RequestSuccess: " + requestSuccess.getProcessId());

    }

    public void onEventMainThread(GenericEvent.RequestFailed failed) {
        Log.d(getClass().getSimpleName(), "RequestFailed: " + failed.getProcessId());
        switch (failed.getProcessId()) {
            case RefreshTokenJob.PROCESS_ID:
                dialogRefresh.dismiss();
                AlertMessage("Refresh token failed");
                dataFailed.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void onEventMainThread(LoginEvent.LoginSuccess loginSuccess) {
        dialogRefresh.dismiss();
        ReceiveSync receiveSync = new ReceiveSync(ReceiveListActivity.this, ReceiveListActivity.this, false);
        receiveSync.execute("0");
    }

    public void onEventMainThread(LoginEvent.LoginFailed loginFailed) {
        dialogRefresh.dismiss();
        dataFailed.setVisibility(View.VISIBLE);
    }
}
