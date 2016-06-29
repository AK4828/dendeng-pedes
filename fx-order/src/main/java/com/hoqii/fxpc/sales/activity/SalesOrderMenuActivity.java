package com.hoqii.fxpc.sales.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.adapter.SoSkuAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SalesOrderDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SalesOrderMenuDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SalesOrderSerialDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.SalesOrder;
import com.hoqii.fxpc.sales.entity.SalesOrderMenu;
import com.hoqii.fxpc.sales.entity.SalesOrderMenuSerial;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.job.JobCounting;
import com.hoqii.fxpc.sales.job.SalesOrderMenuJob;
import com.hoqii.fxpc.sales.job.SalesOrderMenuSerialJob;
import com.hoqii.fxpc.sales.job.SalesOrderUpdateJob;
import com.hoqii.fxpc.sales.util.AuthenticationCeck;
import com.path.android.jobqueue.JobManager;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by miftakhul on 23/06/16.
 */
public class SalesOrderMenuActivity extends AppCompatActivity{

    private static final int REQUEST_SKU = 300;
    public static final int REQUEST_SKU_SERIAL = 301;

    private Button complate;
    private RecyclerView recyclerView;
    private SoSkuAdapter skuAdapter;
    private SalesOrderMenuDatabaseAdapter salesOrderMenuDatabaseAdapter;
    private SalesOrderDatabaseAdapter salesOrderDatabaseAdapter;
    private SalesOrderSerialDatabaseAdapter serialDatabaseAdapter;
    private AuthenticationCeck authenticationCeck = new AuthenticationCeck();
    private JobManager jobManager;
    private SharedPreferences preferences;
    private ProgressDialog progressDialog;
    private JobCounting orderMenuCount = new JobCounting(), serialCount = new JobCounting();
    private String receiptNumber;

    private String SalesOrderId;
    private List<SalesOrderMenu> salesOrderMenuList = new ArrayList<>();


    //temp post data
    private List<SalesOrderMenu> salesOrderMenuPost = new ArrayList<>();
    private List<SalesOrderMenuSerial> serialsPost = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_order_menu);

        ActionBar ac = getSupportActionBar();
        ac.setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.eu_sales_order_menu);
        complate = (Button) findViewById(R.id.eu_ok);

        jobManager = SignageApplication.getInstance().getJobManager();
        preferences = getSharedPreferences(SignageVariables.PREFS_SERVER, 0);

        salesOrderDatabaseAdapter = new SalesOrderDatabaseAdapter(this);
        salesOrderMenuDatabaseAdapter = new SalesOrderMenuDatabaseAdapter(this);
        serialDatabaseAdapter = new SalesOrderSerialDatabaseAdapter(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.please_wait));

        skuAdapter = new SoSkuAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(skuAdapter);

        SalesOrderId = salesOrderDatabaseAdapter.getSalesOrderId();

        Log.d(getClass().getSimpleName(), "sales order is "+SalesOrderId);
        salesOrderMenuList = salesOrderMenuDatabaseAdapter.findSalesOrderMenuByOrderId(SalesOrderId);
        Log.d(getClass().getSimpleName(), "sales order menu size "+salesOrderMenuList.size());
        for (SalesOrderMenu s : salesOrderMenuList){
            skuAdapter.addItems(s);
        }

        complate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSerialSkuComplete()) {
                    SalesOrder salesOrder = salesOrderDatabaseAdapter.findOrderById(SalesOrderId);
                    receiptNumber = salesOrder.getReceiptNumber();

                    jobManager.addJobInBackground(new SalesOrderUpdateJob(preferences.getString("server_url", ""), salesOrder));
                    progressDialog.show();
                }
            }
        });

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sales_order_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                setResult(RESULT_OK);
                onBackPressed();
                break;
            case R.id.menu_add_item:
                Intent i = new Intent(this, SalesSkuActivity.class);
                startActivityForResult(i, REQUEST_SKU);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_SKU || requestCode == REQUEST_SKU_SERIAL ) && resultCode == RESULT_OK){
            salesOrderMenuList = salesOrderMenuDatabaseAdapter.findSalesOrderMenuByOrderId(SalesOrderId);
            Log.d(getClass().getSimpleName(), "sales order menu size "+salesOrderMenuList.size());
            skuAdapter.clear();
            for (SalesOrderMenu s : salesOrderMenuList){
                skuAdapter.addItems(s);
            }
        }
    }

    public void onEventMainThread(GenericEvent.RequestInProgress requestInProgress){
        Log.d(getClass().getSimpleName(), "request id exex == "+requestInProgress.getProcessId());
        switch (requestInProgress.getProcessId()){
            case SignageVariables.SALES_RECEIPT_TASK:
//                progressDialogReceipt.show();
                break;
            default:
                progressDialog.show();
                break;
        }
    }

    public void onEventMainThread(GenericEvent.RequestSuccess requestSuccess){
        switch (requestSuccess.getProcessId()) {
            case SignageVariables.SALES_POST_TASK:
                SalesOrder order = (SalesOrder) requestSuccess.getResponse().getContent();
                salesOrderMenuPost = salesOrderMenuDatabaseAdapter.findSalesOrderMenuByOrderId(SalesOrderId);
                for (SalesOrderMenu s : salesOrderMenuPost){
                    jobManager.addJobInBackground(new SalesOrderMenuJob(preferences.getString("server_url", ""), order.getId(), s));
                }
                break;

            case SignageVariables.SALES_MENU_POST_TASK:
                if (orderMenuCount.isJobFinish(salesOrderMenuPost.size())){

                    Log.d(getClass().getSimpleName(), "sales ordermenu job finish");
                    serialsPost = serialDatabaseAdapter.getSerialNumberListBySalesOrderId(SalesOrderId);
                    for (SalesOrderMenuSerial s : serialsPost) {
                        jobManager.addJobInBackground(new SalesOrderMenuSerialJob(preferences.getString("server_url", ""), s));
                    }
                }
                break;
            case SignageVariables.SALES_MENU_SERIAL_POST_TASK:
                Log.d(getClass().getSimpleName(), "====== serial success size "+serialsPost.size());
                if (serialCount.isJobFinish(serialsPost.size())) {
                    Log.d(getClass().getSimpleName(), "====== serial success finish "+serialsPost.size());
                    progressDialog.dismiss();
                    AlertMessageComplete(getString(R.string.text_message_success));
                    Log.d(getClass().getSimpleName(), "complate from success called");
//                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void onEventMainThread(GenericEvent.RequestFailed requestFailed){
        switch (requestFailed.getProcessId()) {
            case SignageVariables.SALES_POST_TASK:
                Toast.makeText(this, R.string.text_message_failed, Toast.LENGTH_SHORT).show();
                break;

            case SignageVariables.SALES_MENU_POST_TASK:
                if (orderMenuCount.isJobFinish(salesOrderMenuPost.size())){

                    Log.d(getClass().getSimpleName(), "sales ordermenu job finish");
                    serialsPost = serialDatabaseAdapter.getSerialNumberListBySalesOrderId(SalesOrderId);
                    for (SalesOrderMenuSerial s : serialsPost) {
                        jobManager.addJobInBackground(new SalesOrderMenuSerialJob(preferences.getString("server_url", ""), s));
                    }
                }
                break;
            case SignageVariables.SALES_MENU_SERIAL_POST_TASK:
                Log.d(getClass().getSimpleName(), "====== serial failed size "+serialsPost.size());
                if (serialCount.isJobFinish(serialsPost.size())) {
                    Log.d(getClass().getSimpleName(), "====== serial failed finish size "+serialsPost.size());
                    progressDialog.dismiss();
                    AlertMessageComplete(getString(R.string.text_message_success));
                    Log.d(getClass().getSimpleName(), "complate from failed called");
//                    Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void orderUpdate(String jsonProduct, int qty) {
        if (authenticationCeck.isNetworkAvailable()) {
            if (authenticationCeck.isAccess()) {
                Log.d(getClass().getSimpleName(), "[ application access granted ]");
                Log.d(getClass().getSimpleName(), "[ update order run ]");
                Intent i = new Intent(this, SalesOrderDetailActivity.class);
                i.putExtra("jsonProduct", jsonProduct);
                i.putExtra("qtyUpdate", qty);
                startActivityForResult(i, REQUEST_SKU);
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getString(R.string.message_title_internet_access));
            builder.setMessage(getResources().getString(R.string.message_no_internet));
            builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    public void refresh(){
        salesOrderMenuList = salesOrderMenuDatabaseAdapter.findSalesOrderMenuByOrderId(SalesOrderId);
        Log.d(getClass().getSimpleName(), "sales order menu size "+salesOrderMenuList.size());
        skuAdapter.clear();
        for (SalesOrderMenu s : salesOrderMenuList){
            skuAdapter.addItems(s);
        }
    }

    private void AlertMessageComplete(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SalesOrderMenuActivity.this);
        builder.setTitle(getString(R.string.text_message_title_sales_order));
        builder.setMessage(message+"\nReceiptnumber "+receiptNumber);
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                setResult(SalesOrderActivity.REQUEST_SKU_FINISH);
                finish();
            }
        });
        builder.show();
    }

    private void AlertMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SalesOrderMenuActivity.this);
        builder.setTitle(getString(R.string.text_message_title_sales_order));
        builder.setMessage(message);
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    private boolean isSerialSkuComplete(){
        List<SalesOrderMenu> salesOrderMenus = salesOrderMenuDatabaseAdapter.findSalesOrderMenuByOrderId(SalesOrderId);
        if (salesOrderMenus.size() > 0) {
            for (SalesOrderMenu s : salesOrderMenus) {
                int qty = s.getQty();
                int serialCount = serialDatabaseAdapter.getSerialNumberListBySalesOrderMenuId(s.getId()).size();
                if (serialCount < qty) {
                    AlertMessage(getString(R.string.text_sku_serial_exc));
                    return false;
                }
            }
            return true;
        }else {
            AlertMessage(getString(R.string.text_message_select_sku));
            return false;
        }
    }

}
