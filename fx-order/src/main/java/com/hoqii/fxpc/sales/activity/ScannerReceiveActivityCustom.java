package com.hoqii.fxpc.sales.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.ResultPoint;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.adapter.SerialAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.DefaultDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SerialNumberDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.OrderMenuSerial;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.TypiconsIcons;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Created by miftakhul on 12/16/15.
 */
public class ScannerReceiveActivityCustom extends AppCompatActivity {
    private String orderId, shipmentId;

    private TextView productNameView, viewScannedCount;
    private SerialAdapter serialAdapter;
    private CompoundBarcodeView barcodeView;
    private RecyclerView serialRecycle;
    private CoordinatorLayout coordinatorLayout;
    private SerialNumberDatabaseAdapter serialNumberDatabaseAdapter;
    private List<OrderMenuSerial> tempOrderMenuSerialList = new ArrayList<OrderMenuSerial>();

    private List<OrderMenuSerial> orderMenuSerialList = new ArrayList<OrderMenuSerial>();
    private boolean isScanning = true;


    public static final String KEY = "W06nIJR0uXN8WoZpqO5STOYDnyW59GQ9BMNy7egCWYo=";
    public static final SignatureAlgorithm KEY_ALGORITHM = SignatureAlgorithm.HS256;
    //    public static final SecretKey SECRET_KEY = new SecretKeySpec(new Base64Codec().decode(KEY),
//            SignatureAlgorithm.HS256.getJcaName());
    public static final SecretKey SECRET_KEY = new SecretKeySpec(Base64.decode(KEY, Base64.DEFAULT),
            SignatureAlgorithm.HS256.getJcaName());

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result != null) {

                if (isScanning == true){
                    String resultSerial = result.getText();
                    Log.d(getClass().getSimpleName(), "descripting ============================= "+resultSerial);

                    if (resultSerial.length() > 50){
                        Log.d(getClass().getSimpleName(), " > 50 ============================= "+resultSerial);

                        String claims = Jwts.parser()
                                .setSigningKey(SECRET_KEY)
                                .parsePlaintextJws(resultSerial).getBody();

                        Log.d(getClass().getSimpleName(), "descripting QRCODE ============================= "+claims);

                        String partStrings[] = claims.split(";");
                        String partSerial = partStrings[2];
                        Log.d(getClass().getSimpleName(), "part serial "+partSerial+"============================= "+partSerial);

                        checkSerial(partSerial);

                    }else {
                        Log.d(getClass().getSimpleName(), "< 50 ============================= "+resultSerial);
                        checkSerial(result.getText());

                    }
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
        setContentView(R.layout.acitivity_scanner_receive_custom);

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
            shipmentId = getIntent().getStringExtra("shipmentId");
            String jsonSerial = getIntent().getStringExtra("jsonSerial");
            ObjectMapper mapper = SignageApplication.getObjectMapper();
            try {
                tempOrderMenuSerialList = mapper.readValue(jsonSerial, new TypeReference<List<OrderMenuSerial>>(){});
            } catch (Exception e) {
                e.printStackTrace();
            }


            Log.d(getClass().getSimpleName(), "order id : " + orderId);
            Log.d(getClass().getSimpleName(), "tem serial size : " + tempOrderMenuSerialList.size());
        }

        serialRecycle = (RecyclerView) findViewById(R.id.serial_number_recycle);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        serialRecycle.setLayoutManager(layoutManager);

        serialAdapter = new SerialAdapter(this, null);
        serialRecycle.setAdapter(serialAdapter);

        productNameView = (TextView) findViewById(R.id.product_name);
        viewScannedCount = (TextView) findViewById(R.id.scanned_count);

        serialNumberDatabaseAdapter = new SerialNumberDatabaseAdapter(this);

        orderMenuSerialList = serialNumberDatabaseAdapter.getSerialNumberListByOrderId(orderId);
        for (int x = 0; x < orderMenuSerialList.size(); x++) {
            serialAdapter.addSerialNumber(orderMenuSerialList.get(x));
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
                    setResult(RESULT_OK);
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
                    builder.setTitle(getResources().getString(R.string.message_serial_title_serial_number));
                    builder.setMessage(getResources().getString(R.string.message_serial_empty));
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void dialogSubmit() {
        orderMenuSerialList = serialNumberDatabaseAdapter.getSerialNumberListByOrderId(orderId);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.message_serial_title_serial_number));
        builder.setMessage(getResources().getString(R.string.message_total_items)+ tempOrderMenuSerialList.size() +"\n" +
                getString(R.string.message_total_scanned_item)+ serialAdapter.getItemCount() +"\n\n" +
                getResources().getString(R.string.message_submit_serial_number));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (OrderMenuSerial serial : serialAdapter.getSerialNumber()) {

                    OrderMenuSerial orderMenuSerialObj = new OrderMenuSerial();

                    orderMenuSerialObj.setId(DefaultDatabaseAdapter.generateId());
                    orderMenuSerialObj.getOrderMenu().getOrder().setId(orderId);
                    orderMenuSerialObj.getOrderMenu().setId(null);
                    orderMenuSerialObj.setSerialNumber(serial.getSerialNumber());
                    orderMenuSerialObj.getShipment().setId(shipmentId);

                    List<String> serialNumber = new ArrayList<String>();
                    for (OrderMenuSerial sn : orderMenuSerialList){
                        serialNumber.add(sn.getSerialNumber());
                    }

                    if (!serialNumber.contains(serial.getSerialNumber())) {
                        orderMenuSerialList.add(orderMenuSerialObj);
                    }
                }

                serialNumberDatabaseAdapter.save(orderMenuSerialList);
                Log.d(getClass().getSimpleName(), "Serial number saved");

                setResult(RESULT_OK);
                finish();

            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    public void scannedCount(){
        viewScannedCount.setText(getResources().getString(R.string.text_serial_alredy_scan)+ serialAdapter.getItemCount() +getResources().getString(R.string.text_of_total)+ tempOrderMenuSerialList.size() +getResources().getString(R.string.text_item_end));
    }

    private void checkSerial(String serialNumber){
        Log.d(getClass().getSimpleName(), "serial : " + serialNumber);
        if (serialAdapter.getItemCount() < tempOrderMenuSerialList.size()){
            List<String> tempSerial = new ArrayList<String>();
            List<String> tempSerialScan = new ArrayList<String>();

            for (OrderMenuSerial serial : tempOrderMenuSerialList){
                tempSerial.add(serial.getSerialNumber());
            }
            for (OrderMenuSerial serial : serialAdapter.getSerialNumber()){
                tempSerialScan.add(serial.getSerialNumber());
            }

            if (tempSerial.contains(serialNumber)){
                if (!tempSerialScan.contains(serialNumber)){
                    barcodeView.setStatusText(serialNumber);
                    OrderMenuSerial s = new OrderMenuSerial();
                    for (OrderMenuSerial x : tempOrderMenuSerialList){
                        if (x.getSerialNumber().equalsIgnoreCase(serialNumber)){
                            s.getOrderMenu().getProduct().setName(x.getOrderMenu().getProduct().getName());
                        }
                    }
                    s.setSerialNumber(serialNumber);
                    serialAdapter.addSerialNumber(s);

                    scannedCount();
                }else {
                    Snackbar.make(coordinatorLayout, getResources().getString(R.string.text_serial_alredy_scan), Snackbar.LENGTH_LONG).show();
                    AlertMessage(getResources().getString(R.string.text_serial_alredy_scan));
                }

            }else {
                Snackbar.make(coordinatorLayout, getString(R.string.message_incorrect_serial_number), Snackbar.LENGTH_LONG).show();
                AlertMessage(getResources().getString(R.string.message_incorrect_serial_number));
            }

        }else {
            Snackbar.make(coordinatorLayout, getResources().getString(R.string.message_serial_sufficed), Snackbar.LENGTH_LONG).show();
            AlertMessage(getResources().getString(R.string.message_serial_sufficed));
        }
    }

    private void AlertMessage(String message){
        isScanning = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                isScanning = true;
            }
        });
        builder.show();
    }

    @Override
    public void onBackPressed() {
        if (serialAdapter.getItemCount() == 0) {
            setResult(RESULT_OK);
            finish();
        }else {
            super.onBackPressed();
        }
    }
}

