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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.adapter.ShipmentAdapter;
import com.hoqii.fxpc.sales.core.LogInformation;
import com.hoqii.fxpc.sales.core.commons.Site;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.entity.Product;
import com.hoqii.fxpc.sales.entity.SerialNumber;
import com.hoqii.fxpc.sales.entity.Shipment;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.job.OrderStatusUpdateJob;
import com.hoqii.fxpc.sales.job.ShipmentUpdateJob;
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
public class ShipmentListActivity extends AppCompatActivity implements TaskService {

    private List<Shipment> shipmentList = new ArrayList<Shipment>();
    private SharedPreferences preferences;
    private RecyclerView recyclerView;
    private ShipmentAdapter shipmentAdapter;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressDialog progressDialog;
    private LinearLayout dataNull, dataFailed;
    private ProgressDialog loadProgress;
    private int page = 1, totalPage, orderMenuPage, orderMenuTotalpage, serialPage = 1,serialTotalPage;
    private JobManager jobManager;
    private List<OrderMenu> tempOrderMenus = new ArrayList<OrderMenu>();
    private List<SerialNumber> tempSerialnumberList = new ArrayList<SerialNumber>();

    private String shipmentUrl = "/api/order/shipments";
    private String orderMenuUrl = "/api/orderHistory/";
    private String serialUrl = "/api/serial/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_shipment_list);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new Explode());
            getWindow().setExitTransition(new Explode());
        }

        preferences = getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        jobManager = SignageApplication.getInstance().getJobManager();

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.nav_shipment_list);
        actionBar.setHomeAsUpIndicator(new IconDrawable(this, TypiconsIcons.typcn_chevron_left).colorRes(R.color.white).actionBarSize());

        shipmentAdapter = new ShipmentAdapter(this);

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

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                ShipmentSync sync = new ShipmentSync(ShipmentListActivity.this, ShipmentListActivity.this, false);
                sync.execute("0");
            }
        });

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
        if (code == SignageVariables.SELLER_SHIPMENT_GET_TASK) {
            swipeRefreshLayout.setRefreshing(false);
            Log.d(getClass().getSimpleName(), "shipment list size " + shipmentList.size());
            shipmentAdapter.addItems(shipmentList);
            dataFailed.setVisibility(View.GONE);
            if (shipmentList.size() > 0) {
                dataNull.setVisibility(View.GONE);
            } else {
                dataNull.setVisibility(View.VISIBLE);
            }
        } else if (code == SignageVariables.SELLER_ORDER_MENU_CHECK_TASK) {

            Intent data = (Intent) result;
            int totalPage = data.getIntExtra("totalPage", 0);
            String orderId = data.getStringExtra("orderId");
            Log.d(getClass().getSimpleName(), "getted total page from intent " + totalPage + " and order id " + orderId);
            for (int x = 0; x < totalPage; x++) {
                CheckOrderMenu checkOrderMenu = new CheckOrderMenu(ShipmentListActivity.this, ShipmentListActivity.this, orderId, x);
                checkOrderMenu.execute();

                Log.d(getClass().getSimpleName(), "running cheking order menu" + x + "kali");
            }

        } else if (code == SignageVariables.SELLER_ORDER_MENU_GET_TASK) {
            orderMenuPage += 1;

            Log.d(getClass().getSimpleName(), "page order menu sekarang " + orderMenuPage);
            Log.d(getClass().getSimpleName(), "jumlah page order menu " + orderMenuTotalpage);
            if (orderMenuPage == orderMenuTotalpage) {
                Log.d(getClass().getSimpleName(), "executing program true");

                Order order = (Order) result;
                Log.d(getClass().getSimpleName(), "cheking order menu success");
                Log.d(getClass().getSimpleName(), "executing update order, result order id " + order.getId());

                int curentOrderMenu = 0;
                Log.d(getClass().getSimpleName(), "jumlah order menu " + tempOrderMenus.size());
                for (int x=0; x < tempOrderMenus.size(); x++) {
                    Log.d(getClass().getSimpleName(), "qty order menu " + tempOrderMenus.get(x).getQty());
                    Log.d(getClass().getSimpleName(), "qty order menu curent" + curentOrderMenu);
                    curentOrderMenu += tempOrderMenus.get(x).getQty();
                }
                Log.d(getClass().getSimpleName(), "curent order menu count" + curentOrderMenu);

                if (curentOrderMenu == 0) {
                    Log.d(getClass().getSimpleName(), "running update order true");

                    //update order status
                    jobManager.addJobInBackground(new OrderStatusUpdateJob(order.getId(), preferences.getString("server_url", ""), Order.OrderStatus.RECEIVED.name()));
                } else {

                    Log.d(getClass().getSimpleName(), "running update order false");
                    Log.d(getClass().getSimpleName(), "finish because running update order false");
                    progressDialog.dismiss();
                    AlertMessage("Proses selesai");
//                    tempOrderMenus.clear();
                    shipmentAdapter = new ShipmentAdapter(this);
                    recyclerView.setAdapter(shipmentAdapter);
                    page = 1;
                    totalPage = 0;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ShipmentSync sync = new ShipmentSync(ShipmentListActivity.this, ShipmentListActivity.this, false);
                            sync.execute("0");
                        }
                    }, 500);
                }
            } else {
                Log.d(getClass().getSimpleName(), "executing program false");
            }

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

    public void onEventMainThread(GenericEvent.RequestInProgress requestInProgress) {
        Log.d(getClass().getSimpleName(), "RequestInProgress: " + requestInProgress.getProcessId());
        switch (requestInProgress.getProcessId()) {
            case ShipmentUpdateJob.PROCESS_ID:
                progressDialog.show();
                Log.d(getClass().getSimpleName(), "Shipment in progress");
                break;
            case OrderStatusUpdateJob.PROCESS_ID:
                Log.d(getClass().getSimpleName(), "update order in progress");
                break;
        }
    }

    public void onEventMainThread(GenericEvent.RequestSuccess requestSuccess) {
        Log.d(getClass().getSimpleName(), "success event : " + requestSuccess);
        try {
            switch (requestSuccess.getProcessId()) {
                case ShipmentUpdateJob.PROCESS_ID: {
                    Shipment s = (Shipment) requestSuccess.getResponse().getContent();

                    Log.d(getClass().getSimpleName(), "order id from shipment " + s.getOrder().getId());
                    CheckOrderMenuPage check = new CheckOrderMenuPage(ShipmentListActivity.this, ShipmentListActivity.this, s.getOrder().getId());
                    check.execute(s.getOrder().getId());

//                    String shipmentId = requestSuccess.getEntityId();
//                    Log.d(getClass().getSimpleName(), "Shipment id " + shipmentId);
//                    shipmentAdapter.removeItem(shipmentId);
//                    Log.d(getClass().getSimpleName(), "Shipment update success");
//
//                    for (int x = 0; x < shipmentList.size(); x++) {
//                        if (shipmentList.get(x).getId() == shipmentId) {
//                            shipmentList.remove(x);
//                        }
//                    }
//
//                    if (shipmentList.size() > 0) {
//                        dataNull.setVisibility(View.GONE);
//                    } else {
//                        dataNull.setVisibility(View.VISIBLE);
//                    }


                    break;
                }
                case OrderStatusUpdateJob.PROCESS_ID: {
                    Log.d(getClass().getSimpleName(), "finis because updated");
                    progressDialog.dismiss();
                    AlertMessage("Proses selesai");
//                    tempOrderMenus.clear();

                    shipmentAdapter = new ShipmentAdapter(this);
                    recyclerView.setAdapter(shipmentAdapter);

                    page = 1;
                    totalPage = 0;

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ShipmentSync sync = new ShipmentSync(ShipmentListActivity.this, ShipmentListActivity.this, false);
                            sync.execute("0");
                        }
                    }, 500);

                    break;
                }
            }

        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
        }

    }

    public void onEventMainThread(GenericEvent.RequestFailed failed) {
        Log.d(getClass().getSimpleName(), "Request failed: " + failed.getProcessId());
        switch (failed.getProcessId()) {
            case ShipmentUpdateJob.PROCESS_ID:
                progressDialog.dismiss();
                AlertMessage("Gagal mengkonfirmasi shipment");
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
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
                            if (!orderObject.isNull("site")) {
                                siteObject = orderObject.getJSONObject("site");
                                Site site = new Site();
                                site.setId(siteObject.getString("id"));
                                site.setName(siteObject.getString("name"));

                                order.setSite(site);
                            }

                            JSONObject siteFromObject = new JSONObject();
                            if (!orderObject.isNull("siteFrom")) {
                                siteFromObject = orderObject.getJSONObject("siteFrom");
                                Site siteFrom = new Site();
                                siteFrom.setId(siteFromObject.getString("id"));
                                siteFrom.setName(siteFromObject.getString("name"));

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

    class CheckOrderMenuPage extends AsyncTask<String, Void, JSONObject> {
        private Context context;
        private TaskService taskService;
        private String orderId;

        public CheckOrderMenuPage(Context context, TaskService taskService, String orderId) {
            this.context = context;
            this.taskService = taskService;
            this.orderId = orderId;
        }


        @Override
        protected JSONObject doInBackground(String... JsonObject) {
            Log.d(getClass().getSimpleName(), "?acces_token= " + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
            Log.d(getClass().getSimpleName(), " param : " + orderId);
            return ConnectionUtil.get(preferences.getString("server_url", "") + orderMenuUrl + orderId + "/menus?access_token="
                    + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
        }

        @Override
        protected void onCancelled() {
            taskService.onCancel(SignageVariables.SELLER_ORDER_MENU_CHECK_TASK, "Batal");
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject != null) {
                try {
                    int totalPage = jsonObject.getInt("totalElements");
                    orderMenuTotalpage = totalPage;

                    Log.d(getClass().getSimpleName(), "total element " + totalPage);

                    Intent i = new Intent();
                    i.putExtra("orderId", orderId);
                    i.putExtra("totalPage", totalPage);


                    taskService.onSuccess(SignageVariables.SELLER_ORDER_MENU_CHECK_TASK, i);
                } catch (JSONException e) {
                    e.printStackTrace();
                    taskService.onError(SignageVariables.SELLER_ORDER_MENU_CHECK_TASK, "Error");
                }
            }
        }
    }

    class CheckOrderMenu extends AsyncTask<String, Void, JSONObject> {

        private Context context;
        private TaskService taskService;
        private String orderId;


        public CheckOrderMenu(Context context, TaskService taskService, String orderId, int page) {
            this.context = context;
            this.taskService = taskService;
            this.orderId = orderId;
        }


        @Override
        protected JSONObject doInBackground(String... JsonObject) {
            Log.d(getClass().getSimpleName(), "?acces_token= " + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
            Log.d(getClass().getSimpleName(), " param : " + page);
            return ConnectionUtil.get(preferences.getString("server_url", "") + orderMenuUrl + orderId + "/menus?access_token="
                    + AuthenticationUtils.getCurrentAuthentication().getAccessToken() + "&page=" + page);
        }

        @Override
        protected void onCancelled() {
            taskService.onCancel(SignageVariables.SELLER_ORDER_MENU_GET_TASK, "Batal");
        }

        @Override
        protected void onPreExecute() {
            taskService.onExecute(SignageVariables.SELLER_ORDER_MENU_GET_TASK);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            Log.d(getClass().getSimpleName(), "result order check"+result.toString());
            try {
                if (result != null) {

                    JSONArray jsonArray = result.getJSONArray("content");
                    for (int a = 0; a < jsonArray.length(); a++) {
                        JSONObject object = jsonArray.getJSONObject(a);

                        OrderMenu orderMenu = new OrderMenu();
                        orderMenu.setId(object.getString("id"));
                        orderMenu.setQtyOrder(object.getInt("qtyOrder"));
                        orderMenu.setDescription(object.getString("description"));


                        JSONObject productObject = new JSONObject();
                        if (!object.isNull("product")) {
                            productObject = object.getJSONObject("product");

                            Product product = new Product();
                            product.setId(productObject.getString("id"));
                            product.setName(productObject.getString("name"));

                            orderMenu.setProduct(product);

                        }

                        Log.d(getClass().getSimpleName(), "onpost execute lauced");
                        tempOrderMenus.add(orderMenu);

                    }

                    Order o = new Order();
                    o.setId(orderId);

                    taskService.onSuccess(SignageVariables.SELLER_ORDER_MENU_GET_TASK, o);
                } else {
                    taskService.onError(SignageVariables.SELLER_ORDER_MENU_GET_TASK, "Error");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                taskService.onError(SignageVariables.SELLER_ORDER_MENU_GET_TASK, "Error");
            }
        }
    }

    class SerialOrderMenuSync extends AsyncTask<String, Void, JSONObject> {

        private Context context;
        private TaskService taskService;
        private boolean isLoadMore = false;

        public SerialOrderMenuSync(Context context, TaskService taskService, boolean isLoadMore) {
            this.context = context;
            this.taskService = taskService;
            this.isLoadMore = isLoadMore;
        }


        @Override
        protected JSONObject doInBackground(String... JsonObject) {
            Log.d(getClass().getSimpleName(), "?acces_token= " + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
            Log.d(getClass().getSimpleName(), " param : " + JsonObject[0]);
            return ConnectionUtil.get(preferences.getString("server_url", "") + serialUrl + JsonObject[0] + "/menus?access_token="
                    + AuthenticationUtils.getCurrentAuthentication().getAccessToken() + "&page="+JsonObject[1]);
        }

        @Override
        protected void onCancelled() {
            taskService.onCancel(SignageVariables.SERIAL_ORDER_MENU_GET_TASK, "Batal");
        }

        @Override
        protected void onPreExecute() {
            if (!isLoadMore){
                swipeRefreshLayout.setRefreshing(true);
            }
            taskService.onExecute(SignageVariables.SERIAL_ORDER_MENU_GET_TASK);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {
                    JSONArray jsonArray = result.getJSONArray("content");

                    serialTotalPage = result.getInt("totalPages");
                    Log.d(getClass().getSimpleName(), "serial menu : "+result.toString());
                    for (int a = 0; a < jsonArray.length(); a++) {
                        JSONObject object = jsonArray.getJSONObject(a);
                        SerialNumber serialNumber = new SerialNumber();
                        serialNumber.setId(object.getString("id"));
                        serialNumber.setSerialNumber(object.getString("serialNumber"));

                        JSONObject orderMenuObject = new JSONObject();
                        if (!object.isNull("orderMenu")) {
                            orderMenuObject = object.getJSONObject("orderMenu");

                            OrderMenu orderMenu = new OrderMenu();
                            orderMenu.setId(orderMenuObject.getString("id"));
                            orderMenu.setQty(orderMenuObject.getInt("qty"));
                            orderMenu.setQtyOrder(orderMenuObject.getInt("qtyOrder"));
                            orderMenu.setDescription(orderMenuObject.getString("description"));

                            JSONObject productObject = new JSONObject();
                            if (!orderMenuObject.isNull("product")) {
                                productObject = orderMenuObject.getJSONObject("product");

                                Product product = new Product();
                                product.setId(productObject.getString("id"));
                                product.setName(productObject.getString("name"));
                                orderMenu.setProduct(product);
                            }
                            serialNumber.setOrderMenu(orderMenu);
                        }
                        tempSerialnumberList.add(serialNumber);
                        Log.d(getClass().getSimpleName(), "serial added, id "+serialNumber.getId()+" size curent "+tempSerialnumberList.size());
                    }

                    taskService.onSuccess(SignageVariables.SERIAL_ORDER_MENU_GET_TASK, true);
                } else {
                    taskService.onError(SignageVariables.SERIAL_ORDER_MENU_GET_TASK, "Error");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                taskService.onError(SignageVariables.SERIAL_ORDER_MENU_GET_TASK, "Error");
            }
        }
    }


    private void refreshContent() {
        shipmentAdapter = new ShipmentAdapter(this);
        recyclerView.setAdapter(shipmentAdapter);

        for (int x = 0; x < page; x++) {
            final int finalX = x;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    ShipmentSync sync = new ShipmentSync(ShipmentListActivity.this, ShipmentListActivity.this, false);
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
                    ShipmentSync sync = new ShipmentSync(ShipmentListActivity.this, ShipmentListActivity.this, true);
                    sync.execute(Integer.toString(page));
                }
            }, 500);
        }
    }

    private void AlertMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Shipment");
        builder.setMessage(message);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == requestOrderMenuActivityCode){
//            if (resultCode == RESULT_OK){
//
//                swipeRefreshLayout.setRefreshing(true);
//                ShipmentSync sync = new ShipmentSync(ShipmentListActivity.this, ShipmentListActivity.this);
//                sync.execute();
//            }
//        }
//    }
}
