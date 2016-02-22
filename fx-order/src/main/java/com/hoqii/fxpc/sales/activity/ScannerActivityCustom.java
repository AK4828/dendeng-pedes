package com.hoqii.fxpc.sales.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.zxing.ResultPoint;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.adapter.SerialAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.DefaultDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SerialNumberDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.SerialNumber;
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
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 12/16/15.
 */
public class ScannerActivityCustom extends AppCompatActivity {
    private String productName = "product name";
    private String orderMenuId, orderId;
    private int position, qty;

    private TextView productNameView, viewScannedCount;
    private SerialAdapter serialAdapter;
    private CompoundBarcodeView barcodeView;
    private RecyclerView serialRecycle;
    private CoordinatorLayout coordinatorLayout;
    private SerialNumberDatabaseAdapter serialNumberDatabaseAdapter;

    private List<SerialNumber> serialNumberList = new ArrayList<SerialNumber>();

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result != null) {

                Log.d(getClass().getSimpleName(), "serial : " + result.getText());
                Log.d(getClass().getSimpleName(), "product quantity : " + Integer.toString(qty));
                Log.d(getClass().getSimpleName(), "adapter quantity : " + Integer.toString(serialAdapter.getItemCount()));
                if (serialAdapter.getItemCount() < qty){
                    List<SerialNumber> sn = serialNumberDatabaseAdapter.getSerialNumberListByOrderId(orderId);
                    List<String> tempSerial = new ArrayList<String>();
                    List<String> tempSerialScan = new ArrayList<String>();

                    for (SerialNumber serial : sn){
                        tempSerial.add(serial.getSerialNumber());
                    }
                    for (SerialNumber s: serialAdapter.getSerialNumber()){
                        tempSerialScan.add(s.getSerialNumber());
                    }

                    if (!tempSerial.contains(result.getText()) && !tempSerialScan.contains(result.getText())){
                        barcodeView.setStatusText(result.getText());
                        SerialNumber s = new SerialNumber();
                        s.setSerialNumber(result.getText());
                        serialAdapter.addSerialNumber(s);

                        scannedCount();
                    }else {
                        Snackbar.make(coordinatorLayout, "serial number sudah di scan", Snackbar.LENGTH_LONG).show();
                    }


                }else {
                    Snackbar.make(coordinatorLayout, "Jumlah serial number sudah tercukupi", Snackbar.LENGTH_LONG).show();
                }

            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_scanner_custom);

        barcodeView = (CompoundBarcodeView) findViewById(R.id.barcode_scanner);
        barcodeView.setStatusText("Serial Number");
        barcodeView.decodeContinuous(callback);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(new IconDrawable(this, TypiconsIcons.typcn_chevron_left).colorRes(R.color.white).actionBarSize());

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordianotorLayout);

        if (getIntent().getExtras() != null) {
            orderId = getIntent().getStringExtra("orderId");
            orderMenuId = getIntent().getStringExtra("orderMenuId");
            productName = getIntent().getStringExtra("productName");
            qty = getIntent().getIntExtra("productQty", 0);
            position = getIntent().getIntExtra("position", 0);

            Log.d(getClass().getSimpleName(), "order id : " + orderId);
            Log.d(getClass().getSimpleName(), "order menu id : " + orderMenuId);
            Log.d(getClass().getSimpleName(), "product name : " + productName);
            Log.d(getClass().getSimpleName(), "product quantity : " + qty);
            Log.d(getClass().getSimpleName(), "position : " + position);

        }

        serialRecycle = (RecyclerView) findViewById(R.id.serial_number_recycle);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        serialRecycle.setLayoutManager(layoutManager);


        serialAdapter = new SerialAdapter(this, productName);
        serialRecycle.setAdapter(serialAdapter);

        productNameView = (TextView) findViewById(R.id.product_name);
        viewScannedCount = (TextView) findViewById(R.id.scanned_count);
        productNameView.setText(productName);

        serialNumberDatabaseAdapter = new SerialNumberDatabaseAdapter(this);

        serialNumberList = serialNumberDatabaseAdapter.getSerialNumberListByOrderIdAndOrderMenuId(orderId, orderMenuId);
        for (int x = 0; x < serialNumberList.size(); x++) {
            serialAdapter.addSerialNumber(serialNumberList.get(x));
        }


    }

    @Override
    protected void onResume() {
        super.onResume();

        barcodeView.resume();
        scannedCount();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.scanner_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (serialAdapter.getItemCount() == 0) {
                    Log.d(getClass().getSimpleName(), "serial kosong");
                    Intent dataSerial = new Intent();
                    dataSerial.putExtra("orderMenuId", orderMenuId);
                    dataSerial.putExtra("position", position);

                    setResult(RESULT_OK, dataSerial);
                    finish();
                }else {
                    super.onBackPressed();
                }
                break;
            case R.id.menu_submit_serial:
                if (serialAdapter.getItemCount() > 0){
                    dialogSubmit();
                }else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Serial Number");
                    builder.setMessage("Serial Number masih kosong");
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.show();
                }

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void dialogSubmit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Serial Number");
        builder.setMessage("Jumlah barang "+ qty +"\n" +
                "Jumlah barang yang di scan "+ serialAdapter.getItemCount() +"\n\n" +
                "Submit serial number ?");
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent dataSerial = new Intent();
                dataSerial.putExtra("orderMenuId", orderMenuId);
                dataSerial.putExtra("position", position);
                dataSerial.putExtra("status", true);

                for (SerialNumber serial : serialAdapter.getSerialNumber()) {

                    SerialNumber serialNumberObj = new SerialNumber();

                    serialNumberObj.setId(DefaultDatabaseAdapter.generateId());
                    serialNumberObj.getOrderMenu().getOrder().setId(orderId);
                    serialNumberObj.getOrderMenu().setId(orderMenuId);
                    serialNumberObj.setSerialNumber(serial.getSerialNumber());

                    List<String> serialNumber = new ArrayList<String>();
                    for (SerialNumber sn : serialNumberList){
                        serialNumber.add(sn.getSerialNumber());
                    }

                    if (!serialNumber.contains(serial.getSerialNumber())) {
                        serialNumberList.add(serialNumberObj);
                    }
                }

                serialNumberDatabaseAdapter.save(serialNumberList);
                Log.d(getClass().getSimpleName(), "Serial number saved");

                setResult(RESULT_OK, dataSerial);
                finish();

            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }

    public void scannedCount(){
        viewScannedCount.setText("Sudah di scan "+ serialAdapter.getItemCount() +" dari total "+ qty +" barang");
    }

    @Override
    public void onBackPressed() {
        if (serialAdapter.getItemCount() == 0) {
            Log.d(getClass().getSimpleName(), "serial kosong");
            Intent dataSerial = new Intent();
            dataSerial.putExtra("orderMenuId", orderMenuId);
            dataSerial.putExtra("position", position);

            setResult(RESULT_OK, dataSerial);
            finish();
        }else {
            super.onBackPressed();
        }
    }
}
