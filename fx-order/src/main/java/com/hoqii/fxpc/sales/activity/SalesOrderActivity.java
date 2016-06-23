package com.hoqii.fxpc.sales.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

    private String SoId = UUID.randomUUID().toString();

    private SoSkuAdapter skuAdapter;
    private JobManager jobManager;
    private SharedPreferences preferences;
    private SalesOrderSerialDatabaseAdapter databaseAdapter;
    private Button addsku;
    private RecyclerView skuRecicler;
    private EditText sku;
    private ProgressDialog progressDialog;
    private ProgressDialog progressDialogReceipt;
    private JobCounting counting = new JobCounting();
    private JobCounting serialCounting = new JobCounting();
    private SalesOrder salesOrder;

    private TextView textName, textEmail, textAddress, textTelephone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_order);

        addsku = (Button) findViewById(R.id.eu_add_sku);
//        skuRecicler = (RecyclerView) findViewById(R.id.eu_sku_list);
//        sku = (EditText) findViewById(R.id.eu_sku);

        textName = (TextView) findViewById(R.id.eu_name);
        textEmail = (TextView) findViewById(R.id.eu_email);
        textAddress = (TextView) findViewById(R.id.eu_address);
        textTelephone = (TextView) findViewById(R.id.eu_telephone);

        skuAdapter = new SoSkuAdapter(this);
        jobManager = SignageApplication.getInstance().getJobManager();
        preferences = getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        databaseAdapter = new SalesOrderSerialDatabaseAdapter(this);

        addsku.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SalesOrderActivity.this, SalesSkuActivity.class);
                startActivity(i);
            }
        });

//        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        skuRecicler.setItemAnimator(new DefaultItemAnimator());
//        skuRecicler.setAdapter(skuAdapter);
//        skuRecicler.setLayoutManager(layoutManager);
//
//        addsku.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String textSku = sku.getText().toString();
//                skuAdapter.addItem(SoId, textSku);
//
//                sku.getText().clear();
//            }
//        });
//
        progressDialog = new ProgressDialog(this);
//        progressDialogReceipt = new ProgressDialog(this);
//        progressDialogReceipt.setCancelable(false);
//        progressDialogReceipt.setMessage("Pleace wait");
//
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
            Log.d("start", "===yes ");
        }

        jobManager.addJobInBackground(new SalesOrderJob(preferences.getString("server_url", "")));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        } else if (id == R.id.menu_done) {
            postSalesOrder();
        }
        return super.onOptionsItemSelected(item);
    }

    private void postSalesOrder() {
        Log.d(getClass().getSimpleName(), "post so");
        SalesOrder so = salesOrder;
        so.setName(textName.getText().toString());
        so.setEmail(textEmail.getText().toString());
        so.setAddress(textAddress.getText().toString());
        so.setTelephone(textTelephone.getText().toString());

        jobManager.addJobInBackground(new SalesOrderUpdateJob(preferences.getString("server_url", ""), so));
    }

    public void onEventMainThread(GenericEvent.RequestInProgress requestInProgress){
        Log.d(getClass().getSimpleName(), "request id exex == "+requestInProgress.getProcessId());
        switch (requestInProgress.getProcessId()){
            case SignageVariables.SALES_RECEIPT_TASK:
                progressDialogReceipt.show();
                break;
            default:
                progressDialog.show();
                break;
        }
    }

    public void onEventMainThread(GenericEvent.RequestSuccess requestSuccess){
        switch (requestSuccess.getProcessId()) {
            case SignageVariables.SALES_RECEIPT_TASK:
                salesOrder = (SalesOrder) requestSuccess.getResponse().getContent();
                Log.d(getClass().getSimpleName(), "receipt success");
                progressDialogReceipt.dismiss();
                break;
            case SignageVariables.SALES_POST_TASK:
                String refId = requestSuccess.getRefId();
                List<SalesOrderMenu> salesOrderMenus = skuAdapter.getItems();

                for (SalesOrderMenu s : salesOrderMenus) {
                    jobManager.addJobInBackground(new SalesOrderMenuJob(preferences.getString("server_url", ""), refId, s));

                    if (counting.isJobFinish(salesOrderMenus.size())) {
                        Log.d(getClass().getSimpleName(), "counting job finish");
                        List<SalesOrderMenuSerial> serialList = databaseAdapter.getActiveSerialNumberList();


                        for (SalesOrderMenuSerial ss : serialList) {
                            jobManager.addJobInBackground(new SalesOrderMenuSerialJob(preferences.getString("server_url", ""), ss));
                            Log.d(getClass().getSimpleName(), "serial joooo execute");
                            if (serialCounting.isJobFinish(serialList.size())) {
                                progressDialog.dismiss();
                            }

                        }
                    }
                }
                break;
        }
    }

    public void onEventMainThread(GenericEvent.RequestFailed requestFailed){
        switch (requestFailed.getProcessId()){
            case SignageVariables.SALES_RECEIPT_TASK:
                if (progressDialogReceipt.isShowing()) {
                    progressDialogReceipt.dismiss();
                }
                break;
            default:
                if (progressDialog.isShowing()) {
                    progressDialog.show();
                }
                break;
        }
    }



}
