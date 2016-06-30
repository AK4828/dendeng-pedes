package com.hoqii.fxpc.sales.activity;

import android.app.ProgressDialog;
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
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.zxing.ResultPoint;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.adapter.SalesSerialAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.DefaultDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SalesOrderDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SalesOrderSerialDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.OrderMenuSerial;
import com.hoqii.fxpc.sales.entity.SalesOrderMenuSerial;
import com.hoqii.fxpc.sales.task.StockSync;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.TypiconsIcons;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import org.meruvian.midas.core.service.TaskService;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Created by miftakhul on 12/16/15.
 */
public class ScannerSalesActivityCustom extends AppCompatActivity implements TaskService {
    private String productName = "product name", productId = null;
    private String salesOrderMenuId, salesOrderId, tempSerial = null;
    private int qty;
    private boolean isScanning = true;

    private TextView productNameView, viewScannedCount;
    private SalesSerialAdapter serialAdapter;
    private CompoundBarcodeView barcodeView;
    private RecyclerView serialRecycle;
    private CoordinatorLayout coordinatorLayout;
    private SalesOrderDatabaseAdapter salesOrderDatabaseAdapter;
    private SalesOrderSerialDatabaseAdapter serialNumberDatabaseAdapter;
    private List<SalesOrderMenuSerial> orderMenuSerialList = new ArrayList<SalesOrderMenuSerial>();
    private ProgressDialog dialog;

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
                if (isScanning == true) {

                    String resultSerial = result.getText();
                    Log.d(getClass().getSimpleName(), "descripting ============================= " + resultSerial);

                    if (resultSerial.length() > 50) {
                        Log.d(getClass().getSimpleName(), " > 50 ============================= " + resultSerial);

                        String claims = Jwts.parser()
                                .setSigningKey(SECRET_KEY)
                                .parsePlaintextJws(resultSerial).getBody();

                        Log.d(getClass().getSimpleName(), "descripting QRCODE ============================= " + claims);

                        testDesc(claims);

                        String partStrings[] = claims.split(";");
                        String partSerial = partStrings[2];
                        Log.d(getClass().getSimpleName(), "part serial " + partSerial + "============================= " + partSerial);

                        checkSerial(partSerial);

                    } else {
                        Log.d(getClass().getSimpleName(), "< 50 ============================= " + resultSerial);
                        checkSerial(result.getText());

                    }
                }
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {

        }
    };

    private void testDesc(String des) {
        Log.d("hasil desc : ", "" + des);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_scanner_custom);

        barcodeView = (CompoundBarcodeView) findViewById(R.id.barcode_scanner);
        barcodeView.setStatusText(getResources().getString(R.string.message_serial_title_serial_number));
        barcodeView.decodeContinuous(callback);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(new IconDrawable(this, TypiconsIcons.typcn_chevron_left).colorRes(R.color.white).actionBarSize());

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordianotorLayout);
        salesOrderDatabaseAdapter = new SalesOrderDatabaseAdapter(this);

        if (getIntent().getExtras() != null) {
            salesOrderId = salesOrderDatabaseAdapter.getSalesOrderId();
            salesOrderMenuId = getIntent().getStringExtra("salesOrderMenuId");
            productId = getIntent().getStringExtra("productId");
            productName = getIntent().getStringExtra("productName");
            qty = getIntent().getIntExtra("productQty", 0);

            Log.d(getClass().getSimpleName(), "order id : " + salesOrderId);
            Log.d(getClass().getSimpleName(), "order menu id : " + salesOrderMenuId);
            Log.d(getClass().getSimpleName(), "product name : " + productName);
            Log.d(getClass().getSimpleName(), "product quantity : " + qty);

        }

        serialRecycle = (RecyclerView) findViewById(R.id.serial_number_recycle);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        serialRecycle.setLayoutManager(layoutManager);


        serialAdapter = new SalesSerialAdapter(this, productName);
        serialRecycle.setAdapter(serialAdapter);

        productNameView = (TextView) findViewById(R.id.product_name);
        viewScannedCount = (TextView) findViewById(R.id.scanned_count);
        productNameView.setText(productName);

        serialNumberDatabaseAdapter = new SalesOrderSerialDatabaseAdapter(this);
        orderMenuSerialList = serialNumberDatabaseAdapter.getSerialNumberListBySalesOrderMenuId(salesOrderMenuId);
        for (int x = 0; x < orderMenuSerialList.size(); x++) {
            serialAdapter.addSerialNumber(orderMenuSerialList.get(x));
        }

        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage(getString(R.string.progress_check_serial));

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
                setResult(RESULT_OK);
                finish();
                break;
            case R.id.menu_submit_serial:
                if (serialAdapter.getItemCount() > 0) {
                    dialogSubmit();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getString(R.string.message_title_serial));
                    builder.setMessage(getString(R.string.message_serial_empty));
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

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void onExecute(int code) {
        dialog.setTitle(getString(R.string.text_serial) + tempSerial);
        dialog.show();
        isScanning = false;
    }

    @Override
    public void onSuccess(int code, Object result) {
        dialog.dismiss();

        if (code == SignageVariables.STOCK_GET_TASK) {
            boolean status = (boolean) result;
            if (status == true) {
                SalesOrderMenuSerial s = new SalesOrderMenuSerial();
                s.setSerialNumber(tempSerial);
                serialAdapter.addSerialNumber(s);
                tempSerial = null;
                scannedCount();
                isScanning = true;
            } else {
                AlertMessage(getString(R.string.message_serial_not_found));
            }
        }

    }

    @Override
    public void onCancel(int code, String message) {
        dialog.dismiss();
        tempSerial = null;
    }

    @Override
    public void onError(int code, String message) {
        dialog.dismiss();
        AlertMessageProblem();
    }


    private void dialogSubmit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.message_serial_title_serial_number));
        builder.setMessage(getString(R.string.message_total_items) + qty + "\n" +
                getString(R.string.message_total_items_scanned) + serialAdapter.getItemCount() + "\n\n" +
                getString(R.string.message_submit_serial_number));
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent dataSerial = new Intent();
                dataSerial.putExtra("salesOrderMenuId", salesOrderMenuId);
                dataSerial.putExtra("status", true);

                orderMenuSerialList = serialNumberDatabaseAdapter.getSerialNumberListBySalesOrderMenuId(salesOrderMenuId);
                for (SalesOrderMenuSerial serial : serialAdapter.getSerialNumber()) {

                    SalesOrderMenuSerial orderMenuSerialObj = new SalesOrderMenuSerial();

                    orderMenuSerialObj.setId(DefaultDatabaseAdapter.generateId());
                    orderMenuSerialObj.getSalesOrderMenu().getSalesOrder().setId(salesOrderId);
                    orderMenuSerialObj.getSalesOrderMenu().setId(salesOrderMenuId);
                    orderMenuSerialObj.setSerialNumber(serial.getSerialNumber());

                    List<String> serialNumber = new ArrayList<String>();
                    for (SalesOrderMenuSerial sn : orderMenuSerialList) {
                        serialNumber.add(sn.getSerialNumber());
                    }

                    if (!serialNumber.contains(serial.getSerialNumber())) {
                        orderMenuSerialList.add(orderMenuSerialObj);
                    }
                }

                serialNumberDatabaseAdapter.save(orderMenuSerialList);
                Log.d(getClass().getSimpleName(), "Serial number saved");

                setResult(RESULT_OK, dataSerial);
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

    public void scannedCount() {
        viewScannedCount.setText(getString(R.string.text_serial_alredy_scan) + serialAdapter.getItemCount() + getString(R.string.text_of_total) + qty + getResources().getString(R.string.text_item_end));
    }

    private void checkSerial(String serialnumber) {
        tempSerial = serialnumber;

        Log.d(getClass().getSimpleName(), "serial : " + serialnumber);
        Log.d(getClass().getSimpleName(), "product quantity : " + Integer.toString(qty));
        Log.d(getClass().getSimpleName(), "adapter quantity : " + Integer.toString(serialAdapter.getItemCount()));
        if (serialAdapter.getItemCount() < qty) {
            List<SalesOrderMenuSerial> sn = serialNumberDatabaseAdapter.getSerialNumberListBySalesOrderId(salesOrderId);
            List<String> tempSerial = new ArrayList<String>();
            List<String> tempSerialScan = new ArrayList<String>();

            for (SalesOrderMenuSerial serial : sn) {
                tempSerial.add(serial.getSerialNumber());
            }
            for (SalesOrderMenuSerial s : serialAdapter.getSerialNumber()) {
                tempSerialScan.add(s.getSerialNumber());
            }

            if (!tempSerial.contains(serialnumber) && !tempSerialScan.contains(serialnumber)) {
                StockSync check = new StockSync(this, this, StockSync.StockUri.bySerialUri.name());
                check.execute(productId, serialnumber);
                barcodeView.setStatusText(serialnumber);
            } else {
                Snackbar.make(coordinatorLayout, getResources().getString(R.string.text_serial_alredy_scan), Snackbar.LENGTH_LONG).show();
            }
        } else {
            Snackbar.make(coordinatorLayout, getResources().getString(R.string.message_serial_sufficed), Snackbar.LENGTH_LONG).show();
        }

    }

    private void AlertMessage(String message) {
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

    private void AlertMessageProblem() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.message_serial_problem_repeat));
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkSerial(tempSerial);
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                isScanning = true;
            }
        });
        builder.show();
    }

}
