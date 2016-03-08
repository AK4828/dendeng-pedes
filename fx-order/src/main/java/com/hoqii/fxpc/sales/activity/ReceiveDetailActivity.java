package com.hoqii.fxpc.sales.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
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
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.adapter.ReceiveOrderMenuAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SerialNumberDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.entity.Product;
import com.hoqii.fxpc.sales.entity.Receive;
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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * Created by miftakhul on 12/8/15.
 */
public class ReceiveDetailActivity extends AppCompatActivity implements TaskService {
    private int requestScannerCode = 123;

    private List<SerialNumber> serialNumberList = new ArrayList<SerialNumber>();
    private List<OrderMenu> tempOrderMenus = new ArrayList<OrderMenu>();
    private List<SerialNumber> tempSerialNumbers = new ArrayList<SerialNumber>();
    private SharedPreferences preferences;
    private RecyclerView recyclerView;
    private ReceiveOrderMenuAdapter receiveOrderMenuAdapter;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView receiveDate, orderNumber, orderDate, site;
    private String shipmentId, orderId;
    private ProgressDialog loadProgress;
    private int page = 1, totalPage;
    private String receiveUrl = "/api/order/receives/";
    private String historyCheckUrl = "/api/orderHistory/";
    private String shipmentCheckUrl = "/api/order/shipments/";
    private Button verifyButton;
    private boolean isLoli = false;
    private Receive receive;
    private JobManager jobManager;
    private ProgressDialog progressDialog;
    private boolean statusDelivery = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_detail);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isLoli = true;
        } else {
            isLoli = false;
        }

        preferences = getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        jobManager = SignageApplication.getInstance().getJobManager();

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(new IconDrawable(this, TypiconsIcons.typcn_chevron_left).colorRes(R.color.white).actionBarSize());

        receiveDate = (TextView) findViewById(R.id.rl_tgl_receive);
        orderNumber = (TextView) findViewById(R.id.rl_number);
        orderDate = (TextView) findViewById(R.id.rl_tgl);
        site = (TextView) findViewById(R.id.rl_site);
        verifyButton = (Button) findViewById(R.id.button_verify);

        if (getIntent().getExtras() != null) {
            shipmentId = getIntent().getStringExtra("shipmentId");

            Log.d(getClass().getSimpleName(), "shipment id : " + shipmentId);
            orderNumber.setText(getIntent().getStringExtra("orderReceipt"));

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy / hh:mm:ss");
            Date date = new Date();
            date.setTime(getIntent().getLongExtra("receiveDate", 0));

            Date oDate = new Date();
            oDate.setTime(getIntent().getLongExtra("orderDate", 0));
            orderId = getIntent().getStringExtra("orderId");
            site.setText("Received From : " + getIntent().getStringExtra("siteDescription"));
            receiveDate.setText(simpleDateFormat.format(date));
            orderDate.setText(simpleDateFormat.format(oDate));

            ObjectMapper mapper = SignageApplication.getObjectMapper();

            String jsonReceive = getIntent().getStringExtra("jsonReceive");
            try {
                receive = mapper.readValue(jsonReceive, Receive.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (receive.getShipment().getStatus() == Shipment.ShipmentStatus.DELIVERED) {
                statusDelivery = true;
            }
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Confirm shipment");

        if (statusDelivery == true) {
            receiveOrderMenuAdapter = new ReceiveOrderMenuAdapter(this, orderId, true);
        } else {
            receiveOrderMenuAdapter = new ReceiveOrderMenuAdapter(this, orderId);
        }

        recyclerView = (RecyclerView) findViewById(R.id.receive_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(receiveOrderMenuAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiptRefress);
        swipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.yellow, R.color.blue, R.color.red);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });


        if (statusDelivery == true) {
            verifyButton.setText(getResources().getText(R.string.verified));
        }
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (verifyButton.getText().equals(getResources().getText(R.string.verify))) {
                    verify();
                } else if (verifyButton.getText().equals(getResources().getText(R.string.verify_now))) {
                    confirmDelivery();
                } else if (verifyButton.getText().equals(getResources().getText(R.string.verified))) {
                    AlertMessage("Sudah diverifikasi !");
                }
            }
        });

        loadProgress = new ProgressDialog(this);
        loadProgress.setMessage("Fetching data...");

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                SerialOrderMenuSync serialOrderMenuSync = new SerialOrderMenuSync(ReceiveDetailActivity.this, ReceiveDetailActivity.this, false);
                serialOrderMenuSync.execute(shipmentId);
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finishDetail();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onExecute(int code) {

    }

    @Override
    public void onSuccess(int code, Object result) {
        if (code == SignageVariables.SERIAL_ORDER_MENU_GET_TASK) {
            swipeRefreshLayout.setRefreshing(false);
            if (statusDelivery == true) {
                receiveOrderMenuAdapter = new ReceiveOrderMenuAdapter(this, orderId, true);
            } else {
                receiveOrderMenuAdapter = new ReceiveOrderMenuAdapter(this, orderId);
            }
            recyclerView.setAdapter(receiveOrderMenuAdapter);
            receiveOrderMenuAdapter.addItems(serialNumberList);
            updateVerifyButton();

        } else if (code == SignageVariables.HISTORY_ORDER_MENU_GET_TASK) {
            CheckSerialOrderMenus checkSerialOrderMenus = new CheckSerialOrderMenus(ReceiveDetailActivity.this, ReceiveDetailActivity.this);
            checkSerialOrderMenus.execute(orderId);
        } else if (code == SignageVariables.SERIAL_ORDER_MENU_CHECK_TASK) {

            Log.d(getClass().getSimpleName(), "==============================================");
            Map<OrderMenu, Integer> mapStatus = new HashMap<OrderMenu, Integer>();
            for (OrderMenu om : tempOrderMenus) {
                Log.d(getClass().getSimpleName(), "[ order menu id " + om.getId() + "order menu qty order " + om.getQtyOrder() + " ]");


                String orderMenuId = om.getId();
                int qty = om.getQtyOrder();
                int qtySerial = 0;

                Log.d(getClass().getSimpleName(), "[ jumlah order " + qty + " ]");
                Log.d(getClass().getSimpleName(), "[ default serial " + qtySerial + " ]");

                for (SerialNumber sn : tempSerialNumbers) {
                    Log.d(getClass().getSimpleName(), "[ mencari serial dengan id order menu " + orderMenuId + "]");
                    if (sn.getOrderMenu().getId().equalsIgnoreCase(orderMenuId)) {
                        qtySerial += 1;
                        Log.d(getClass().getSimpleName(), "[ serial di temukan / jumlah default serial = " + qtySerial + " ]");
                    }

                }

                Log.d(getClass().getSimpleName(), "[ total jumlah order " + qty + " ]");
                Log.d(getClass().getSimpleName(), "[ total default serial akhir " + qtySerial + " ]");

                if (qty == qtySerial) {
                    mapStatus.put(om, 1);
                    Log.d(getClass().getSimpleName(), "[ append status order menu id " + orderMenuId + " / status (1) ]");
                } else {
                    mapStatus.put(om, 0);
                    Log.d(getClass().getSimpleName(), "[ append status order menu id " + orderMenuId + " / status (0) ]");
                }
            }


            List<Integer> list = new ArrayList<Integer>(mapStatus.values());
            Log.d(getClass().getSimpleName(), "========================================");
            for (Integer i : list) {
                Log.d(getClass().getSimpleName(), "value has map " + i);
            }
            Log.d(getClass().getSimpleName(), "========================================");

            if (!mapStatus.containsValue(0)) {
                Log.d(getClass().getSimpleName(), "======================================== update starting");
                jobManager.addJobInBackground(new OrderStatusUpdateJob(orderId, preferences.getString("server_url", ""), Order.OrderStatus.RECEIVED.name()));
            } else {
                Log.d(getClass().getSimpleName(), "======================================== update not running");
                progressDialog.dismiss();
                AlertMessage("Proses selesai");
            }


        }
    }

    @Override
    public void onCancel(int code, String message) {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onError(int code, String message) {
        if (code == SignageVariables.SERIAL_ORDER_MENU_GET_TASK) {
            swipeRefreshLayout.setRefreshing(false);
        } else if (code == SignageVariables.HISTORY_ORDER_MENU_GET_TASK) {
            reCheckOrderMenu();
        } else if (code == SignageVariables.SERIAL_ORDER_MENU_CHECK_TASK) {
            reCheckSerialMenu();
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
            //disable load more
//            return ConnectionUtil.get(preferences.getString("server_url", "") + receiveUrl + JsonObject[0] + "/menus?access_token="
//                    + AuthenticationUtils.getCurrentAuthentication().getAccessToken() + "&page=" + JsonObject[1]);
            return ConnectionUtil.get(preferences.getString("server_url", "") + receiveUrl + JsonObject[0] + "/menus?access_token="
                    + AuthenticationUtils.getCurrentAuthentication().getAccessToken() + "&max=" + Integer.MAX_VALUE);
        }

        @Override
        protected void onCancelled() {
            taskService.onCancel(SignageVariables.SERIAL_ORDER_MENU_GET_TASK, "Batal");
        }

        @Override
        protected void onPreExecute() {
            if (isLoadMore == false) {
                swipeRefreshLayout.setRefreshing(true);
            }
            taskService.onExecute(SignageVariables.SERIAL_ORDER_MENU_GET_TASK);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {
                    List<SerialNumber> serialNumbers = new ArrayList<SerialNumber>();
                    JSONArray jsonArray = result.getJSONArray("content");

                    totalPage = result.getInt("totalPages");
                    Log.d(getClass().getSimpleName(), "serial menu : " + result.toString());
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
                        serialNumbers.add(serialNumber);
                    }
                    serialNumberList = serialNumbers;

                    if (isLoadMore == true) {
                        page++;
                        loadProgress.dismiss();
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

    class CheckSerialOrderMenus extends AsyncTask<String, Void, JSONObject> {

        private Context context;
        private TaskService taskService;

        public CheckSerialOrderMenus(Context context, TaskService taskService) {
            this.context = context;
            this.taskService = taskService;
        }


        @Override
        protected JSONObject doInBackground(String... JsonObject) {
            Log.d(getClass().getSimpleName(), "?acces_token= " + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
            Log.d(getClass().getSimpleName(), " param : " + JsonObject[0]);
            return ConnectionUtil.get(preferences.getString("server_url", "") + shipmentCheckUrl + JsonObject[0] + "/serial/check?access_token="
                    + AuthenticationUtils.getCurrentAuthentication().getAccessToken() + "&max=" + Integer.MAX_VALUE);
        }

        @Override
        protected void onCancelled() {
            taskService.onCancel(SignageVariables.SERIAL_ORDER_MENU_CHECK_TASK, "Batal");
        }

        @Override
        protected void onPreExecute() {
            taskService.onExecute(SignageVariables.SERIAL_ORDER_MENU_CHECK_TASK);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {
                    List<SerialNumber> serialNumbers = new ArrayList<SerialNumber>();
                    JSONArray jsonArray = result.getJSONArray("content");

                    Log.d(getClass().getSimpleName(), "serial menu : " + result.toString());
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
                        serialNumbers.add(serialNumber);
                    }
                    tempSerialNumbers = serialNumbers;

                    taskService.onSuccess(SignageVariables.SERIAL_ORDER_MENU_CHECK_TASK, true);
                } else {
                    taskService.onError(SignageVariables.SERIAL_ORDER_MENU_CHECK_TASK, "Error");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                taskService.onError(SignageVariables.SERIAL_ORDER_MENU_CHECK_TASK, "Error");
            }
        }
    }

    class CheckOrderMenu extends AsyncTask<String, Void, JSONObject> {

        private Context context;
        private TaskService taskService;

        public CheckOrderMenu(Context context, TaskService taskService) {
            this.context = context;
            this.taskService = taskService;
        }


        @Override
        protected JSONObject doInBackground(String... JsonObject) {
            Log.d(getClass().getSimpleName(), "?acces_token= " + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
            Log.d(getClass().getSimpleName(), " param : " + JsonObject[0]);
            return ConnectionUtil.get(preferences.getString("server_url", "") + historyCheckUrl + JsonObject[0] + "/menus?access_token="
                    + AuthenticationUtils.getCurrentAuthentication().getAccessToken() + "&max=" + Integer.MAX_VALUE);
        }

        @Override
        protected void onCancelled() {
            taskService.onCancel(SignageVariables.HISTORY_ORDER_MENU_GET_TASK, "Batal");
        }

        @Override
        protected void onPreExecute() {
            taskService.onExecute(SignageVariables.HISTORY_ORDER_MENU_GET_TASK);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {

                    Log.d("result order menu =====", result.toString());
                    List<OrderMenu> orderMenus = new ArrayList<OrderMenu>();

                    JSONArray jsonArray = result.getJSONArray("content");
                    for (int a = 0; a < jsonArray.length(); a++) {
                        JSONObject object = jsonArray.getJSONObject(a);

                        OrderMenu orderMenu = new OrderMenu();
                        orderMenu.setId(object.getString("id"));
                        orderMenu.setQty(object.getInt("qty"));
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
                        orderMenus.add(orderMenu);
                    }

                    tempOrderMenus = orderMenus;

                    taskService.onSuccess(SignageVariables.HISTORY_ORDER_MENU_GET_TASK, true);
                } else {
                    taskService.onError(SignageVariables.HISTORY_ORDER_MENU_GET_TASK, "Error");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                taskService.onError(SignageVariables.HISTORY_ORDER_MENU_GET_TASK, "Error");
            }
        }
    }

    private void refreshContent() {
        if (verifyButton.getText().equals(getResources().getString(R.string.verified))) {
            receiveOrderMenuAdapter = new ReceiveOrderMenuAdapter(ReceiveDetailActivity.this, orderId, true);
            recyclerView.setAdapter(receiveOrderMenuAdapter);
            receiveOrderMenuAdapter.addItems(serialNumberList);
            swipeRefreshLayout.setRefreshing(false);
            updateVerifyButton();
        } else if (verifyButton.getText().equals(getResources().getString(R.string.verify_now))) {
            receiveOrderMenuAdapter = new ReceiveOrderMenuAdapter(ReceiveDetailActivity.this, orderId, true);
            recyclerView.setAdapter(receiveOrderMenuAdapter);
            receiveOrderMenuAdapter.addItems(serialNumberList);
            swipeRefreshLayout.setRefreshing(false);
            updateVerifyButton();
        } else {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    SerialOrderMenuSync serialOrderMenuSync = new SerialOrderMenuSync(ReceiveDetailActivity.this, ReceiveDetailActivity.this, false);
                    serialOrderMenuSync.execute(shipmentId);
                }
            });

//            receiveOrderMenuAdapter = new ReceiveOrderMenuAdapter(this, orderId);
//            recyclerView.setAdapter(receiveOrderMenuAdapter);
//
//            for (int x = 0; x < page; x++) {
//                final int finalX = x;
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        SerialOrderMenuSync receiveSync = new SerialOrderMenuSync(ReceiveDetailActivity.this, ReceiveDetailActivity.this, false);
//                        receiveSync.execute(shipmentId, Integer.toString(finalX));
//                    }
//                }, 500);
//            }
        }
    }

//    public void loadMoreContent() {
//        if (page < totalPage) {
//            loadProgress.show();
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    SerialOrderMenuSync receiveSync = new SerialOrderMenuSync(ReceiveDetailActivity.this, ReceiveDetailActivity.this, true);
//                    receiveSync.execute(shipmentId, Integer.toString(page));
//                }
//            }, 500);
//        }
//    }

    private void verify() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verifikasi");
        builder.setItems(new String[]{"Verifikasi semua", "Scan serial number"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    receiveOrderMenuAdapter = new ReceiveOrderMenuAdapter(ReceiveDetailActivity.this, orderId, true);
                    recyclerView.setAdapter(receiveOrderMenuAdapter);
                    receiveOrderMenuAdapter.addItems(serialNumberList);
                    updateVerifyButton();
                } else if (which == 1) {
                    Intent i = new Intent(ReceiveDetailActivity.this, ScannerReceiveActivityCustom.class);

                    ObjectMapper mapper = SignageApplication.getObjectMapper();
                    try {
                        String jsonSerial = mapper.writeValueAsString(serialNumberList);
                        i.putExtra("orderId", orderId);
                        i.putExtra("shipmentId", shipmentId);
                        i.putExtra("jsonSerial", jsonSerial);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    startActivityForResult(i, requestScannerCode);
                }
            }
        });
        builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == requestScannerCode) {
            Log.d(getClass().getSimpleName(), "result ok");
            receiveOrderMenuAdapter = new ReceiveOrderMenuAdapter(this, orderId);
            recyclerView.setAdapter(receiveOrderMenuAdapter);
            receiveOrderMenuAdapter.addItems(serialNumberList);
            updateVerifyButton();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateVerifyButton() {
        Log.d(getClass().getSimpleName(), "[ status verivikasi "+String.valueOf(statusDelivery)+" ]");

        if (statusDelivery == true) {
            verifyButton.setText(getResources().getText(R.string.verified));
            Log.d(getClass().getSimpleName(), "[ tombol sudah terverifikasi aktif ]");
        } else {
            if (receiveOrderMenuAdapter.isVerify() == true) {
                verifyButton.setText(getResources().getText(R.string.verify_now));
                Log.d(getClass().getSimpleName(), "[ tombol verifikasi sekarang aktif ]");
            } else {
                verifyButton.setText(getResources().getText(R.string.verify));
                Log.d(getClass().getSimpleName(), "[ tombol veriv aktif ]");
            }
        }
    }

    private void confirmDelivery() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);


        final View view = View.inflate(this, R.layout.view_shipment_update, null);

        TextView shipmentNumber, shipmentDate;
        final ImageView shipmentStatus;
        final CheckBox shipmentDelivered;
        final LinearLayout layout;
        final boolean[] checked = {false};

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy / hh:mm:ss");
        Date date = new Date();
        date.setTime(receive.getShipment().getLogInformation().getCreateDate().getTime());

        shipmentNumber = (TextView) view.findViewById(R.id.sl_number);
        shipmentDate = (TextView) view.findViewById(R.id.sl_tgl);
        shipmentStatus = (ImageView) view.findViewById(R.id.img_shipment_status);
        shipmentDelivered = (CheckBox) view.findViewById(R.id.shipment_delivered);

        shipmentNumber.setText("Shipment Number : " + receive.getShipment().getReceiptNumber());
        shipmentDate.setText("Tanggal : " + simpleDateFormat.format(date));

        String status = receive.getShipment().getStatus().name();

        if (status.equalsIgnoreCase(Shipment.ShipmentStatus.WAIT.name())) {
            shipmentStatus.setImageResource(R.drawable.ic_cached_black_48dp);
        } else if (status.equalsIgnoreCase(Shipment.ShipmentStatus.DELIVERED.name())) {
            shipmentStatus.setImageResource(R.drawable.ic_done_all_black_48dp);
        }

        if (!shipmentDelivered.isChecked()) {
            shipmentStatus.setImageResource(R.drawable.ic_cached_black_48dp);
            checked[0] = false;
        } else {
            shipmentStatus.setImageResource(R.drawable.ic_done_all_black_48dp);
            checked[0] = true;
        }

        shipmentDelivered.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    shipmentStatus.setImageResource(R.drawable.ic_cached_black_48dp);
                    checked[0] = isChecked;

                    if (isLoli) {
                        int x = (shipmentDelivered.getLeft() + shipmentDelivered.getRight()) / 2;
                        int y = (shipmentDelivered.getTop() + shipmentDelivered.getBottom()) / 2;
                        backAnimateRevealColorFromCoordinate(view, x, y);
                    }
                } else {
                    shipmentStatus.setImageResource(R.drawable.ic_done_all_black_48dp);
                    checked[0] = isChecked;


                    if (isLoli) {
                        int x = (shipmentDelivered.getLeft() + shipmentDelivered.getRight()) / 2;
                        int y = (shipmentDelivered.getTop() + shipmentDelivered.getBottom()) / 2;
                        startAnimateRevealColorFromCoordinate(view, x, y);
                    }
                }
            }
        });

        builder.setView(view);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (checked[0]) {
                    String shipmentId = receive.getShipment().getId();
                    jobManager.addJobInBackground(new ShipmentUpdateJob(preferences.getString("server_url", ""), shipmentId, checked));
                } else {
                    AlertMessage("Data tidak di rubah");
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();

    }

    private void AlertMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Peringatan");
        builder.setMessage(message);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private void reCheckOrderMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi");
        builder.setMessage("Terjadi sesuatu,\nUlangi proses");
        builder.setCancelable(false);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CheckOrderMenu checkOrderMenu = new CheckOrderMenu(ReceiveDetailActivity.this, ReceiveDetailActivity.this);
                checkOrderMenu.execute(orderId);
            }
        });
        builder.show();
    }

    private void reCheckSerialMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi");
        builder.setMessage("Terjadi sesuatu,\nUlangi proses");
        builder.setCancelable(false);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CheckSerialOrderMenus s = new CheckSerialOrderMenus(ReceiveDetailActivity.this, ReceiveDetailActivity.this);
                s.execute(orderId);
            }
        });
        builder.show();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startAnimateRevealColorFromCoordinate(View view, int x, int y) {
        float finalRadius = (float) Math.hypot(view.getWidth(), view.getHeight());

        Animator anim = ViewAnimationUtils.createCircularReveal(view, x, y, 0, finalRadius);
        view.setBackgroundColor(getResources().getColor(R.color.green));
        anim.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void backAnimateRevealColorFromCoordinate(final View view, int x, int y) {

        int initialRadius = view.getWidth();

        Animator anim = ViewAnimationUtils.createCircularReveal(view, x, y, initialRadius, 0);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setBackgroundColor(getResources().getColor(R.color.light_grey));
            }
        });

        anim.start();
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
                    SerialNumberDatabaseAdapter serialNumberDatabaseAdapter = new SerialNumberDatabaseAdapter(this);
                    Log.d(getClass().getSimpleName(), "serial shipemnt to delete " + requestSuccess.getEntityId());
                    serialNumberDatabaseAdapter.updateStatusByShipmentId(requestSuccess.getEntityId());

                    CheckOrderMenu checkOrderMenu = new CheckOrderMenu(ReceiveDetailActivity.this, ReceiveDetailActivity.this);
                    checkOrderMenu.execute(orderId);

                    statusDelivery = true;
                    updateVerifyButton();

                    break;
                }
                case OrderStatusUpdateJob.PROCESS_ID: {
                    Log.d(getClass().getSimpleName(), "finis because updated");
                    progressDialog.dismiss();
                    AlertMessage("Proses selesai");

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

    @Override
    public void onBackPressed() {
        finishDetail();
    }

    private void finishDetail() {
        if (statusDelivery == true) {
            Intent i = new Intent();
            i.putExtra("receiveId", receive.getId());
            setResult(RESULT_OK, i);
            Log.d(getClass().getSimpleName(), "[ status result true ]");
        } else {
            setResult(RESULT_OK);
            Log.d(getClass().getSimpleName(), "[ status result false ]");
        }
        finish();
    }


}
