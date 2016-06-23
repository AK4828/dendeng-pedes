package com.hoqii.fxpc.sales.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.adapter.SoSkuAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SalesOrderDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SalesOrderMenuDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.SalesOrderMenu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 23/06/16.
 */
public class SalesOrderMenuActivity extends AppCompatActivity{

    private RecyclerView recyclerView;
    private SoSkuAdapter skuAdapter;
    private SalesOrderMenuDatabaseAdapter salesOrderMenuDatabaseAdapter;
    private SalesOrderDatabaseAdapter salesOrderDatabaseAdapter;

    private String SalesOrderId;
    private List<SalesOrderMenu> salesOrderMenuList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_order_menu);

        recyclerView = (RecyclerView) findViewById(R.id.eu_sales_order_menu);

        salesOrderDatabaseAdapter = new SalesOrderDatabaseAdapter(this);
        salesOrderMenuDatabaseAdapter = new SalesOrderMenuDatabaseAdapter(this);
        skuAdapter = new SoSkuAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(skuAdapter);

        SalesOrderId = salesOrderDatabaseAdapter.getSalesOrderId();
        salesOrderMenuList = salesOrderMenuDatabaseAdapter.findSalesOrderMenuByOrderId(SalesOrderId);
        Log.d(getClass().getSimpleName(), "sales order menu size "+salesOrderMenuList.size());
        for (SalesOrderMenu s : salesOrderMenuList){
            skuAdapter.addItems(s);
        }

    }
}
