package com.hoqii.fxpc.sales.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.adapter.ReceiveOrderMenuAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.DefaultDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SerialNumberDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.entity.OrderMenuSerial;
import com.hoqii.fxpc.sales.entity.Product;
import com.hoqii.fxpc.sales.entity.Receive;
import com.hoqii.fxpc.sales.entity.Shipment;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.job.OrderStatusUpdateJob;
import com.hoqii.fxpc.sales.job.ReturnJob;
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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
    private int requestSerialFileCode = 223;

    private List<OrderMenuSerial> orderMenuSerialList = new ArrayList<OrderMenuSerial>();
    private List<OrderMenu> tempOrderMenus = new ArrayList<OrderMenu>();
    private List<OrderMenuSerial> tempOrderMenuSerials = new ArrayList<OrderMenuSerial>();
    private SharedPreferences preferences;
    private RecyclerView recyclerView;
    private ReceiveOrderMenuAdapter receiveOrderMenuAdapter;
    private SerialNumberDatabaseAdapter serialNumberDatabaseAdapter;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView receiveDate, orderNumber, orderDate, site;
    private String shipmentId, orderId;
    private ProgressDialog loadProgress, progress;
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

    private static final String txt = "txt";
    private static final String csv = "csv";

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
        serialNumberDatabaseAdapter = new SerialNumberDatabaseAdapter(this);

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
            site.setText(getString(R.string.text_receive_from) + getIntent().getStringExtra("siteDescription"));
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
        progressDialog.setMessage(getString(R.string.message_confirm_sipment));

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
                    AlertMessage(getString(R.string.message_verified));
                }
            }
        });

        loadProgress = new ProgressDialog(this);
        loadProgress.setMessage(getString(R.string.message_fetch_data));
        progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.please_wait));
        progress.setCancelable(false);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                SerialOrderMenuSync serialOrderMenuSync = new SerialOrderMenuSync(ReceiveDetailActivity.this, ReceiveDetailActivity.this, false);
                serialOrderMenuSync.execute(shipmentId);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.receiving_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finishDetail();
                break;
//            case R.id.menu_add_return:
//                String jsonReceive = getIntent().getStringExtra("jsonReceive");
//                orderId = getIntent().getStringExtra("orderId");
//                Intent intent = new Intent(ReceiveDetailActivity.this, ReturnDetailActivity.class);
//                intent.putExtra("orderId", orderId);
//                intent.putExtra("shipmentId", shipmentId);
//                intent.putExtra("jsonReceive", jsonReceive);
//                intent.putExtra("siteDescription", getIntent().getStringExtra("siteDescription"));
//                intent.putExtra("orderDate", getIntent().getLongExtra("orderDate", 0));
//                intent.putExtra("receiveDate", getIntent().getLongExtra("receiveDate", 0));
//                intent.putExtra("orderReceipt", getIntent().getStringExtra("orderReceipt"));
//                intent.putExtra("siteToId", getIntent().getStringExtra("siteToId"));
//
//
//                startActivity(intent);
//                break;
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
            receiveOrderMenuAdapter.addItems(orderMenuSerialList);
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

                for (OrderMenuSerial sn : tempOrderMenuSerials) {
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
                AlertMessage(getString(R.string.message_progress_complete));
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
                    List<OrderMenuSerial> orderMenuSerials = new ArrayList<OrderMenuSerial>();
                    JSONArray jsonArray = result.getJSONArray("content");

                    totalPage = result.getInt("totalPages");
                    for (int a = 0; a < jsonArray.length(); a++) {
                        JSONObject object = jsonArray.getJSONObject(a);
                        OrderMenuSerial orderMenuSerial = new OrderMenuSerial();
                        orderMenuSerial.setId(object.getString("id"));
                        orderMenuSerial.setSerialNumber(object.getString("serialNumber"));

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
                            orderMenuSerial.setOrderMenu(orderMenu);
                        }
                        orderMenuSerials.add(orderMenuSerial);
                    }
                    orderMenuSerialList = orderMenuSerials;

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
                    List<OrderMenuSerial> orderMenuSerials = new ArrayList<OrderMenuSerial>();
                    JSONArray jsonArray = result.getJSONArray("content");

                    Log.d(getClass().getSimpleName(), "serial menu : " + result.toString());
                    for (int a = 0; a < jsonArray.length(); a++) {
                        JSONObject object = jsonArray.getJSONObject(a);
                        OrderMenuSerial orderMenuSerial = new OrderMenuSerial();
                        orderMenuSerial.setId(object.getString("id"));
                        orderMenuSerial.setSerialNumber(object.getString("serialNumber"));

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
                            orderMenuSerial.setOrderMenu(orderMenu);
                        }
                        orderMenuSerials.add(orderMenuSerial);
                    }
                    tempOrderMenuSerials = orderMenuSerials;

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
            receiveOrderMenuAdapter.addItems(orderMenuSerialList);
            swipeRefreshLayout.setRefreshing(false);
            updateVerifyButton();
        } else if (verifyButton.getText().equals(getResources().getString(R.string.verify_now))) {
            receiveOrderMenuAdapter = new ReceiveOrderMenuAdapter(ReceiveDetailActivity.this, orderId, true);
            recyclerView.setAdapter(receiveOrderMenuAdapter);
            receiveOrderMenuAdapter.addItems(orderMenuSerialList);
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


    private void verify() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.verify));
        builder.setItems(new String[]{getString(R.string.text_scan_serial_number), getString(R.string.text_manual_serial), getString(R.string.text_serial_from_file), getString(R.string.text_verify_all)}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent i = new Intent(ReceiveDetailActivity.this, ScannerReceiveActivityCustom.class);

                    ObjectMapper mapper = SignageApplication.getObjectMapper();
                    try {
                        String jsonSerial = mapper.writeValueAsString(orderMenuSerialList);
                        i.putExtra("orderId", orderId);
                        i.putExtra("shipmentId", shipmentId);
                        i.putExtra("jsonSerial", jsonSerial);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    startActivityForResult(i, requestScannerCode);
                }else if (which == 1){
                    AlertDialog.Builder manualDialog = new AlertDialog.Builder(ReceiveDetailActivity.this);
                    manualDialog.setCancelable(false);
                    manualDialog.setTitle(getString(R.string.message_title_enter_serial));
                    View customView = LayoutInflater.from(ReceiveDetailActivity.this).inflate(R.layout.view_manual_verify, null, false);
                    final EditText editSerial = (EditText) customView.findViewById(R.id.edit_serial);
                    final TextView textCount = (TextView) customView.findViewById(R.id.text_count_serial);
                    final TextInputLayout titleEditSerial = (TextInputLayout) customView.findViewById(R.id.title_edit_serial);

                    List<OrderMenuSerial> sn = serialNumberDatabaseAdapter.getSerialNumberListByOrderId(orderId);
                    textCount.setText(getResources().getString(R.string.text_already_verivfy_count)+sn.size()+getResources().getString(R.string.text_verivy_of_total)+ orderMenuSerialList.size());

                    final List<OrderMenuSerial> tempSerial = new ArrayList<OrderMenuSerial>();
                    final List<String> serialstring = new ArrayList<String>();
                    for (OrderMenuSerial s : orderMenuSerialList){
                        serialstring.add(s.getSerialNumber());
                    }

                    editSerial.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            titleEditSerial.setError(null);
                            if (serialstring.contains(editSerial.getText().toString())){

                                List<String> serialNumber = new ArrayList<String>();
                                for (OrderMenuSerial sn : tempSerial){
                                    serialNumber.add(sn.getSerialNumber());
                                }

                                if (!serialNumber.contains(editSerial.getText().toString())) {
                                    OrderMenuSerial orderMenuSerialObj = new OrderMenuSerial();

                                    orderMenuSerialObj.setId(DefaultDatabaseAdapter.generateId());
                                    orderMenuSerialObj.getOrderMenu().getOrder().setId(orderId);
                                    orderMenuSerialObj.getOrderMenu().setId(null);
                                    orderMenuSerialObj.setSerialNumber(editSerial.getText().toString());
                                    orderMenuSerialObj.getShipment().setId(shipmentId);
                                    tempSerial.add(orderMenuSerialObj);

                                    editSerial.getText().clear();
                                    textCount.setText(getResources().getString(R.string.text_already_verivfy_count)+tempSerial.size()+getResources().getString(R.string.text_verivy_of_total)+ orderMenuSerialList.size());
                                }else {
                                    titleEditSerial.setError(getString(R.string.message_serial_exist));
                                }
                            }
                        }
                    });

                    manualDialog.setView(customView);
                    manualDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            serialNumberDatabaseAdapter.save(tempSerial);
                            receiveOrderMenuAdapter = new ReceiveOrderMenuAdapter(ReceiveDetailActivity.this, orderId);
                            recyclerView.setAdapter(receiveOrderMenuAdapter);
                            receiveOrderMenuAdapter.addItems(orderMenuSerialList);
                            updateVerifyButton();
                        }
                    });
                    manualDialog.setNegativeButton(getString(R.string.text_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    manualDialog.show();
                } else if (which == 2){
                    Intent manager = new Intent(Intent.ACTION_GET_CONTENT);
                    manager.setType("text/plain");
                    startActivityForResult(manager, requestSerialFileCode);
                }else if (which == 3) {
                    receiveOrderMenuAdapter = new ReceiveOrderMenuAdapter(ReceiveDetailActivity.this, orderId, true);
                    recyclerView.setAdapter(receiveOrderMenuAdapter);
                    receiveOrderMenuAdapter.addItems(orderMenuSerialList);
                    updateVerifyButton();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.text_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
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

        shipmentNumber.setText(getResources().getString(R.string.text_receipt_number) + receive.getShipment().getReceiptNumber());
        shipmentDate.setText(getResources().getString(R.string.text_date)+ simpleDateFormat.format(date));

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
                    AlertMessage(getString(R.string.message_data_unchanged));
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
        builder.setTitle(getResources().getString(R.string.message_title_warning));
        builder.setMessage(message);
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void reCheckOrderMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.message_title_confirmation));
        builder.setMessage(getString(R.string.message_recheck));
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
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
        builder.setTitle(getResources().getString(R.string.message_title_confirmation));
        builder.setMessage(getString(R.string.message_recheck));
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CheckSerialOrderMenus s = new CheckSerialOrderMenus(ReceiveDetailActivity.this, ReceiveDetailActivity.this);
                s.execute(orderId);
            }
        });
        builder.show();
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

    private List<String> readSerialFromText(String pathSerial){
        File serialFile = new File(pathSerial);
        FileInputStream serialStream = null;

        List<String> serialList = new ArrayList<String>();
        try {
            serialStream = new FileInputStream(serialFile);
            DataInputStream serialDataStream = new DataInputStream(serialStream);
            BufferedReader buff = new BufferedReader(new InputStreamReader(serialDataStream));
            String serialLine = null;
            while ((serialLine = buff.readLine()) != null){
                serialList.add(serialLine);
            }
            serialStream.close();



        }catch (Exception e){
            Log.d("serial file ", "failed reading serial file");
        }
        return serialList;
    }

    private List<String> readSerialFromCsv(String pathSerial){
        File serialFile = new File(pathSerial);
        FileInputStream serialStream = null;
        String splitCaracter = ",";

        List<String> serialList = new ArrayList<String>();
        try {
            serialStream = new FileInputStream(serialFile);
            DataInputStream serialDataStream = new DataInputStream(serialStream);
            BufferedReader buff = new BufferedReader(new InputStreamReader(serialDataStream));

            String serialLine = null;
            while ((serialLine = buff.readLine()) != null){
                String[] serialLineColl = serialLine.split(splitCaracter);
                if (serialLineColl.length > 0){
                    for (int x = 0; x < serialLineColl.length; x++){
                        serialList.add(serialLineColl[x]);
                    }
                }
            }
            serialStream.close();

        }catch (Exception e){
            Log.d("serial file ", "failed reading serial file");
        }
        return serialList;
    }


    private List<OrderMenuSerial> verifyFromUploadFile(List<String> serialList){
        List<OrderMenuSerial> verifiedSerial = new ArrayList<OrderMenuSerial>();
        if (serialList.size() > 0){
            List<String> tempSerialList = new ArrayList<String>();
            for (OrderMenuSerial s : orderMenuSerialList){
                tempSerialList.add(s.getSerialNumber());
            }

            for (String sn : tempSerialList){
                if (serialList.contains(sn)){
                    OrderMenuSerial orderMenuSerialObj = new OrderMenuSerial();

                    orderMenuSerialObj.setId(DefaultDatabaseAdapter.generateId());
                    orderMenuSerialObj.getOrderMenu().getOrder().setId(orderId);
                    orderMenuSerialObj.getOrderMenu().setId(null);
                    orderMenuSerialObj.setSerialNumber(sn);
                    orderMenuSerialObj.getShipment().setId(shipmentId);
                    verifiedSerial.add(orderMenuSerialObj);
                }
            }

        }
        return verifiedSerial;
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
                    AlertMessage(getResources().getString(R.string.message_progress_complete));

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
                AlertMessage(getString(R.string.message_failed_confirm_shipment));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == requestScannerCode) {
            Log.d(getClass().getSimpleName(), "result ok");
            receiveOrderMenuAdapter = new ReceiveOrderMenuAdapter(this, orderId);
            recyclerView.setAdapter(receiveOrderMenuAdapter);
            receiveOrderMenuAdapter.addItems(orderMenuSerialList);
            updateVerifyButton();
        }else if (requestCode == requestSerialFileCode){
            if (resultCode == RESULT_OK){
                if (data.getData().getPath() != null){
                    Uri uriFile = data.getData();
                    Log.d("path from uri ", uriFile.getPath());

                    String path = uriFile.getPath().toString().replaceAll("\\s","");
                    Log.d("path ", path);
                    String type = MimeTypeMap.getFileExtensionFromUrl(path);
                    try {
                        if (type != null){
                            Log.d("type ",""+type);
                            progress.show();
                            List<String> serialList = new ArrayList<>();
                            switch (type){
                                case txt:
                                    serialList = readSerialFromText(uriFile.getPath());
                                    if (serialList.size() > 0) {
                                        doVerify(serialList);
                                    }else {
                                        progress.dismiss();
                                        AlertMessage("Serial file empty");
                                    }
                                    break;
                                case csv:
                                    serialList = readSerialFromCsv(uriFile.getPath());
                                    if (serialList.size() > 0) {
                                        doVerify(serialList);
                                    }else {
                                        progress.dismiss();
                                        AlertMessage("Serial file empty");
                                    }
                                    break;
                                default:
                                    progress.dismiss();
                                    AlertMessage("File not supported");
                                    break;
                            }
                        }
                    }catch (Exception e){
                        Log.e(getClass().getSimpleName(), e.getMessage());
                    }

                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void doVerify(List<String> serialList){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        final List<OrderMenuSerial> verified = verifyFromUploadFile(serialList);
        progress.dismiss();
        builder.setTitle(getString(R.string.verify));
        builder.setMessage(getResources().getString(R.string.text_already_verivfy_count)
                +Integer.toString(verified.size())
                +getResources().getString(R.string.text_verivy_of_total)
                + orderMenuSerialList.size());
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (verified.size() > 0){
                    serialNumberDatabaseAdapter.save(verified);
                    receiveOrderMenuAdapter = new ReceiveOrderMenuAdapter(ReceiveDetailActivity.this, orderId);
                    recyclerView.setAdapter(receiveOrderMenuAdapter);
                    receiveOrderMenuAdapter.addItems(orderMenuSerialList);
                    updateVerifyButton();
                }
                dialog.dismiss();
            }
        });
        builder.setNeutralButton(getString(R.string.repeat), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent manager = new Intent(Intent.ACTION_GET_CONTENT);
                manager.setType("text/plain");
                startActivityForResult(manager, requestSerialFileCode);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }


}
