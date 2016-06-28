package com.hoqii.fxpc.sales.activity;

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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputLayout;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.adapter.SellerOrderMenuAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.DefaultDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SerialNumberDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.entity.OrderMenuSerial;
import com.hoqii.fxpc.sales.entity.Product;
import com.hoqii.fxpc.sales.entity.SerialEvent;
import com.hoqii.fxpc.sales.entity.SerialTemplate;
import com.hoqii.fxpc.sales.entity.Shipment;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.job.MenuUpdateJob;
import com.hoqii.fxpc.sales.job.OrderStatusUpdateJob;
import com.hoqii.fxpc.sales.job.OrderUpdateJob;
import com.hoqii.fxpc.sales.job.SerialJob;
import com.hoqii.fxpc.sales.job.ShipmentJob;
import com.hoqii.fxpc.sales.task.NotificationOrderTask;
import com.hoqii.fxpc.sales.task.StockSync;
import com.hoqii.fxpc.sales.util.AuthenticationCeck;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.hoqii.fxpc.sales.util.DirUtils;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.TypiconsIcons;
import com.joanzapata.iconify.widget.IconTextView;
import com.opencsv.CSVWriter;
import com.path.android.jobqueue.JobManager;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.meruvian.midas.core.service.TaskService;
import org.meruvian.midas.core.util.ConnectionUtil;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import de.greenrobot.event.EventBus;


/**
 * Created by miftakhul on 12/8/15.
 */
public class SellerOrderMenuListActivity extends AppCompatActivity implements TaskService {
    private int requestScannerCode = 123;
    private int requestSerialFileCode = 223;

    private AuthenticationCeck authenticationCeck = new AuthenticationCeck();
    private List<OrderMenu> orderMenuList = new ArrayList<OrderMenu>();
    private List<Order> orders = new ArrayList<Order>();
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
    private ProgressDialog loadProgress, progress;
    private ProgressDialog loadProgressCheckSerial;
    private int page = 1, totalPage;

//    private String orderMenuId = null;
    private JobManager jobManager;

    private List<String> orderMenuListSerial = new ArrayList<String>();
    private List<OrderMenuSerial> orderMenuSerials = new ArrayList<OrderMenuSerial>();
    private SerialNumberDatabaseAdapter serialNumberDatabaseAdapter;

    private int position;
    private int count = 0;
    private int maxSerialist = 0;
    private boolean serialError = false, updateMenuError = false;
    private Shipment shipment = null;

    //manual add serial
    private List<OrderMenuSerial> tempSerial = new ArrayList<OrderMenuSerial>();
    private List<OrderMenuSerial> verifiedSerial = new ArrayList<OrderMenuSerial>();
    private List<OrderMenuSerial> unVerifiedSerial = new ArrayList<OrderMenuSerial>();
    private int countTocheck = 0;
    private int maxSerialistTocheck = 0;

    private static final String txt = "txt";
    private static final String csv = "csv";
    private static final String excel = "xls";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_order_menu_list);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new Fade());
            getWindow().setExitTransition(new Fade());
        }

        preferences = getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        orderId = getIntent().getExtras().getString("orderId");
        serialNumberDatabaseAdapter = new SerialNumberDatabaseAdapter(this);


        if (getIntent().getExtras() != null) {
            position = getIntent().getIntExtra("position", 0);
        }

        jobManager = SignageApplication.getInstance().getJobManager();

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.message_send_shipment));

        omDate = (TextView) findViewById(R.id.om_date);
        omReceipt = (TextView) findViewById(R.id.om_receipt);
        mailSiteFrom = (IconTextView) findViewById(R.id.om_email);
        siteFromName = (TextView) findViewById(R.id.om_siteFromName);

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.text_shipment);
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

        long orderDate = getIntent().getLongExtra("orderDate", 0);
        String orderReceipt = getIntent().getExtras().getString("orderReceipt");
        String orderSiteFrom = getIntent().getStringExtra("siteFromEmail");
        String orderSiteFromName = getIntent().getStringExtra("siteFromName");

        if (orderDate == 0 && orderReceipt == null && orderSiteFrom == null && orderSiteFromName == null) {
            jobManager.addJobInBackground(new NotificationOrderTask(orderId));
        } else {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy / hh:mm:ss");
            Date date = new Date();
            date.setTime(getIntent().getLongExtra("orderDate", 0));

            omDate.setText(getResources().getString(R.string.text_order_date) + simpleDateFormat.format(date).toString());
            omReceipt.setText(getResources().getString(R.string.text_order_receipt) + getIntent().getExtras().getString("orderReceipt"));
            mailSiteFrom.setText("{typcn-mail} " + getIntent().getStringExtra("siteFromEmail"));
            siteFromName.setText(getString(R.string.text_shipto) + getIntent().getStringExtra("siteFromName"));

            checkOrderMenuSerialList();

            loadProgress = new ProgressDialog(this);
            loadProgress.setMessage(getResources().getString(R.string.message_fetch_data));
            loadProgress.setCancelable(false);

            progress = new ProgressDialog(this);
            progress.setMessage(getString(R.string.please_wait));
            progress.setCancelable(false);

            loadProgressCheckSerial = new ProgressDialog(this);
            loadProgressCheckSerial.setMessage("Checking availability serial number");
            loadProgressCheckSerial.setCancelable(false);

            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    OrderMenuSync orderMenuSync = new OrderMenuSync(SellerOrderMenuListActivity.this, SellerOrderMenuListActivity.this, false);
                    orderMenuSync.execute(orderId, "0");
                }
            });
        }

//        progress = new ProgressDialog(this);
//        progress.setMessage(getString(R.string.please_wait));
//        progress.setCancelable(false);
//
//        loadProgressCheckSerial = new ProgressDialog(this);
//        loadProgressCheckSerial.setMessage("Checking availability serial number");
//        loadProgressCheckSerial.setCancelable(false);
//
//        new Handler().post(new Runnable() {
//            @Override
//            public void run() {
//                OrderMenuSync orderMenuSync = new OrderMenuSync(SellerOrderMenuListActivity.this, SellerOrderMenuListActivity.this, false);
//                orderMenuSync.execute(orderId, "0");
//            }
//        });

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
                    if (authenticationCeck.isNetworkAvailable()) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(this);
                        alert.setTitle(getString(R.string.dialog_shipment));
                        alert.setMessage(getString(R.string.message_ask_send_shipment));
                        alert.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                jobManager.addJobInBackground(new ShipmentJob(preferences.getString("server_url", ""), orderId));
                                   progressDialog.show();
                            }
                        });
                        alert.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        alert.show();
                    } else {
                        AlertMessage(getResources().getString(R.string.message_no_internet));
                    }

                } else {
                    AlertMessage(getString(R.string.message_scan_serial_number));
                }

                break;
            case R.id.menu_serial_options:
                inputAllSerial();
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
        switch (code){
            case SignageVariables.SELLER_ORDER_MENU_GET_TASK:
                swipeRefreshLayout.setRefreshing(false);
                Log.d(getClass().getSimpleName(), "order menu serial size d1: " + orderMenuListSerial.size());
                Log.d(getClass().getSimpleName(), "order menu serial size d2: " + orderMenuListSerial.size());
                for (String id : orderMenuListSerial) {
                    Log.d(getClass().getSimpleName(), "order menu serial id: " + id);
                }
                Log.d(getClass().getSimpleName(), "order menu serial size d2: " + orderMenuListSerial.size());

                Log.d(getClass().getSimpleName(), "order menu serial size d3: " + orderMenuListSerial.size());
                for (String id : orderMenuListSerial) {
                    Log.d(getClass().getSimpleName(), "order menu serial id: " + id);
                }

                sellerOrderMenuAdapter.addItems(orderMenuList);
                break;
            case SignageVariables.SERIAL_CHECK_GET_TASK:
                Log.d("check serial", "y");
                countTocheck++;
                SerialEvent serialEvent = (SerialEvent) result;
                for (OrderMenuSerial sn : tempSerial){
                    if (sn.getSerialNumber() == serialEvent.getSerial()){
                        boolean status = (boolean) serialEvent.isStatus();
                        if (status == true) {
                            verifiedSerial.add(sn);
                        } else {
                            unVerifiedSerial.add(sn);
                        }
                        if (countTocheck >= maxSerialistTocheck) {
                            loadProgressCheckSerial.dismiss();
                            if (verifiedSerial.size() > 0){
                                showVeriviedCheck();
                            }else {
                                verifiedSerial.clear();
                                unVerifiedSerial.clear();
                                tempSerial.clear();
                                maxSerialistTocheck = 0;
                                countTocheck = 0;
                                AlertMessage("Serial not found");
                            }
                        }
                    }
                }
                break;
        }

    }

    @Override
    public void onCancel(int code, String message) {
        if (code == SignageVariables.SELLER_ORDER_MENU_GET_TASK){
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onError(int code, String message) {
        switch (code){
            case SignageVariables.SELLER_ORDER_MENU_GET_TASK:
                swipeRefreshLayout.setRefreshing(false);
                break;
            case SignageVariables.SERIAL_CHECK_GET_TASK:
                countTocheck++;
                if (countTocheck >= maxSerialistTocheck) {
                    loadProgressCheckSerial.dismiss();
                    if (verifiedSerial.size() > 0){
                        showVeriviedCheck();
                    }else {
                        verifiedSerial.clear();
                        unVerifiedSerial.clear();
                        tempSerial.clear();
                        maxSerialistTocheck = 0;
                        countTocheck = 0;
                        AlertMessage("Serial not found");
                    }
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(NotificationOrderTask.NotificationOrderEvent event) {
        int status = event.getStatus();

        if (status == NotificationOrderTask.NotificationOrderEvent.NOTIF_SUCCESS) {
            orders = event.getOrders();
            for (Order order : orders) {
                omDate.setText(getResources().getString(R.string.text_order_date) + order.getLogInformation().getCreateDate());
                omReceipt.setText(getResources().getString(R.string.text_order_receipt) + order.getReceiptNumber());
                mailSiteFrom.setText("{typcn-mail} " + order.getSiteFrom().getEmail());
                siteFromName.setText(getString(R.string.text_shipto) + order.getSiteFrom().getName());
            }
        } else if (status == NotificationOrderTask.NotificationOrderEvent.NOTIF_FAILED) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == requestScannerCode) {
            checkOrderMenuSerialList();

            if (resultCode == RESULT_OK) {
                if (orderMenuListSerial.size() > 0) {
                    Log.d(getClass().getSimpleName(), "updated");
                    sellerOrderMenuAdapter.updateOrderMenuSerial(orderMenuListSerial);
                } else {
                    Log.d(getClass().getSimpleName(), "remove all");
                    sellerOrderMenuAdapter = new SellerOrderMenuAdapter(this, orderId);
                    recyclerView.setAdapter(sellerOrderMenuAdapter);
                    sellerOrderMenuAdapter.addItems(orderMenuList);
                }
            }
        } else if (requestCode == requestSerialFileCode){
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
                            loadProgressCheckSerial.show();
                            List<SerialTemplate> serialList = new ArrayList<>();
                            switch (type){
//                                case txt:
//                                    serialList = readSerialFromText(uriFile.getPath());
//                                    if (serialList.size() > 0) {
//                                        doVerify(serialList);
//                                    }else {
//                                        progress.dismiss();
//                                        AlertMessage("Serial file empty");
//                                    }
//                                    break;
                                case csv:
                                    serialList = readSerialFromCsv(uriFile.getPath());
                                    if (serialList.size() > 0) {
                                        Log.d("serial", "size "+Integer.toString(serialList.size()));
                                        for (SerialTemplate s : serialList){
                                            Log.d("sku "+s.getSku(), "serial "+s.getSerialNumber());
                                        }
                                        maxSerialistTocheck = serialList.size();
                                        for (SerialTemplate s : serialList){
                                            StockSync check = new StockSync(SellerOrderMenuListActivity.this, SellerOrderMenuListActivity.this, StockSync.StockUri.bySerialUri.name());
                                            check.execute(s.getSku(),s.getSerialNumber());
                                        }

                                    }else {
                                        loadProgressCheckSerial.dismiss();
                                        AlertMessage("Serial file empty");
                                    }
                                    break;
                                case excel:
                                    serialList = readSerialFromExcel(uriFile.getPath());
                                    if (serialList.size() > 0) {
                                        Log.d("serial", "size "+Integer.toString(serialList.size()));
                                        for (SerialTemplate s : serialList){
                                            Log.d("sku "+s.getSku(), "serial "+s.getSerialNumber());
                                        }
                                        maxSerialistTocheck = serialList.size();
                                        for (SerialTemplate s : serialList){
                                            StockSync check = new StockSync(SellerOrderMenuListActivity.this, SellerOrderMenuListActivity.this, StockSync.StockUri.bySerialUri.name());
                                            check.execute(s.getSku(),s.getSerialNumber());
                                        }
                                    }else {
                                        loadProgressCheckSerial.dismiss();
                                        AlertMessage("Serial file empty");
                                    }
                                    break;
                                default:
                                    loadProgressCheckSerial.dismiss();
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
        Log.d(getClass().getSimpleName(), "order menu serial size ====== : " + orderMenuListSerial.size());

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onEventMainThread(GenericEvent.RequestInProgress requestInProgress) {
        Log.d(getClass().getSimpleName(), "RequestInProgress: " + requestInProgress.getProcessId());
        switch (requestInProgress.getProcessId()) {
            case ShipmentJob.PROCESS_ID:
                progressDialog.setTitle(getString(R.string.message_shipping));
                Log.d(getClass().getSimpleName(), "shipping");
                break;

            case MenuUpdateJob.PROCESS_ID:
                progressDialog.setTitle(getString(R.string.message_update_progress));
                Log.d(getClass().getSimpleName(), "Updating in progress");
                break;

            case SerialJob.PROCESS_ID:
                progressDialog.setTitle(getString(R.string.message_serial_progress));
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

                    List<OrderMenuSerial> serialList = serialNumberDatabaseAdapter.getSerialNumberListByOrderId(orderId);
                    maxSerialist = 0;
                    count = 0;
                    maxSerialist = serialList.size();
                    Log.d(getClass().getSimpleName(), " getted serial count" + serialList.size() + " ================================================ ");

                    Log.d(getClass().getSimpleName(), " getted order menu serial count" + orderMenuListSerial.size() + " ================================================ ");

                    for (OrderMenuSerial snn : serialList) {
                        Log.d(getClass().getSimpleName(), " getted serial " + snn.getId() + " ================================================ ");
                        Log.d(getClass().getSimpleName(), " getted serial shipment id " + requestSuccess.getEntityId() + " ================================================ ");
                        jobManager.addJobInBackground(new SerialJob(preferences.getString("server_url", ""), snn.getId(), requestSuccess.getEntityId()));
                    }
                    break;
                }

                case SerialJob.PROCESS_ID: {
                    Log.d(getClass().getSimpleName(), "Serial succcess");
                    count++;
                    Log.d(getClass().getSimpleName(), "max serialist : " + Integer.valueOf(maxSerialist));
                    Log.d(getClass().getSimpleName(), "count serialist number: " + Integer.valueOf(count));

                    if (count == maxSerialist) {
                        if (serialError) {
                            progressDialog.dismiss();
                            resendSerial();
                        } else {
                            checkOrderMenuSerialListHasSync();
                            serialError = false;
                            maxSerialist = 0;
                            count = 0;
                            maxSerialist = orderMenuListSerial.size();

                            for (String om : orderMenuListSerial) {
                                Log.d(getClass().getSimpleName(), " access job order menu serial id " + om + " ================================================");
                                jobManager.addJobInBackground(new MenuUpdateJob(preferences.getString("server_url", ""), orderId, om));
                            }
                        }
                    }

                    break;
                }

                case MenuUpdateJob.PROCESS_ID: {
                    Log.d(getClass().getSimpleName(), "update order menu succcess");
                    count++;
                    progressDialog.dismiss();
                    if (count == maxSerialist) {
                        if (updateMenuError) {
                            resendOrderMenuUpdate();
                        } else {
                            updateMenuError = false;
                            AlertDialog.Builder builder = new AlertDialog.Builder(SellerOrderMenuListActivity.this);
                            builder.setTitle(getResources().getString(R.string.text_shipment));
                            builder.setMessage(getResources().getString(R.string.message_progress_complete) + "\n" + getResources().getString(R.string.text_receipt_number) + shipment.getReceiptNumber());
                            builder.setCancelable(false);
                            builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
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
                Log.d(getClass().getSimpleName(), "[ shipment prosess failed ]");
                shipment = null;
                resendShipment();
                break;

            case MenuUpdateJob.PROCESS_ID:
                Log.d(getClass().getSimpleName(), "[ menu update failed ]");
                count++;
                updateMenuError = true;
                if (count == maxSerialist) {
                    resendOrderMenuUpdate();
                }
                break;

            case SerialJob.PROCESS_ID:
//                AlertMessage("Gagal mengirim serial");
                Log.d(getClass().getSimpleName(), "[ serial job failed ]");
                count++;
                serialError = true;
                if (count == maxSerialist) {
                    Log.d(getClass().getSimpleName(), "[ serial job failed ]");
                    resendSerial();
                }
                break;

            case OrderUpdateJob.PROCESS_ID:
                AlertMessage(getString(R.string.message_failed_update_status_order));
                break;
        }

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

    private void checkOrderMenuSerialList() {
        serialNumberDatabaseAdapter = new SerialNumberDatabaseAdapter(this);
        orderMenuSerials = serialNumberDatabaseAdapter.getSerialNumberListByOrderId(orderId);

        if (orderMenuSerials.size() > 0) {
            for (int x = 0; x < orderMenuSerials.size(); x++) {
                String id = orderMenuSerials.get(x).getOrderMenu().getId();
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

    private void checkOrderMenuSerialListHasSync() {
        serialNumberDatabaseAdapter = new SerialNumberDatabaseAdapter(this);
        orderMenuSerials = serialNumberDatabaseAdapter.getSerialNumberListByOrderIdAndhasSync(orderId);

        if (orderMenuSerials.size() > 0) {
            for (int x = 0; x < orderMenuSerials.size(); x++) {
                String id = orderMenuSerials.get(x).getOrderMenu().getId();
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
        builder.setTitle(getResources().getString(R.string.text_shipment));
        builder.setMessage(message);
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private void AlertMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SellerOrderMenuListActivity.this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    public void openScanner(Intent data) {
        Intent scanner = data;
        scanner.putExtra("orderId", orderId);

        Log.d(getClass().getSimpleName(), "product name : " + data.getStringExtra("productName"));
        Log.d(getClass().getSimpleName(), "order Menu id : " + data.getStringExtra("orderMenuId"));

        startActivityForResult(scanner, requestScannerCode);
    }

    public void inputSerial(final Intent data) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.verify));
        builder.setItems(new String[]{getString(R.string.text_scan_serial_number), getString(R.string.text_manual_input_serial)}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent scanner = data;
                    scanner.putExtra("orderId", orderId);

                    Log.d(getClass().getSimpleName(), "product name : " + data.getStringExtra("productName"));
                    Log.d(getClass().getSimpleName(), "order Menu id : " + data.getStringExtra("orderMenuId"));

                    startActivityForResult(scanner, requestScannerCode);

                } else if (which == 1){
                    final String orderMenuId = data.getStringExtra("orderMenuId");
                    final String productId = data.getStringExtra("productId");
                    String productName = data.getStringExtra("productName");
                    final int qty = data.getIntExtra("productQty", 0);

                    AlertDialog.Builder manualDialog = new AlertDialog.Builder(SellerOrderMenuListActivity.this);
                    manualDialog.setCancelable(false);
                    manualDialog.setTitle(getString(R.string.message_title_enter_serial));
                    View customView = LayoutInflater.from(SellerOrderMenuListActivity.this).inflate(R.layout.view_manual_serial, null, false);
                    final EditText editSerial = (EditText) customView.findViewById(R.id.edit_serial);
                    final TextView textCount = (TextView) customView.findViewById(R.id.text_count_serial);
                    final TextInputLayout titleEditSerial = (TextInputLayout) customView.findViewById(R.id.title_edit_serial);
                    ImageButton btnEnter = (ImageButton) customView.findViewById(R.id.btn_enter);

                    List<OrderMenuSerial> sn = serialNumberDatabaseAdapter.getSerialNumberListByOrderIdAndOrderMenuId(orderId, orderMenuId);
                    final List<OrderMenuSerial> allSerial = sn;
                    textCount.setText(getString(R.string.text_already_inputted)+allSerial.size()+getResources().getString(R.string.text_verivy_of_total)+qty);

                    final List<String> serialstring = new ArrayList<String>();
                    for (OrderMenuSerial s:allSerial){
                        serialstring.add(s.getSerialNumber());
                    }

                    editSerial.setOnKeyListener(new View.OnKeyListener() {
                        @Override
                        public boolean onKey(View v, int keyCode, KeyEvent event) {
                            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)){
                                if (!editSerial.getText().toString().replace(" ","").matches("")) {
                                    if (allSerial.size() < qty) {
                                        if (!serialstring.contains(editSerial.getText().toString())) {
                                            titleEditSerial.setError(null);

                                            OrderMenuSerial orderMenuSerialObj = new OrderMenuSerial();
                                            orderMenuSerialObj.setId(DefaultDatabaseAdapter.generateId());
                                            orderMenuSerialObj.getOrderMenu().getOrder().setId(orderId);
                                            orderMenuSerialObj.getOrderMenu().setId(orderMenuId);
                                            orderMenuSerialObj.setSerialNumber(editSerial.getText().toString());
                                            allSerial.add(orderMenuSerialObj);
                                            tempSerial.add(orderMenuSerialObj);
                                            serialstring.add(editSerial.getText().toString());
                                            textCount.setText(getString(R.string.text_already_inputted) + allSerial.size() + getResources().getString(R.string.text_verivy_of_total) + qty);

                                            editSerial.getText().clear();
                                        } else {
                                            titleEditSerial.setError(getString(R.string.message_serial_exist));
                                            editSerial.getText().clear();
                                        }
                                    } else {
                                        AlertMessage(getResources().getString(R.string.message_serial_sufficed));
                                        editSerial.getText().clear();
                                    }
                                }
                            }
                            return false;
                        }
                    });

                    btnEnter.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!editSerial.getText().toString().replace(" ","").matches("")){
                                if (allSerial.size() < qty) {
                                    if (!serialstring.contains(editSerial.getText().toString())) {
                                        titleEditSerial.setError(null);

                                        OrderMenuSerial orderMenuSerialObj = new OrderMenuSerial();
                                        orderMenuSerialObj.setId(DefaultDatabaseAdapter.generateId());
                                        orderMenuSerialObj.getOrderMenu().getOrder().setId(orderId);
                                        orderMenuSerialObj.getOrderMenu().setId(orderMenuId);
                                        orderMenuSerialObj.setSerialNumber(editSerial.getText().toString());
                                        allSerial.add(orderMenuSerialObj);
                                        tempSerial.add(orderMenuSerialObj);
                                        serialstring.add(editSerial.getText().toString());
                                        textCount.setText(getString(R.string.text_already_inputted)+allSerial.size()+getResources().getString(R.string.text_verivy_of_total)+qty);

                                        editSerial.getText().clear();
                                    } else {
                                        titleEditSerial.setError(getString(R.string.message_serial_exist));
                                        editSerial.getText().clear();
                                    }
                                }else {
                                    AlertMessage(getResources().getString(R.string.message_serial_sufficed));
                                    editSerial.getText().clear();
                                }
                            }
                        }
                    });

                    manualDialog.setView(customView);
                    manualDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (tempSerial.size() > 0) {
                                loadProgressCheckSerial.show();
                                maxSerialistTocheck = tempSerial.size();
                                for (OrderMenuSerial s : tempSerial) {
                                    StockSync check = new StockSync(SellerOrderMenuListActivity.this, SellerOrderMenuListActivity.this, StockSync.StockUri.bySerialUri.name());
                                    check.execute(productId, s.getSerialNumber());
                                }
                            }
                        }
                    });
                    manualDialog.setNegativeButton(getString(R.string.text_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            verifiedSerial.clear();
                            unVerifiedSerial.clear();
                            tempSerial.clear();
                            maxSerialistTocheck = 0;
                            countTocheck = 0;
                            dialog.dismiss();
                        }
                    });
                    manualDialog.show();
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

    public void inputAllSerial() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.verify));
        builder.setItems(new String[]{"Download Master Sku",getString(R.string.text_serial_from_file)}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                if (which == 0) {
//                    Intent scanner = data;
//                    scanner.putExtra("orderId", orderId);
//
//                    Log.d(getClass().getSimpleName(), "product name : " + data.getStringExtra("productName"));
//                    Log.d(getClass().getSimpleName(), "order Menu id : " + data.getStringExtra("orderMenuId"));
//
//                    startActivityForResult(scanner, requestScannerCode);
//
//                } else if (which == 1){
//                    final String orderMenuId = data.getStringExtra("orderMenuId");
//                    final String productId = data.getStringExtra("productId");
//                    String productName = data.getStringExtra("productName");
//                    final int qty = data.getIntExtra("productQty", 0);
//
//                    AlertDialog.Builder manualDialog = new AlertDialog.Builder(SellerOrderMenuListActivity.this);
//                    manualDialog.setCancelable(false);
//                    manualDialog.setTitle(getString(R.string.message_title_enter_serial));
//                    View customView = LayoutInflater.from(SellerOrderMenuListActivity.this).inflate(R.layout.view_manual_serial, null, false);
//                    final EditText editSerial = (EditText) customView.findViewById(R.id.edit_serial);
//                    final TextView textCount = (TextView) customView.findViewById(R.id.text_count_serial);
//                    final TextInputLayout titleEditSerial = (TextInputLayout) customView.findViewById(R.id.title_edit_serial);
//                    ImageButton btnEnter = (ImageButton) customView.findViewById(R.id.btn_enter);
//
//                    List<OrderMenuSerial> sn = serialNumberDatabaseAdapter.getSerialNumberListByOrderIdAndOrderMenuId(orderId, orderMenuId);
//                    final List<OrderMenuSerial> allSerial = sn;
//                    textCount.setText(getString(R.string.text_already_inputted)+allSerial.size()+getResources().getString(R.string.text_verivy_of_total)+qty);
//
//                    final List<String> serialstring = new ArrayList<String>();
//                    for (OrderMenuSerial s:allSerial){
//                        serialstring.add(s.getOrderMenuSerial());
//                    }
//
//                    editSerial.setOnKeyListener(new View.OnKeyListener() {
//                        @Override
//                        public boolean onKey(View v, int keyCode, KeyEvent event) {
//                            if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER)){
//                                if (allSerial.size() < qty) {
//                                    if (!serialstring.contains(editSerial.getText().toString())) {
//                                        titleEditSerial.setError(null);
//
//                                        OrderMenuSerial serialNumberObj = new OrderMenuSerial();
//                                        serialNumberObj.setId(DefaultDatabaseAdapter.generateId());
//                                        serialNumberObj.getOrderMenu().getOrder().setId(orderId);
//                                        serialNumberObj.getOrderMenu().setId(orderMenuId);
//                                        serialNumberObj.setOrderMenuSerial(editSerial.getText().toString());
//                                        allSerial.add(serialNumberObj);
//                                        tempSerial.add(serialNumberObj);
//                                        serialstring.add(editSerial.getText().toString());
//                                        textCount.setText(getString(R.string.text_already_inputted)+allSerial.size()+getResources().getString(R.string.text_verivy_of_total)+qty);
//
//                                        editSerial.getText().clear();
//                                    } else {
//                                        titleEditSerial.setError(getString(R.string.message_serial_exist));
//                                        editSerial.getText().clear();
//                                    }
//                                }else {
//                                    AlertMessage(getResources().getString(R.string.message_serial_sufficed));
//                                    editSerial.getText().clear();
//                                }
//                            }
//                            return false;
//                        }
//                    });
//
//                    btnEnter.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            if (!editSerial.getText().toString().replace(" ","").matches("")){
//                                if (allSerial.size() < qty) {
//                                    if (!serialstring.contains(editSerial.getText().toString())) {
//                                        titleEditSerial.setError(null);
//
//                                        OrderMenuSerial serialNumberObj = new OrderMenuSerial();
//                                        serialNumberObj.setId(DefaultDatabaseAdapter.generateId());
//                                        serialNumberObj.getOrderMenu().getOrder().setId(orderId);
//                                        serialNumberObj.getOrderMenu().setId(orderMenuId);
//                                        serialNumberObj.setOrderMenuSerial(editSerial.getText().toString());
//                                        allSerial.add(serialNumberObj);
//                                        tempSerial.add(serialNumberObj);
//                                        serialstring.add(editSerial.getText().toString());
//                                        textCount.setText(getString(R.string.text_already_inputted)+allSerial.size()+getResources().getString(R.string.text_verivy_of_total)+qty);
//
//                                        editSerial.getText().clear();
//                                    } else {
//                                        titleEditSerial.setError(getString(R.string.message_serial_exist));
//                                        editSerial.getText().clear();
//                                    }
//                                }else {
//                                    AlertMessage(getResources().getString(R.string.message_serial_sufficed));
//                                    editSerial.getText().clear();
//                                }
//                            }
//                        }
//                    });
//
//                    manualDialog.setView(customView);
//                    manualDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            loadProgressCheckSerial.show();
//                            maxSerialistTocheck = tempSerial.size();
//                            for (OrderMenuSerial s : tempSerial){
//                                StockSync check = new StockSync(SellerOrderMenuListActivity.this, SellerOrderMenuListActivity.this, StockSync.StockUri.bySerialUri.name());
//                                check.execute(productId,s.getOrderMenuSerial());
//                            }
//                        }
//                    });
//                    manualDialog.setNegativeButton(getString(R.string.text_cancel), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            verifiedSerial.clear();
//                            unVerifiedSerial.clear();
//                            tempSerial.clear();
//                            maxSerialistTocheck = 0;
//                            countTocheck = 0;
//                            dialog.dismiss();
//                        }
//                    });
//                    manualDialog.show();
//                } else
                if (which == 0){
                    AlertDialog.Builder saveDialog = new AlertDialog.Builder(SellerOrderMenuListActivity.this);
                    saveDialog.setTitle("Save Options");
                    saveDialog.setItems(new String[]{"Save to csv", "Save to xls"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case 0:
                                    saveToCsvFile();
                                    break;
                                case 1:
                                    saveToExcelFile();
                                    break;
                            }
                        }
                    });
                    saveDialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    saveDialog.show();

                }else if (which == 1){
                    clearTemp();
                    Intent manager = new Intent(Intent.ACTION_GET_CONTENT);
                    manager.setType("text/plain");
                    startActivityForResult(manager, requestSerialFileCode);
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

    private void clearTemp(){
        verifiedSerial.clear();
        unVerifiedSerial.clear();
        tempSerial.clear();
        maxSerialistTocheck = 0;
        countTocheck = 0;
    }

    private void showVeriviedCheck(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        View view = LayoutInflater.from(this).inflate(R.layout.view_manual_input_check, null, false);
        TextView verifiedSize = (TextView) view.findViewById(R.id.text_verified_size);
        TextView unverifiedSize = (TextView) view.findViewById(R.id.text_unverified_size);
        ListView verifiedList = (ListView) view.findViewById(R.id.verified_serial);
        ListView unverifiedList = (ListView) view.findViewById(R.id.unverified_serial);

        verifiedSize.setText(Integer.toString(verifiedSerial.size()));
        unverifiedSize.setText(Integer.toString(unVerifiedSerial.size()));

        List<String> verifiedString = new ArrayList<String>();
        for (OrderMenuSerial s : verifiedSerial){
            verifiedString.add(s.getSerialNumber());
        }
        List<String> unverifiedString = new ArrayList<String>();
        for (OrderMenuSerial s : unVerifiedSerial){
            unverifiedString.add(s.getSerialNumber());
        }

        ArrayAdapter<String> verifiedAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, verifiedString);
        verifiedList.setAdapter(verifiedAdapter);
        ArrayAdapter<String> unverifiedAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, unverifiedString);
        unverifiedList.setAdapter(unverifiedAdapter);

        builder.setTitle(getResources().getString(R.string.text_serial_number));
        builder.setView(view);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                serialNumberDatabaseAdapter.save(verifiedSerial);
                checkOrderMenuSerialList();
                sellerOrderMenuAdapter.updateOrderMenuSerial(orderMenuListSerial);
                verifiedSerial.clear();
                unVerifiedSerial.clear();
                tempSerial.clear();
                maxSerialistTocheck = 0;
                countTocheck = 0;
            }
        });
        builder.setNegativeButton(getString(R.string.text_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                verifiedSerial.clear();
                unVerifiedSerial.clear();
                tempSerial.clear();
                maxSerialistTocheck = 0;
                countTocheck = 0;
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void resendShipment(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.text_shipment));
        builder.setCancelable(false);
        builder.setMessage(getString(R.string.message_process_failed_ask_repeat));
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                jobManager.addJobInBackground(new ShipmentJob(preferences.getString("server_url", ""), orderId));
                progressDialog.show();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void resendSerial(){
        serialError = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.text_shipment));
        builder.setCancelable(false);
        builder.setMessage(getString(R.string.message_process_failed_repeat));
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.show();
                List<OrderMenuSerial> serialList = serialNumberDatabaseAdapter.getSerialNumberListByOrderId(orderId);
                maxSerialist = 0;
                count = 0;
                maxSerialist = serialList.size();
                for (OrderMenuSerial snn : serialList) {
                    Log.d(getClass().getSimpleName(), " getted serial " + snn.getId() + " ================================================ ");
                    jobManager.addJobInBackground(new SerialJob(preferences.getString("server_url", ""), snn.getId(), shipment.getId()));
                }
            }
        });
        builder.show();
    }

    private void resendOrderMenuUpdate(){
        updateMenuError = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.text_shipment));
        builder.setCancelable(false);
        builder.setMessage(getString(R.string.message_process_failed_repeat));
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                progressDialog.show();
                maxSerialist = 0;
                count = 0;
                maxSerialist = orderMenuListSerial.size();

                checkOrderMenuSerialListHasSync();
                for (String om : orderMenuListSerial) {
                    Log.d(getClass().getSimpleName(), " access job order menu serial id " + om + " ================================================");

                    jobManager.addJobInBackground(new MenuUpdateJob(preferences.getString("server_url", ""), orderId, om));
                }
            }
        });
        builder.show();
    }


    /**
     * save master sku to csv file
     */
    private void saveToCsvFile(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date date = new Date();

        String downlowdTime = dateFormat.format(date);
        String csv = DirUtils.getDirectory()+File.separator+SignageVariables.CSV_TEMPLATE_NAME+downlowdTime+".csv";

        try {
            CSVWriter csvWriter = new CSVWriter(new FileWriter(csv));
            List<String[]> serialTemplate = new ArrayList<>();
            serialTemplate.add(new String[] {"Sku", "Serial Number"});

            for (OrderMenu menu : orderMenuList){
                for (int x = 0; x < menu.getQtyOrder(); x++) {
                    serialTemplate.add(new String[]{menu.getProduct().getName(), ""});
                }
            }

            csvWriter.writeAll(serialTemplate);
            csvWriter.close();
            AlertMessage("Master Sku","Download Complate");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * save master sku to xls file
     */
    private void saveToExcelFile() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        Date date = new Date();

        String downlowdTime = dateFormat.format(date);
        String excel = DirUtils.getDirectory()+File.separator+SignageVariables.CSV_TEMPLATE_NAME+downlowdTime+".xls";
        File file = new File(excel);

        Workbook workbook = new HSSFWorkbook();
        Cell cell = null;

        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setFillForegroundColor(HSSFColor.LIME.index);
        cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        Sheet sheet = null;
        sheet = workbook.createSheet();

        Row row = sheet.createRow(0);

        cell = row.createCell(0);
        cell.setCellValue("Sku");
        cell.setCellStyle(cellStyle);

        cell = row.createCell(1);
        cell.setCellValue("Serial Number");
        cell.setCellStyle(cellStyle);

        for (OrderMenu menu : orderMenuList){
            for (int x = 0; x < menu.getQtyOrder(); x++) {
                row = sheet.createRow(x+1);

                cell = row.createCell(0);
                cell.setCellValue(menu.getProduct().getName());
            }
        }

        sheet.setColumnWidth(0, (15 * 500));
        sheet.setColumnWidth(1, (15 * 500));

        FileOutputStream excelOut = null;
        try {
            excelOut = new FileOutputStream(file);
            workbook.write(excelOut);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (excelOut != null){
                try {
                    excelOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            AlertMessage("Master Sku","Download Complate");
        }
    }

    /**
     * read serial from csv file
     * @param pathSerial
     * @return
     */
    private List<SerialTemplate> readSerialFromCsv(String pathSerial){
        File serialFile = new File(pathSerial);
        FileInputStream serialStream = null;
        String splitCharacter = ",";

        List<SerialTemplate> serialList = new ArrayList<SerialTemplate>();
        try {
            serialStream = new FileInputStream(serialFile);
            DataInputStream serialDataStream = new DataInputStream(serialStream);
            BufferedReader buff = new BufferedReader(new InputStreamReader(serialDataStream));

            String serialLine = null;
            while ((serialLine = buff.readLine()) != null){
                String[] serialLineColl = serialLine.split(splitCharacter);
                if (serialLineColl.length > 1){

                    String name = serialLineColl[0];
                    String serial = serialLineColl[1];

                    OrderMenuSerial orderMenuSerialObj = new OrderMenuSerial();
                    orderMenuSerialObj.setId(DefaultDatabaseAdapter.generateId());
                    orderMenuSerialObj.getOrderMenu().getOrder().setId(orderId);
                    orderMenuSerialObj.getOrderMenu().setId(getOrderMenuId(name));
                    orderMenuSerialObj.setSerialNumber(serial);
                    tempSerial.add(orderMenuSerialObj);

                    SerialTemplate st = new SerialTemplate();
                    String sku = serialLineColl[0].replaceAll(" ","_");
                    st.setSku(sku);
                    st.setSerialNumber(serialLineColl[1]);
                    serialList.add(st);
                }
            }
            serialStream.close();

        }catch (Exception e){
            Log.d("serial file ", "failed reading serial file");
        }
        return serialList;
    }

    /**
     * read serial from excel file
     * @param pathSerial
     * @return
     */
    private List<SerialTemplate> readSerialFromExcel(String pathSerial){
        File serialFile = new File(pathSerial);


        List<SerialTemplate> serialList = new ArrayList<>();
//        try {
//            Workbook workbook = WorkbookFactory.create(serialFile);
//            Sheet sheet = workbook.getSheetAt(0);
//            Iterator rowIterator = sheet.rowIterator();
//
//            while (rowIterator.hasNext()){
//                Row row = (Row) rowIterator.next();
//                Iterator celIterator = row.cellIterator();
//                while (celIterator.hasNext()){
//                    Cell cell = (Cell) celIterator.next();
//                    Log.d("cell value", cell.toString());
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InvalidFormatException e) {
//            e.printStackTrace();
//        }

//        try {
//            XSSFWorkbook workbook = new XSSFWorkbook(serialFile);
//            XSSFSheet sheet = workbook.getSheetAt(0);
//            Iterator rowIterator = sheet.rowIterator();
//
//            while (rowIterator.hasNext()){
//                XSSFRow row = (XSSFRow) rowIterator.next();
//                Iterator celIterator = row.cellIterator();
//                while (celIterator.hasNext()){
//                    XSSFCell cell = (XSSFCell) celIterator.next();
//                    Log.d("cell value", cell.toString());
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InvalidFormatException e) {
//            e.printStackTrace();
//        }

        try {
            POIFSFileSystem poifsFileSystem = new POIFSFileSystem(serialFile);
            HSSFWorkbook workbook = new HSSFWorkbook(poifsFileSystem);
            HSSFSheet sheet = workbook.getSheetAt(0);
            Iterator rowIterator = sheet.rowIterator();

            while (rowIterator.hasNext()){
                HSSFRow row = (HSSFRow) rowIterator.next();
                Iterator celIterator = row.cellIterator();
                ArrayList<String> tempCell = new ArrayList<>();
                while (celIterator.hasNext()){
                    HSSFCell cell = (HSSFCell) celIterator.next();
                    String str = cell.toString();
                    if(str.contains(".0")){
                        tempCell.add(str.substring(0, str.length() - 2));
                    }else {
                        tempCell.add(str);
                    }
                }

                String name = tempCell.get(0);
                String serial = tempCell.get(1);

                OrderMenuSerial orderMenuSerialObj = new OrderMenuSerial();
                orderMenuSerialObj.setId(DefaultDatabaseAdapter.generateId());
                orderMenuSerialObj.getOrderMenu().getOrder().setId(orderId);
                orderMenuSerialObj.getOrderMenu().setId(getOrderMenuId(name));
                orderMenuSerialObj.setSerialNumber(serial);
                tempSerial.add(orderMenuSerialObj);


                SerialTemplate st = new SerialTemplate();
                String sku = tempCell.get(0).replaceAll(" ","_");
                st.setSku(sku);
                st.setSerialNumber(serial);
                serialList.add(st);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return serialList;
    }

    /**
     * get ordermenu id by product name
     * @param name
     * @return
     */
    private String getOrderMenuId(String name){
        for (OrderMenu m : orderMenuList){
            if (m.getProduct().getName().matches(name)){
                return m.getId();
            }
        }
        return null;
    }

}