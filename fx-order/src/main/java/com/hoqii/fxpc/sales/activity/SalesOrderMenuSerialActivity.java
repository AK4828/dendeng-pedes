package com.hoqii.fxpc.sales.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.adapter.SoSerialAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.DefaultDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SalesOrderDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SalesOrderSerialDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.SalesOrderMenu;
import com.hoqii.fxpc.sales.entity.SalesOrderMenuSerial;
import com.hoqii.fxpc.sales.entity.SerialEvent;
import com.hoqii.fxpc.sales.task.StockSync;

import org.meruvian.midas.core.service.TaskService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 23/06/16.
 */
public class SalesOrderMenuSerialActivity extends AppCompatActivity implements TaskService{

    private static final int REQUEST_SCAN_CODE = 100;

    private RecyclerView listSerial;
    private Button complateSerial;
    private ProgressDialog loadProgressCheckSerial;
    private TextView productSku, skuQty;

    private SoSerialAdapter serialAdapter;
    private SalesOrderSerialDatabaseAdapter databaseAdapter;
    private SalesOrderDatabaseAdapter salesOrderDatabaseAdapter;
    private String SoMenuId = null, salesOrderId;

    //manual add serial
    private List<SalesOrderMenuSerial> tempSerial = new ArrayList<SalesOrderMenuSerial>();
    private List<SalesOrderMenuSerial> verifiedSerial = new ArrayList<SalesOrderMenuSerial>();
    private List<SalesOrderMenuSerial> unVerifiedSerial = new ArrayList<SalesOrderMenuSerial>();
    private int countTocheck = 0;
    private int maxSerialistTocheck = 0;
    private SalesOrderMenu salesordermenu;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_order_serial);

        ActionBar ac = getSupportActionBar();
        ac.setDisplayHomeAsUpEnabled(true);

        databaseAdapter = new SalesOrderSerialDatabaseAdapter(this);
        salesOrderDatabaseAdapter = new SalesOrderDatabaseAdapter(this);
        serialAdapter = new SoSerialAdapter(this);

        salesOrderId = salesOrderDatabaseAdapter.getSalesOrderId();

        listSerial = (RecyclerView) findViewById(R.id.eu_serialList);
        complateSerial = (Button) findViewById(R.id.eu_serial_complete);
        productSku = (TextView) findViewById(R.id.eu_sku);
        skuQty = (TextView) findViewById(R.id.eu_serial_count);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        listSerial.setLayoutManager(layoutManager);
        listSerial.setItemAnimator(new DefaultItemAnimator());
        listSerial.setAdapter(serialAdapter);

        if (getIntent().getStringExtra("salesOrderMenuId") != null) {
            SoMenuId = getIntent().getStringExtra("salesOrderMenuId");
            productSku.setText(getIntent().getStringExtra("productName"));
            skuInfoUpdate();
        }

        loadProgressCheckSerial = new ProgressDialog(this);
        loadProgressCheckSerial.setMessage("Checking availability serial number");
        loadProgressCheckSerial.setCancelable(false);

        complateSerial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serialAdapter.getItemCount() > 0) {
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sales_order_menu_serial, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            onBackPressed();
        }else if (id == R.id.menu_add_serial){
            inputSerial();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SCAN_CODE && resultCode == RESULT_OK){
            skuInfoUpdate();
        }
    }

    public void skuInfoUpdate(){
        List<SalesOrderMenuSerial> serials = databaseAdapter.getSerialNumberListBySalesOrderMenuId(SoMenuId);
        serialAdapter.clear();
        Log.d("serial","data found size "+serials.size());
        if (serials.size() > 0){
            Log.d("serial","data found");
            for (SalesOrderMenuSerial s : serials){
                serialAdapter.addItem(s);
            }
        }

        int qty = getIntent().getIntExtra("productQty", 0);
        List<SalesOrderMenuSerial> sn = databaseAdapter.getSerialNumberListBySalesOrderMenuId(SoMenuId);
        final List<SalesOrderMenuSerial> allSerial = sn;
        skuQty.setText(getString(R.string.text_already_inputted)+allSerial.size()+getResources().getString(R.string.text_verivy_of_total)+qty);
    }

    public void inputSerial() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.verify));
        builder.setItems(new String[]{getString(R.string.text_scan_serial_number), getString(R.string.text_manual_input_serial)}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent scanner = new Intent(SalesOrderMenuSerialActivity.this, ScannerSalesActivityCustom.class);
                    scanner.putExtra("salesOrderMenuId", getIntent().getStringExtra("salesOrderMenuId"));
                    scanner.putExtra("productId", getIntent().getStringExtra("productId"));
                    scanner.putExtra("productName", getIntent().getStringExtra("productName"));
                    scanner.putExtra("productQty", getIntent().getIntExtra("productQty", 0));


                    Log.d(getClass().getSimpleName(), "product name : " + getIntent().getStringExtra("productName"));
                    Log.d(getClass().getSimpleName(), "order Menu id : " + getIntent().getStringExtra("salesOrderMenuId"));

                    startActivityForResult(scanner, REQUEST_SCAN_CODE);

                } else if (which == 1){
                    final String salesOrderMenuId = getIntent().getStringExtra("salesOrderMenuId");
                    final String productId = getIntent().getStringExtra("productId");
                    String productName = getIntent().getStringExtra("productName");
                    final int qty = getIntent().getIntExtra("productQty", 0);

                    AlertDialog.Builder manualDialog = new AlertDialog.Builder(SalesOrderMenuSerialActivity.this);
                    manualDialog.setCancelable(false);
                    manualDialog.setTitle(getString(R.string.message_title_enter_serial));
                    View customView = LayoutInflater.from(SalesOrderMenuSerialActivity.this).inflate(R.layout.view_manual_serial, null, false);
                    final EditText editSerial = (EditText) customView.findViewById(R.id.edit_serial);
                    final TextView textCount = (TextView) customView.findViewById(R.id.text_count_serial);
                    final TextInputLayout titleEditSerial = (TextInputLayout) customView.findViewById(R.id.title_edit_serial);
                    ImageButton btnEnter = (ImageButton) customView.findViewById(R.id.btn_enter);

                    List<SalesOrderMenuSerial> sn = databaseAdapter.getSerialNumberListBySalesOrderMenuId(salesOrderMenuId);
                    final List<SalesOrderMenuSerial> allSerial = sn;
                    textCount.setText(getString(R.string.text_already_inputted)+allSerial.size()+getResources().getString(R.string.text_verivy_of_total)+qty);

                    final List<String> serialstring = new ArrayList<String>();
                    for (SalesOrderMenuSerial s:allSerial){
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

                                            SalesOrderMenuSerial orderMenuSerialObj = new SalesOrderMenuSerial();
                                            orderMenuSerialObj.setId(DefaultDatabaseAdapter.generateId());
                                            orderMenuSerialObj.getSalesOrderMenu().getSalesOrder().setId(salesOrderId);
                                            orderMenuSerialObj.getSalesOrderMenu().setId(salesOrderMenuId);
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

                                        SalesOrderMenuSerial orderMenuSerialObj = new SalesOrderMenuSerial();
                                        orderMenuSerialObj.setId(DefaultDatabaseAdapter.generateId());
                                        orderMenuSerialObj.getSalesOrderMenu().getSalesOrder().setId(salesOrderId);
                                        orderMenuSerialObj.getSalesOrderMenu().setId(salesOrderMenuId);
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
                                for (SalesOrderMenuSerial s : tempSerial) {
                                    StockSync check = new StockSync(SalesOrderMenuSerialActivity.this, SalesOrderMenuSerialActivity.this, StockSync.StockUri.bySerialUri.name());
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

    private void AlertMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SalesOrderMenuSerialActivity.this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private void AlertMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SalesOrderMenuSerialActivity.this);
        builder.setMessage(message);
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
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
        for (SalesOrderMenuSerial s : verifiedSerial){
            verifiedString.add(s.getSerialNumber());
        }
        List<String> unverifiedString = new ArrayList<String>();
        for (SalesOrderMenuSerial s : unVerifiedSerial){
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
                databaseAdapter.save(verifiedSerial);
                skuInfoUpdate();
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




    @Override
    public void onExecute(int code) {

    }

    @Override
    public void onSuccess(int code, Object result) {
        switch (code){
            case SignageVariables.SERIAL_CHECK_GET_TASK:
                Log.d("check serial", "y");
                countTocheck++;
                SerialEvent serialEvent = (SerialEvent) result;
                for (SalesOrderMenuSerial sn : tempSerial){
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

    }

    @Override
    public void onError(int code, String message) {
        switch (code){
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
}
