package com.hoqii.fxpc.sales.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.adapter.SoSkuAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SalesOrderDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SalesOrderSerialDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.SalesOrder;
import com.hoqii.fxpc.sales.entity.SalesOrderMenu;
import com.hoqii.fxpc.sales.entity.SalesOrderMenuSerial;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.job.JobCounting;
import com.hoqii.fxpc.sales.job.SalesOrderJob;
import com.hoqii.fxpc.sales.job.SalesOrderMenuJob;
import com.hoqii.fxpc.sales.job.SalesOrderMenuSerialJob;
import com.hoqii.fxpc.sales.job.SalesOrderUpdateJob;
import com.path.android.jobqueue.JobManager;

import java.util.List;
import java.util.UUID;

import de.greenrobot.event.EventBus;

/**
 * Created by miftakhul on 23/06/16.
 */
public class SalesOrderActivity extends AppCompatActivity {
    public static final int SERIAL_CODE = 100;
    private static final int REQUEST_SKU_CODE = 101;
    public static final int REQUEST_SKU_FINISH = 102;

    private SoSkuAdapter skuAdapter;
    private JobManager jobManager;
    private SharedPreferences preferences;
    private SalesOrderDatabaseAdapter salesOrderDatabaseAdapter;
    private SalesOrderSerialDatabaseAdapter databaseAdapter;
    private Button addsku;
    private ProgressDialog progressDialog;
    private JobCounting counting = new JobCounting();
    private JobCounting serialCounting = new JobCounting();
    private SalesOrder salesOrder;
    private String salesOrderId = null;

    private TextView textName, textEmail, textAddress, textTelephone;
//    private Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_order);

        addsku = (Button) findViewById(R.id.eu_add_sku);

//        toolbar = (Toolbar) findViewById(R.id.toolbar);
        textName = (TextView) findViewById(R.id.eu_name);
        textEmail = (TextView) findViewById(R.id.eu_email);
        textAddress = (TextView) findViewById(R.id.eu_address);
        textTelephone = (TextView) findViewById(R.id.eu_telephone);

        ActionBar ac = getSupportActionBar();
        ac.setDisplayHomeAsUpEnabled(true);

        skuAdapter = new SoSkuAdapter(this);
        jobManager = SignageApplication.getInstance().getJobManager();
        preferences = getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        salesOrderDatabaseAdapter = new SalesOrderDatabaseAdapter(this);
        databaseAdapter = new SalesOrderSerialDatabaseAdapter(this);

        addsku.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateSalesOrder();
                Intent i = new Intent(SalesOrderActivity.this, SalesOrderMenuActivity.class);
                startActivityForResult(i, REQUEST_SKU_CODE);
            }
        });

        progressDialog = new ProgressDialog(this);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        salesOrderId = salesOrderDatabaseAdapter.getSalesOrderId();
        if ( salesOrderId == null ) {
            jobManager.addJobInBackground(new SalesOrderJob(preferences.getString("server_url", "")));
        }else {
            salesOrder = salesOrderDatabaseAdapter.findOrderById(salesOrderId);
            Log.d(getClass().getSimpleName(), "sales order id exist : "+salesOrderId);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
            Log.d("start", "===");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
            Log.d("stopped","===");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SKU_CODE && resultCode == REQUEST_SKU_FINISH){
            finish();
        }
    }

    private void updateSalesOrder() {
        SalesOrder so = salesOrder;
        so.setName(textName.getText().toString());
        so.setEmail(textEmail.getText().toString());
        so.setAddress(textAddress.getText().toString());
        so.setTelephone(textTelephone.getText().toString());
        salesOrderDatabaseAdapter.updateSalesOrder(so);

//        jobManager.addJobInBackground(new SalesOrderUpdateJob(preferences.getString("server_url", ""), so));
    }

    public void onEventMainThread(GenericEvent.RequestInProgress requestInProgress){
        Log.d(getClass().getSimpleName(), "request id exex == "+requestInProgress.getProcessId());
        switch (requestInProgress.getProcessId()){
            case SignageVariables.SALES_RECEIPT_TASK:
                progressDialog.show();
                break;
        }
    }

    public void onEventMainThread(GenericEvent.RequestSuccess requestSuccess){
        switch (requestSuccess.getProcessId()) {
            case SignageVariables.SALES_RECEIPT_TASK:
                salesOrder = (SalesOrder) requestSuccess.getResponse().getContent();
                Log.d(getClass().getSimpleName(), "receipt success");
                progressDialog.dismiss();
                break;

        }
    }

    public void onEventMainThread(GenericEvent.RequestFailed requestFailed){
        switch (requestFailed.getProcessId()){
            case SignageVariables.SALES_RECEIPT_TASK:
                if (progressDialog.isShowing()) {
                    progressDialog.show();
                }
                break;
        }
    }



}
