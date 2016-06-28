package com.hoqii.fxpc.sales.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.adapter.SoSerialAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SalesOrderSerialDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.SalesOrderMenuSerial;

import java.util.List;
import java.util.UUID;

/**
 * Created by miftakhul on 23/06/16.
 */
public class SalesOrderMenuSerialActivity extends AppCompatActivity{

    private RecyclerView listSerial;
    private Button complateSerial, addSerial;
    private EditText editSerial;

    private SoSerialAdapter serialAdapter;
    private SalesOrderSerialDatabaseAdapter databaseAdapter;
    private String SoMenuId = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_order_serial);
        if (getIntent().getStringExtra("SoMenuId") != null) {
            SoMenuId = getIntent().getStringExtra("SoMenuId");
        }


        databaseAdapter = new SalesOrderSerialDatabaseAdapter(this);
        serialAdapter = new SoSerialAdapter(this);

        listSerial = (RecyclerView) findViewById(R.id.eu_serialList);
        complateSerial = (Button) findViewById(R.id.eu_serial_complete);
        addSerial = (Button) findViewById(R.id.eu_add_serial);
        editSerial = (EditText) findViewById(R.id.eu_serial);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        listSerial.setLayoutManager(layoutManager);
        listSerial.setItemAnimator(new DefaultItemAnimator());
        listSerial.setAdapter(serialAdapter);

        List<SalesOrderMenuSerial> serials = databaseAdapter.getSerialNumberListBySalesOrderMenuId(SoMenuId);
        if (serials.size() > 0){
            Log.d("serial","data found");
            for (SalesOrderMenuSerial s : serials){
                serialAdapter.addItem(s);
            }
        }

        addSerial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String serial = editSerial.getText().toString();
                if (!serial.equals("")){

                    SalesOrderMenuSerial s = new SalesOrderMenuSerial();
                    s.setId(UUID.randomUUID().toString());
                    s.getSalesOrderMenu().setId(SoMenuId);
                    s.setSerialNumber(serial);

                    serialAdapter.addItem(s);
                    editSerial.getText().clear();
                }
            }
        });

        complateSerial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serialAdapter.getItemCount() > 0) {
                    databaseAdapter.save(serialAdapter.getSerials());
                    finish();
                }
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
