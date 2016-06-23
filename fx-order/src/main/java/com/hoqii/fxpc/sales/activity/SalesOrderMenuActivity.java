package com.hoqii.fxpc.sales.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.adapter.SoSkuAdapter;

/**
 * Created by miftakhul on 23/06/16.
 */
public class SalesOrderMenuActivity extends AppCompatActivity{

    private RecyclerView recyclerView;
    private SoSkuAdapter skuAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_order_menu);

        recyclerView = (RecyclerView) findViewById(R.id.eu_sales_order_menu);

        skuAdapter = new SoSkuAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(skuAdapter);



    }
}
