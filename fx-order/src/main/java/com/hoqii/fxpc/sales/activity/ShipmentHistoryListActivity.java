package com.hoqii.fxpc.sales.activity;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.adapter.ShipmentHistoryAdapter;
import com.hoqii.fxpc.sales.core.LogInformation;
import com.hoqii.fxpc.sales.core.commons.Site;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.entity.Shipment;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.event.LoginEvent;
import com.hoqii.fxpc.sales.job.RefreshTokenJob;
import com.hoqii.fxpc.sales.util.AuthenticationCeck;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.TypiconsIcons;
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
public class ShipmentHistoryListActivity extends AppCompatActivity implements TaskService {
    private static final int REFRESH_TOKEN_HISTORY_SHIPMENT = 300;
    private List<Shipment> shipmentList = new ArrayList<Shipment>();
    private SharedPreferences preferences;
    private RecyclerView recyclerView;
    private ShipmentHistoryAdapter shipmentAdapter;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressDialog progressDialog;
    private LinearLayout dataNull, dataFailed;
    private ProgressDialog loadProgress;
    private int page = 1, totalPage;
    private AuthenticationCeck authenticationCeck = new AuthenticationCeck();

    private String shipmentUrl = "/api/shipmentHistory";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_shipment_list);
        EventBus.getDefault().register(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new Explode());
            getWindow().setExitTransition(new Explode());
        }

        preferences = getSharedPreferences(SignageVariables.PREFS_SERVER, 0);

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.nav_shipment_history_list);
        actionBar.setHomeAsUpIndicator(new IconDrawable(this, TypiconsIcons.typcn_chevron_left).colorRes(R.color.white).actionBarSize());

        shipmentAdapter = new ShipmentHistoryAdapter(this);

        recyclerView = (RecyclerView) findViewById(R.id.shipment_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(shipmentAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiptRefress);
        swipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.yellow, R.color.blue, R.color.red);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Confirm sipment");

        dataNull = (LinearLayout) findViewById(R.id.dataNull);
        dataFailed = (LinearLayout) findViewById(R.id.dataFailed);

        loadProgress = new ProgressDialog(this);
        loadProgress.setMessage("Fetching data...");
        loadProgress.setCancelable(false);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (authenticationCeck.isAccess()) {
                    ShipmentSync sync = new ShipmentSync(ShipmentHistoryListActivity.this, ShipmentHistoryListActivity.this, false);
                    sync.execute("0");
                    Log.d(getClass().getSimpleName(), "[ acces true / refreshing token not needed]");
                } else {
                    Log.d(getClass().getSimpleName(), "[ acces false / refreshing token]");
                    authenticationCeck.refreshToken(ShipmentHistoryListActivity.this, REFRESH_TOKEN_HISTORY_SHIPMENT);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                shipmentAdapter = new ShipmentHistoryAdapter(ShipmentHistoryListActivity.this);
                recyclerView.setAdapter(shipmentAdapter);

                ShipmentSyncSearch s = new ShipmentSyncSearch(ShipmentHistoryListActivity.this, ShipmentHistoryListActivity.this, false);
                s.execute(query, "0");
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                refreshContent();
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
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
        Log.d(getClass().getSimpleName(), "shipment list size " + shipmentList.size());
        shipmentAdapter.addItems(shipmentList);

        dataFailed.setVisibility(View.GONE);

        if (shipmentList.size() > 0) {
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

    public void onEventMainThread(GenericEvent.RequestSuccess requestSuccess) {
        Log.d(getClass().getSimpleName(), "RequestSuccess: " + requestSuccess.getProcessId());
        switch (requestSuccess.getProcessId()){
            case REFRESH_TOKEN_HISTORY_SHIPMENT:
                ShipmentSync sync = new ShipmentSync(ShipmentHistoryListActivity.this, ShipmentHistoryListActivity.this, false);
                sync.execute("0");
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    class ShipmentSync extends AsyncTask<String, Void, JSONObject> {

        private Context context;
        private TaskService taskService;
        private boolean isLoadMore = false;

        public ShipmentSync(Context context, TaskService taskService, boolean isLoadMore) {
            this.context = context;
            this.taskService = taskService;
            this.isLoadMore = isLoadMore;
        }


        @Override
        protected JSONObject doInBackground(String... JsonObject) {
            Log.d(getClass().getSimpleName(), "?acces_token= " + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
            return ConnectionUtil.get(preferences.getString("server_url", "") + shipmentUrl + "?access_token="
                    + AuthenticationUtils.getCurrentAuthentication().getAccessToken() + "&page=" + JsonObject[0]);
        }

        @Override
        protected void onCancelled() {
            taskService.onCancel(SignageVariables.SELLER_SHIPMENT_GET_TASK, "Batal");
        }

        @Override
        protected void onPreExecute() {
            if (!isLoadMore) {
                swipeRefreshLayout.setRefreshing(true);
            }
            taskService.onExecute(SignageVariables.SELLER_SHIPMENT_GET_TASK);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {

                    List<Shipment> shipments = new ArrayList<Shipment>();

                    totalPage = result.getInt("totalPages");
                    Log.d("result shipment =====", result.toString());
                    JSONArray jsonArray = result.getJSONArray("content");
                    for (int a = 0; a < jsonArray.length(); a++) {
                        JSONObject object = jsonArray.getJSONObject(a);

                        Shipment shipment = new Shipment();
                        shipment.setId(object.getString("id"));
                        shipment.setReceiptNumber(object.getString("receiptNumber"));
                        shipment.setDeliveryServiceName(object.getString("deliveryServiceName"));
                        if (object.getString("status").equalsIgnoreCase("WAIT")) {
                            shipment.setStatus(Shipment.ShipmentStatus.WAIT);
                        } else if (object.getString("status").equalsIgnoreCase("DELIVERED")) {
                            shipment.setStatus(Shipment.ShipmentStatus.DELIVERED);
                        } else if (object.getString("status").equalsIgnoreCase("FAILED")) {
                            shipment.setStatus(Shipment.ShipmentStatus.FAILED);
                        }

                        JSONObject logInformationObject = new JSONObject();
                        if (!object.isNull("logInformation")) {
                            logInformationObject = object.getJSONObject("logInformation");

                            LogInformation logInformation = new LogInformation();
                            logInformation.setCreateDate(new Date(logInformationObject.getLong("createDate")));

                            shipment.setLogInformation(logInformation);
                        }

                        JSONObject orderObject = new JSONObject();
                        if (!object.isNull("order")) {
                            orderObject = object.getJSONObject("order");

                            Order order = new Order();
                            order.setId(orderObject.getString("id"));
                            order.setReceiptNumber(orderObject.getString("receiptNumber"));

                            JSONObject orderlogInformationObject = new JSONObject();
                            if (!orderObject.isNull("logInformation")) {
                                orderlogInformationObject = orderObject.getJSONObject("logInformation");

                                LogInformation logInformation = new LogInformation();
                                logInformation.setCreateDate(new Date(orderlogInformationObject.getLong("createDate")));

                                order.setLogInformation(logInformation);
                            }

                            JSONObject siteObject = new JSONObject();
                            if (!orderObject.isNull("siteFrom")) {
                                siteObject = orderObject.getJSONObject("siteFrom");
                                Site siteFrom = new Site();
                                siteFrom.setId(siteObject.getString("id"));
                                siteFrom.setName(siteObject.getString("name"));
                                siteFrom.setEmail(siteObject.getString("email"));
                                siteFrom.setPhone(siteObject.getString("phone"));
                                siteFrom.setAddress(siteObject.getString("address"));
                                siteFrom.setFax(siteObject.getString("fax"));
                                siteFrom.setPostalCode(siteObject.getString("postalCode"));
                                siteFrom.setCity(siteObject.getString("city"));

                                order.setSiteFrom(siteFrom);
                            }
                            shipment.setOrder(order);
                        }
                        shipments.add(shipment);
                    }

                    if (isLoadMore) {
                        page++;
                        loadProgress.dismiss();
                    }
                    shipmentList = shipments;

                    taskService.onSuccess(SignageVariables.SELLER_SHIPMENT_GET_TASK, true);
                } else {
                    taskService.onError(SignageVariables.SELLER_SHIPMENT_GET_TASK, "Error");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                taskService.onError(SignageVariables.SELLER_SHIPMENT_GET_TASK, "Error");
            }

        }
    }

    class ShipmentSyncSearch extends AsyncTask<String, Void, JSONObject> {

        private Context context;
        private TaskService taskService;
        private boolean isLoadMore = false;

        public ShipmentSyncSearch(Context context, TaskService taskService, boolean isLoadMore) {
            this.context = context;
            this.taskService = taskService;
            this.isLoadMore = isLoadMore;
        }


        @Override
        protected JSONObject doInBackground(String... JsonObject) {
            Log.d(getClass().getSimpleName(), "?acces_token= " + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
            return ConnectionUtil.get(preferences.getString("server_url", "") + shipmentUrl + "?access_token="
                    + AuthenticationUtils.getCurrentAuthentication().getAccessToken() + "&q=" + JsonObject[0] + "&page=" + JsonObject[1]);
        }

        @Override
        protected void onCancelled() {
            taskService.onCancel(SignageVariables.SELLER_SHIPMENT_GET_TASK, "Batal");
        }

        @Override
        protected void onPreExecute() {
            if (!isLoadMore) {
                swipeRefreshLayout.setRefreshing(true);
            }
            taskService.onExecute(SignageVariables.SELLER_SHIPMENT_GET_TASK);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {

                    List<Shipment> shipments = new ArrayList<Shipment>();

                    totalPage = result.getInt("totalPages");
                    Log.d("result shipment =====", result.toString());
                    JSONArray jsonArray = result.getJSONArray("content");
                    for (int a = 0; a < jsonArray.length(); a++) {
                        JSONObject object = jsonArray.getJSONObject(a);

                        Shipment shipment = new Shipment();
                        shipment.setId(object.getString("id"));
                        shipment.setReceiptNumber(object.getString("receiptNumber"));
                        shipment.setDeliveryServiceName(object.getString("deliveryServiceName"));
                        if (object.getString("status").equalsIgnoreCase("WAIT")) {
                            shipment.setStatus(Shipment.ShipmentStatus.WAIT);
                        } else if (object.getString("status").equalsIgnoreCase("DELIVERED")) {
                            shipment.setStatus(Shipment.ShipmentStatus.DELIVERED);
                        } else if (object.getString("status").equalsIgnoreCase("FAILED")) {
                            shipment.setStatus(Shipment.ShipmentStatus.FAILED);
                        }

                        JSONObject logInformationObject = new JSONObject();
                        if (!object.isNull("logInformation")) {
                            logInformationObject = object.getJSONObject("logInformation");

                            LogInformation logInformation = new LogInformation();
                            logInformation.setCreateDate(new Date(logInformationObject.getLong("createDate")));

                            shipment.setLogInformation(logInformation);
                        }

                        JSONObject orderObject = new JSONObject();
                        if (!object.isNull("order")) {
                            orderObject = object.getJSONObject("order");

                            Order order = new Order();
                            order.setId(orderObject.getString("id"));
                            order.setReceiptNumber(orderObject.getString("receiptNumber"));

                            JSONObject orderlogInformationObject = new JSONObject();
                            if (!orderObject.isNull("logInformation")) {
                                orderlogInformationObject = orderObject.getJSONObject("logInformation");

                                LogInformation logInformation = new LogInformation();
                                logInformation.setCreateDate(new Date(orderlogInformationObject.getLong("createDate")));

                                order.setLogInformation(logInformation);
                            }

                            JSONObject siteObject = new JSONObject();
                            if (!orderObject.isNull("siteFrom")) {
                                siteObject = orderObject.getJSONObject("siteFrom");
                                Site siteFrom = new Site();
                                siteFrom.setId(siteObject.getString("id"));
                                siteFrom.setName(siteObject.getString("name"));
                                siteFrom.setEmail(siteObject.getString("email"));
                                siteFrom.setPhone(siteObject.getString("phone"));
                                siteFrom.setAddress(siteObject.getString("address"));
                                siteFrom.setFax(siteObject.getString("fax"));
                                siteFrom.setPostalCode(siteObject.getString("postalCode"));
                                siteFrom.setCity(siteObject.getString("city"));

                                order.setSiteFrom(siteFrom);
                            }
                            shipment.setOrder(order);
                        }
                        shipments.add(shipment);
                    }

                    if (isLoadMore) {
                        page++;
                        loadProgress.dismiss();
                    }
                    shipmentList = shipments;

                    taskService.onSuccess(SignageVariables.SELLER_SHIPMENT_GET_TASK, true);
                } else {
                    taskService.onError(SignageVariables.SELLER_SHIPMENT_GET_TASK, "Error");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                taskService.onError(SignageVariables.SELLER_SHIPMENT_GET_TASK, "Error");
            }

        }
    }

    private void refreshContent() {
        shipmentAdapter = new ShipmentHistoryAdapter(this);
        recyclerView.setAdapter(shipmentAdapter);

        for (int x = 0; x < page; x++) {
            final int finalX = x;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ShipmentSync sync = new ShipmentSync(ShipmentHistoryListActivity.this, ShipmentHistoryListActivity.this, false);
                    sync.execute(Integer.toString(finalX));
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
                    ShipmentSync sync = new ShipmentSync(ShipmentHistoryListActivity.this, ShipmentHistoryListActivity.this, true);
                    sync.execute(Integer.toString(page));
                }
            }, 500);
        }
    }

}
