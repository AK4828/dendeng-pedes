package com.hoqii.fxpc.sales.activity;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
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
import android.widget.Toast;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.adapter.RecycleOnverticalScrollListener;
import com.hoqii.fxpc.sales.adapter.SelfHistoryOrderAdapter;
import com.hoqii.fxpc.sales.adapter.SellerOrderAdapter;
import com.hoqii.fxpc.sales.core.LogInformation;
import com.hoqii.fxpc.sales.core.commons.Site;
import com.hoqii.fxpc.sales.entity.Order;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.meruvian.midas.core.service.TaskService;
import org.meruvian.midas.core.util.ConnectionUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by miftakhul on 12/8/15.
 */
public class SelfHistoryOrderListActivity extends AppCompatActivity implements TaskService {

    private int requestOrderMenuActivityCode = 122;
    private List<Order> orderList = new ArrayList<Order>();
    private SharedPreferences preferences;
    private RecyclerView recyclerView;
    private SelfHistoryOrderAdapter selfHistoryOrderAdapter;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout dataNull, dataFailed;
    private boolean isMinLoli = false;
    private String orderHistoryUrl = "/api/orderHistory";
    private ProgressDialog loadProgress;
    private int page = 1, totalPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_history_order_list);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isMinLoli = true;
            getWindow().setEnterTransition(new Explode());
            getWindow().setExitTransition(new Explode());
        } else {
            isMinLoli = false;
        }

        preferences = getSharedPreferences(SignageVariables.PREFS_SERVER, 0);

        toolbar = (Toolbar)findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.nav_order_purches_history);
        actionBar.setHomeAsUpIndicator(new IconDrawable(this, TypiconsIcons.typcn_chevron_left).colorRes(R.color.white).actionBarSize());

        selfHistoryOrderAdapter = new SelfHistoryOrderAdapter(this);

        recyclerView = (RecyclerView) findViewById(R.id.order_list);
        final RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(selfHistoryOrderAdapter);

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

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                OrderHistorySync orderSync = new OrderHistorySync(SelfHistoryOrderListActivity.this, SelfHistoryOrderListActivity.this, false);
                orderSync.execute("0");
            }
        });

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
                selfHistoryOrderAdapter = new SelfHistoryOrderAdapter(SelfHistoryOrderListActivity.this);
                recyclerView.setAdapter(selfHistoryOrderAdapter);
                OrderHistorySyncSearch o = new OrderHistorySyncSearch(SelfHistoryOrderListActivity.this, SelfHistoryOrderListActivity.this, false);
                o.execute(query, "0");

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
        switch (item.getItemId()){
            case android.R.id.home :
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
        selfHistoryOrderAdapter.addItems(orderList);
        dataFailed.setVisibility(View.GONE);
        if (orderList.size() > 0){
            dataNull.setVisibility(View.GONE);
        }else {
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

    class OrderHistorySync extends AsyncTask<String, Void, JSONObject> {

        private Context context;
        private TaskService taskService;
        private boolean isLoadMore = false;

        public OrderHistorySync(Context context, TaskService taskService, boolean isLoadMore) {
            this.context = context;
            this.taskService = taskService;
            this.isLoadMore = isLoadMore;
        }

        @Override
        protected JSONObject doInBackground(String... JsonObject) {
            Log.d(getClass().getSimpleName(), "?acces_token= " + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
            return ConnectionUtil.get(preferences.getString("server_url", "") + orderHistoryUrl+"?access_token="
                    + AuthenticationUtils.getCurrentAuthentication().getAccessToken() + "&page="+JsonObject[0]);
        }

        @Override
        protected void onCancelled() {
            taskService.onCancel(SignageVariables.HISTORY_ORDER_GET_TASK, "Batal");
        }

        @Override
        protected void onPreExecute() {
            if (!isLoadMore){
                swipeRefreshLayout.setRefreshing(true);
            }
            taskService.onExecute(SignageVariables.HISTORY_ORDER_GET_TASK);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {
                    List<Order> orders = new ArrayList<Order>();

                    Log.d("result order =====", result.toString());
                    totalPage = result.getInt("totalPages");
                    JSONArray jsonArray = result.getJSONArray("content");
                    for (int a = 0; a < jsonArray.length(); a++) {
                        JSONObject object = jsonArray.getJSONObject(a);

                        Order order = new Order();
                        order.setId(object.getString("id"));
                        order.setReceiptNumber(object.getString("receiptNumber"));

                        JSONObject logInformationObject = new JSONObject();
                        if (!object.isNull("logInformation")) {
                            logInformationObject = object.getJSONObject("logInformation");
                            LogInformation logInformation = new LogInformation();
                            logInformation.setCreateDate(new Date(logInformationObject.getLong("createDate")));
                            order.setLogInformation(logInformation);
                        }

                        JSONObject siteObject = new JSONObject();
                        if (!object.isNull("site")){
                            siteObject = object.getJSONObject("site");
                            Site site = new Site();
                            site.setId(siteObject.getString("id"));
                            site.setName(siteObject.getString("name"));
                            site.setEmail(siteObject.getString("email"));
                            site.setDescription(siteObject.getString("description"));
                            order.setSite(site);
                        }

                        if (object.getString("status").equalsIgnoreCase(Order.OrderStatus.PROCESSED.name())){
                            order.setStatus(Order.OrderStatus.PROCESSED);
                        }else if (object.getString("status").equalsIgnoreCase(Order.OrderStatus.SENDING.name())){
                            order.setStatus(Order.OrderStatus.SENDING);
                        }else if (object.getString("status").equalsIgnoreCase(Order.OrderStatus.RECEIVED.name())){
                            order.setStatus(Order.OrderStatus.RECEIVED);
                        }

                        orders.add(order);
                    }
                    orderList = orders;
                    if (isLoadMore) {
                        page++;
                        loadProgress.dismiss();
                    }
                    taskService.onSuccess(SignageVariables.HISTORY_ORDER_GET_TASK, true);
                } else {
                    taskService.onError(SignageVariables.HISTORY_ORDER_GET_TASK, "Error");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                taskService.onError(SignageVariables.HISTORY_ORDER_GET_TASK, "Error");
            }

        }
    }

    class OrderHistorySyncSearch extends AsyncTask<String, Void, JSONObject> {

        private Context context;
        private TaskService taskService;
        private boolean isLoadMore = false;

        public OrderHistorySyncSearch(Context context, TaskService taskService, boolean isLoadMore) {
            this.context = context;
            this.taskService = taskService;
            this.isLoadMore = isLoadMore;
        }

        @Override
        protected JSONObject doInBackground(String... JsonObject) {
            Log.d(getClass().getSimpleName(), "?acces_token= " + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
            return ConnectionUtil.get(preferences.getString("server_url", "") + orderHistoryUrl+"?access_token="
                    + AuthenticationUtils.getCurrentAuthentication().getAccessToken()+"&q="+JsonObject[0]+ "&page="+JsonObject[1]);
        }

        @Override
        protected void onCancelled() {
            taskService.onCancel(SignageVariables.HISTORY_ORDER_GET_TASK, "Batal");
        }

        @Override
        protected void onPreExecute() {
            if (!isLoadMore){
                swipeRefreshLayout.setRefreshing(true);
            }
            taskService.onExecute(SignageVariables.HISTORY_ORDER_GET_TASK);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {
                    List<Order> orders = new ArrayList<Order>();

                    Log.d("result order =====", result.toString());
                    totalPage = result.getInt("totalPages");
                    JSONArray jsonArray = result.getJSONArray("content");
                    for (int a = 0; a < jsonArray.length(); a++) {
                        JSONObject object = jsonArray.getJSONObject(a);

                        Order order = new Order();
                        order.setId(object.getString("id"));
                        order.setReceiptNumber(object.getString("receiptNumber"));

                        JSONObject logInformationObject = new JSONObject();
                        if (!object.isNull("logInformation")) {
                            logInformationObject = object.getJSONObject("logInformation");
                            LogInformation logInformation = new LogInformation();
                            logInformation.setCreateDate(new Date(logInformationObject.getLong("createDate")));
                            order.setLogInformation(logInformation);
                        }

                        JSONObject siteObject = new JSONObject();
                        if (!object.isNull("site")){
                            siteObject = object.getJSONObject("site");
                            Site site = new Site();
                            site.setId(siteObject.getString("id"));
                            site.setName(siteObject.getString("name"));
                            site.setEmail(siteObject.getString("Email"));
                            order.setSite(site);
                        }

                        if (object.getString("status").equalsIgnoreCase(Order.OrderStatus.PROCESSED.name())){
                            order.setStatus(Order.OrderStatus.PROCESSED);
                        }else if (object.getString("status").equalsIgnoreCase(Order.OrderStatus.SENDING.name())){
                            order.setStatus(Order.OrderStatus.SENDING);
                        }else if (object.getString("status").equalsIgnoreCase(Order.OrderStatus.RECEIVED.name())){
                            order.setStatus(Order.OrderStatus.RECEIVED);
                        }

                        orders.add(order);
                    }
                    orderList = orders;
                    if (isLoadMore) {
                        page++;
                        loadProgress.dismiss();
                    }
                    taskService.onSuccess(SignageVariables.HISTORY_ORDER_GET_TASK, true);
                } else {
                    taskService.onError(SignageVariables.HISTORY_ORDER_GET_TASK, "Error");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                taskService.onError(SignageVariables.HISTORY_ORDER_GET_TASK, "Error");
            }

        }
    }

    private void refreshContent(){
        //clearing data
        selfHistoryOrderAdapter = new SelfHistoryOrderAdapter(this);
        recyclerView.setAdapter(selfHistoryOrderAdapter);

        //adding new data
        for (int x = 0; x < page; x++){
            Log.d(getClass().getSimpleName(), "running on page "+ x);
            final int finalX = x;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    OrderHistorySync orderSync = new OrderHistorySync(SelfHistoryOrderListActivity.this, SelfHistoryOrderListActivity.this, false);
                    orderSync.execute(Integer.toString(finalX));
                }
            },500);
        }
    }

    public void loadMoreContent(){
        if (page < totalPage){

            //show progress
            loadProgress.show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    OrderHistorySync orderSync = new OrderHistorySync(SelfHistoryOrderListActivity.this, SelfHistoryOrderListActivity.this, true);
                    orderSync.execute(Integer.toString(page));
                }
            }, 500);
        }
    }

    public void openOrderMenuActivity(Intent data, View orderNumber, View orderDate){

        Pair<View, String> pairOrderNumber = Pair.create(orderNumber, getString(R.string.transition_number));
        Pair<View, String> pairOrderDate = Pair.create(orderDate, getString(R.string.transition_date));

        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, pairOrderNumber, pairOrderDate);
        if (isMinLoli) {
            startActivityForResult(data, requestOrderMenuActivityCode, optionsCompat.toBundle());
        }else {
            startActivityForResult(data, requestOrderMenuActivityCode);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == requestOrderMenuActivityCode){
            if (resultCode == RESULT_OK){
                refreshContent();
            }
        }
    }
}
