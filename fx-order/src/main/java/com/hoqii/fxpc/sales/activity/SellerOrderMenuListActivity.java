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
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageAppication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.adapter.SellerOrderMenuAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SerialNumberDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.entity.Product;
import com.hoqii.fxpc.sales.entity.SerialNumber;
import com.hoqii.fxpc.sales.entity.Shipment;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.job.MenuUpdateJob;
import com.hoqii.fxpc.sales.job.OrderStatusUpdateJob;
import com.hoqii.fxpc.sales.job.OrderUpdateJob;
import com.hoqii.fxpc.sales.job.SerialJob;
import com.hoqii.fxpc.sales.job.ShipmentJob;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.TypiconsIcons;
import com.joanzapata.iconify.widget.IconTextView;
import com.path.android.jobqueue.JobManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.meruvian.midas.core.service.TaskService;
import org.meruvian.midas.core.util.ConnectionUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;


/**
 * Created by miftakhul on 12/8/15.
 */
public class SellerOrderMenuListActivity extends AppCompatActivity implements TaskService {
    private int requestScannerCode = 123;

    private List<OrderMenu> orderMenuList = new ArrayList<OrderMenu>();
    private SharedPreferences preferences;
    private RecyclerView recyclerView;
    private SellerOrderMenuAdapter sellerOrderMenuAdapter;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String orderId;
    private CoordinatorLayout coordinatorLayout;
    private TextView omDate, omReceipt, siteFromName, omBusinessPartner;
    private IconTextView mailSiteFrom;
    private String orderUrl = "/api/purchaseOrders/";
    private ProgressDialog loadProgress;
    private int page = 1, totalPage;

    private String orderMenuId = null;
    private JobManager jobManager;
//    private Order orderShip = new Order();

    private List<String> orderMenuListSerial = new ArrayList<String>();
    private List<SerialNumber> serialNumbers = new ArrayList<SerialNumber>();
    private SerialNumberDatabaseAdapter serialNumberDatabaseAdapter;

    private int position;
    private int count = 0;
    private int maxSerialist = 0;
    private Shipment shipment = new Shipment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_order_menu_list);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new Fade());
            getWindow().setExitTransition(new Fade());
        }

        preferences = getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        orderId = getIntent().getExtras().getString("orderId");
        serialNumberDatabaseAdapter = new SerialNumberDatabaseAdapter(this);

        if (getIntent().getExtras().get("orderMenuListType") != null) {
            Log.d(getClass().getSimpleName(), getIntent().getExtras().get("orderMenuListType").toString());

            if (getIntent().getExtras().get("orderMenuListType").toString().equalsIgnoreCase("purchaseOrderMenuList")) {
                orderUrl = "/api/purchaseOrders/";
            } else if (getIntent().getExtras().get("orderMenuListType").toString().equalsIgnoreCase("orderMenuList")) {
                orderUrl = "/api/orders/";
            }
        }

        if (getIntent().getExtras() != null) {
            position = getIntent().getIntExtra("position", 0);
        }

        jobManager = SignageAppication.getInstance().getJobManager();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Mengirim Shipment");

        omDate = (TextView) findViewById(R.id.om_date);
        omReceipt = (TextView) findViewById(R.id.om_receipt);
        mailSiteFrom = (IconTextView) findViewById(R.id.om_email);
        siteFromName = (TextView) findViewById(R.id.om_siteFromName);

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Shipment");
        actionBar.setHomeAsUpIndicator(new IconDrawable(this, TypiconsIcons.typcn_chevron_left).colorRes(R.color.white).actionBarSize());

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordianotorLayout);

        sellerOrderMenuAdapter = new SellerOrderMenuAdapter(this, orderId);

        recyclerView = (RecyclerView) findViewById(R.id.orderMenu_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(sellerOrderMenuAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiptRefress);
        swipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.yellow, R.color.blue, R.color.red);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy / hh:mm:ss");
        Date date = new Date();
        date.setTime(getIntent().getLongExtra("orderDate", 0));

        omDate.setText("Tanggal" + simpleDateFormat.format(date).toString());
        omReceipt.setText("Nomor order : " + getIntent().getExtras().getString("orderReceipt"));
        mailSiteFrom.setText("{typcn-mail} " + getIntent().getStringExtra("siteFromEmail"));
        siteFromName.setText("Kirim ke : " + getIntent().getStringExtra("siteFromName"));

        checkOrderMenuSerialList();

        loadProgress = new ProgressDialog(this);
        loadProgress.setMessage("Fetching data...");
        loadProgress.setCancelable(false);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                OrderMenuSync orderMenuSync = new OrderMenuSync(SellerOrderMenuListActivity.this, SellerOrderMenuListActivity.this, false);
                orderMenuSync.execute(orderId, "0");
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shipment_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
            case R.id.menu_submit_shipment:

                if (orderMenuListSerial.size() != 0) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);
                    alert.setTitle("Shipment");
                    alert.setMessage("Send shipment ?");
                    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            jobManager.addJobInBackground(new ShipmentJob(preferences.getString("server_url", ""), orderId));
                            progressDialog.show();
                        }
                    });
                    alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    alert.show();
                } else {
                    AlertMessage("Scan serial number dahulu");
                }

                break;
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
        Log.d(getClass().getSimpleName(), "order menu serial size d1: " + orderMenuListSerial.size());
        Log.d(getClass().getSimpleName(), "order menu serial size d2: " + orderMenuListSerial.size());
        for (String id : orderMenuListSerial) {
            Log.d(getClass().getSimpleName(), "order menu serial id: " + id);
        }
        Log.d(getClass().getSimpleName(), "order menu serial size d2: " + orderMenuListSerial.size());

//        sellerOrderMenuAdapter = new SellerOrderMenuAdapter(this, orderId, orderMenuList);

        Log.d(getClass().getSimpleName(), "order menu serial size d3: " + orderMenuListSerial.size());
        for (String id : orderMenuListSerial) {
            Log.d(getClass().getSimpleName(), "order menu serial id: " + id);
        }

//        recyclerView.setAdapter(sellerOrderMenuAdapter);
        sellerOrderMenuAdapter.addItems(orderMenuList);
    }

    @Override
    public void onCancel(int code, String message) {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onError(int code, String message) {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == requestScannerCode) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    int position = data.getIntExtra("position", 0);
                    orderMenuId = data.getStringExtra("orderMenuId");
                    boolean status = data.getBooleanExtra("status", false);
                    Log.d(getClass().getSimpleName(), "order menu id : " + orderMenuId);
                    Log.d(getClass().getSimpleName(), "status : " + String.valueOf(status));


                    if (status) {
                        if (!orderMenuListSerial.contains(orderMenuId)) {
                            Log.d(getClass().getSimpleName(), "order menu id contains : " + orderMenuId);
                            Log.d(getClass().getSimpleName(), "order menu serial size : " + orderMenuListSerial.size());
                            orderMenuListSerial.add(orderMenuId);
                            Log.d(getClass().getSimpleName(), "order menu serial size : " + orderMenuListSerial.size());
                            sellerOrderMenuAdapter.addOrderMenuSerial(orderMenuId);
                            sellerOrderMenuAdapter.notifyItemChanged(position);
                        }
                    } else {
                        if (orderMenuListSerial.contains(orderMenuId)) {
                            Log.d(getClass().getSimpleName(), "order menu id status delete: yes");
                            Log.d(getClass().getSimpleName(), "order menu serial size : " + orderMenuListSerial.size());
//                            orderMenuListSerial.remove(position);

                            Log.d(getClass().getSimpleName(), "order menu serial size : " + orderMenuListSerial.size());
                            Log.d(getClass().getSimpleName(), "position to remove : " + position);
                            sellerOrderMenuAdapter.removeOrderMenuSerial(orderMenuId);
                            sellerOrderMenuAdapter.notifyItemChanged(position);

                            for (int x = 0; x < orderMenuListSerial.size(); x++) {
                                Log.d(getClass().getSimpleName(), "Serching contains order menu id");
                                if (orderMenuListSerial.get(x).equalsIgnoreCase(orderMenuId)) {
                                    orderMenuListSerial.remove(x);
                                    Log.d(getClass().getSimpleName(), "item serial removed");
                                    break;
                                } else {
                                    Log.d(getClass().getSimpleName(), "item serial not found, so not removed");
                                }
                            }


                        }
                    }
                }
            }
        }
        Log.d(getClass().getSimpleName(), "order menu serial size ====== : " + orderMenuListSerial.size());

        super.onActivityResult(requestCode, resultCode, data);
    }


    class OrderMenuSync extends AsyncTask<String, Void, JSONObject> {

        private Context context;
        private TaskService taskService;
        private boolean isLoadMore;

        public OrderMenuSync(Context context, TaskService taskService, boolean isLoadMore) {
            this.context = context;
            this.taskService = taskService;
            this.isLoadMore = isLoadMore;
        }


        @Override
        protected JSONObject doInBackground(String... JsonObject) {
            Log.d(getClass().getSimpleName(), "?acces_token= " + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
            Log.d(getClass().getSimpleName(), " param : " + JsonObject[0]);
            return ConnectionUtil.get(preferences.getString("server_url", "") + orderUrl + JsonObject[0] + "/menus?access_token="
                    + AuthenticationUtils.getCurrentAuthentication().getAccessToken() + "&page=" + JsonObject[1]);
        }

        @Override
        protected void onCancelled() {
            taskService.onCancel(SignageVariables.SELLER_ORDER_MENU_GET_TASK, "Batal");
        }

        @Override
        protected void onPreExecute() {
            if (!isLoadMore) {
                swipeRefreshLayout.setRefreshing(true);
            }
            taskService.onExecute(SignageVariables.SELLER_ORDER_MENU_GET_TASK);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {

                    List<OrderMenu> orderMenus = new ArrayList<OrderMenu>();

                    totalPage = result.getInt("totalPages");
                    Log.d("result order =====", result.toString());
                    JSONArray jsonArray = result.getJSONArray("content");
                    for (int a = 0; a < jsonArray.length(); a++) {
                        JSONObject object = jsonArray.getJSONObject(a);

                        OrderMenu orderMenu = new OrderMenu();
                        orderMenu.setId(object.getString("id"));
                        orderMenu.setQty(object.getInt("qty"));
                        orderMenu.setDescription(object.getString("description"));


                        JSONObject productObject = new JSONObject();
                        if (!object.isNull("product")) {
                            productObject = object.getJSONObject("product");

                            Product product = new Product();
                            product.setId(productObject.getString("id"));
                            product.setName(productObject.getString("name"));

                            orderMenu.setProduct(product);

                        }

                        orderMenus.add(orderMenu);

                    }

                    orderMenuList = orderMenus;
                    Log.d(getClass().getSimpleName(), "order menu size : " + orderMenuList.size());


                    if (isLoadMore) {
                        page++;
                        loadProgress.dismiss();
                    }

                    if (orderMenuList.size() == 0) {
                        Log.d(getClass().getSimpleName(), "order menu size : " + orderMenuList.size() + "===============");
                        Log.d(getClass().getSimpleName(), "Updating order status ");
                        jobManager.addJobInBackground(new OrderStatusUpdateJob(orderId, preferences.getString("server_url", ""), Order.OrderStatus.SENDING.name()));
                    }

                    taskService.onSuccess(SignageVariables.SELLER_ORDER_MENU_GET_TASK, true);
                } else {
                    taskService.onError(SignageVariables.SELLER_ORDER_MENU_GET_TASK, "Error");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                taskService.onError(SignageVariables.SELLER_ORDER_MENU_GET_TASK, "Error");
            }


        }
    }


    private void refreshContent() {
        //clean data
        sellerOrderMenuAdapter = new SellerOrderMenuAdapter(this, orderId);
        recyclerView.setAdapter(sellerOrderMenuAdapter);

        //add data
        for (int x = 0; x < page; x++) {
            final int finalX = x;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    OrderMenuSync orderMenuSync = new OrderMenuSync(SellerOrderMenuListActivity.this, SellerOrderMenuListActivity.this, false);
                    orderMenuSync.execute(orderId, Integer.toString(finalX));
                }
            }, 500);
        }
    }

    public void loadMoreContent() {
        if (page != totalPage) {

            loadProgress.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    OrderMenuSync orderMenuSync = new OrderMenuSync(SellerOrderMenuListActivity.this, SellerOrderMenuListActivity.this, true);
                    orderMenuSync.execute(orderId, Integer.toString(page));
                }
            }, 500);
        }
    }

    public void onEventMainThread(GenericEvent.RequestInProgress requestInProgress) {
        Log.d(getClass().getSimpleName(), "RequestInProgress: " + requestInProgress.getProcessId());
        switch (requestInProgress.getProcessId()) {
            case ShipmentJob.PROCESS_ID:
                progressDialog.setTitle("shipping");
                Log.d(getClass().getSimpleName(), "shipping");
                break;

            case MenuUpdateJob.PROCESS_ID:
                progressDialog.setTitle("Updating in progress");
                Log.d(getClass().getSimpleName(), "Updating in progress");
                break;

            case SerialJob.PROCESS_ID:
                progressDialog.setTitle("Serial in progress");
                Log.d(getClass().getSimpleName(), "Serial in progress");
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
                case ShipmentJob.PROCESS_ID: {

                    Log.d(getClass().getSimpleName(), "shipment sucess");
                    shipment = (Shipment) requestSuccess.getResponse().getContent();

                    List<SerialNumber> serialList = serialNumberDatabaseAdapter.getSerialNumberListByOrderId(orderId);
                    maxSerialist = 0;
                    count = 0;
                    maxSerialist = serialList.size();
                    Log.d(getClass().getSimpleName(), " getted serial count" + serialList.size() + " ================================================ ");

                    Log.d(getClass().getSimpleName(), " getted order menu serial count" + orderMenuListSerial.size() + " ================================================ ");
                    for (String om : orderMenuListSerial) {
                        Log.d(getClass().getSimpleName(), " access job order menu serial id " + om + " ================================================");

                        jobManager.addJobInBackground(new MenuUpdateJob(preferences.getString("server_url", ""), orderId, om));
                    }
                    for (SerialNumber snn : serialList) {
                        Log.d(getClass().getSimpleName(), " getted serial " + snn.getId() + " ================================================ ");
                        Log.d(getClass().getSimpleName(), " getted serial shipment id " + requestSuccess.getEntityId() + " ================================================ ");
                        jobManager.addJobInBackground(new SerialJob(preferences.getString("server_url", ""), snn.getId(), requestSuccess.getEntityId()));

                    }
                    break;
                }

                case MenuUpdateJob.PROCESS_ID: {
                    Log.d(getClass().getSimpleName(), "update order menu succcess");
                    break;
                }

                case SerialJob.PROCESS_ID: {
                    Log.d(getClass().getSimpleName(), "Serial succcess");
                    count++;
                    Log.d(getClass().getSimpleName(), "max serialist : " + Integer.valueOf(maxSerialist));
                    Log.d(getClass().getSimpleName(), "count serialist number: " + Integer.valueOf(count));

                    if (count == maxSerialist) {

                        Log.d(getClass().getSimpleName(), "number " + count);
                        Log.d(getClass().getSimpleName(), "max " + maxSerialist);
                        progressDialog.dismiss();

                        AlertDialog.Builder builder = new AlertDialog.Builder(SellerOrderMenuListActivity.this);
                        builder.setTitle("Shipment");
                        builder.setMessage("Proses selesai\nNomor Pengiriman : " + shipment.getReceiptNumber());
                        builder.setCancelable(false);
                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                page = 1;
                                totalPage = 0;

                                sellerOrderMenuAdapter = new SellerOrderMenuAdapter(SellerOrderMenuListActivity.this, orderId);
                                recyclerView.setAdapter(sellerOrderMenuAdapter);

                                OrderMenuSync orderMenuSync = new OrderMenuSync(SellerOrderMenuListActivity.this, SellerOrderMenuListActivity.this, false);
                                orderMenuSync.execute(orderId, "0");

                                checkOrderMenuSerialList();
                            }
                        });
                        builder.show();
                    }
                    break;
                }

                case OrderStatusUpdateJob.PROCESS_ID: {
                    Log.d(getClass().getSimpleName(), "Order status updated");
                    Intent data = new Intent();
                    data.putExtra("position", position);
                    setResult(RESULT_OK, data);
                    finish();
                    break;
                }
            }

        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    public void onEventMainThread(GenericEvent.RequestFailed failed) {
        progressDialog.dismiss();

        switch (failed.getProcessId()) {
            case ShipmentJob.PROCESS_ID:
                AlertMessage("Gagal mengirim shipment");
                break;

            case MenuUpdateJob.PROCESS_ID:
                AlertMessage("Gagal mengupdate order menu");
                break;

            case SerialJob.PROCESS_ID:
                AlertMessage("Gagal mengirim serial");
                break;

            case OrderUpdateJob.PROCESS_ID:
                AlertMessage("Gagal mengupdate status order");
                break;
        }

        Log.e(getClass().getSimpleName(),
                failed.getResponse().getHttpResponse().getStatusLine().getStatusCode() + " :"
                        + failed.getResponse().getHttpResponse().getStatusLine().getReasonPhrase());
    }

    private void checkOrderMenuSerialList() {
        serialNumberDatabaseAdapter = new SerialNumberDatabaseAdapter(this);
        serialNumbers = serialNumberDatabaseAdapter.getSerialNumberListByOrderId(orderId);

        if (serialNumbers.size() > 0) {
            for (int x = 0; x < serialNumbers.size(); x++) {
                String id = serialNumbers.get(x).getOrderMenu().getId();
                if (!orderMenuListSerial.contains(id)) {
                    orderMenuListSerial.add(id);
                    Log.d(getClass().getSimpleName(), "check order menu list size " + orderMenuListSerial.size());
                }
            }
        } else {
            orderMenuListSerial = new ArrayList<String>();
        }
        Log.d(getClass().getSimpleName(), "check order menu list size " + orderMenuListSerial.size());
    }

    private void AlertMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SellerOrderMenuListActivity.this);
        builder.setTitle("Shipment");
        builder.setMessage(message);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    public void openScanner(Intent data) {
//        Intent scanner = new Intent(this, ScannerActivityCustom.class);
//        scanner.putExtra("productName", data.getStringExtra("productName"));
//        scanner.putExtra("orderId", orderId);
//        scanner.putExtra("orderMenuId", data.getStringExtra("orderMenuId"));
//        scanner.putExtra("position", data.getIntExtra("position", 0));

        Intent scanner = data;
        scanner.putExtra("orderId", orderId);

        Log.d(getClass().getSimpleName(), "product name : " + data.getStringExtra("productName"));
        Log.d(getClass().getSimpleName(), "order Menu id : " + data.getStringExtra("orderMenuId"));

        startActivityForResult(scanner, requestScannerCode);
    }


}
