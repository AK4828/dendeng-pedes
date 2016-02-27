package com.hoqii.fxpc.sales.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageAppication;
import com.hoqii.fxpc.sales.adapter.ShipmentDetailPagerAdapter;
import com.hoqii.fxpc.sales.entity.Shipment;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.TypiconsIcons;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by miftakhul on 2/10/16.
 */
public class ShipmentHistoryDetailActivity extends AppCompatActivity{

    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Shipment shipment;
    private TextView shipDate, orderNumber, orderDate, to;
    private String shipmentJson;
    private View appShadow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_shipment_history_detail);

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Shipment Detail");
        actionBar.setHomeAsUpIndicator(new IconDrawable(this, TypiconsIcons.typcn_chevron_left).colorRes(R.color.white).actionBarSize());

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabView);
        shipDate = (TextView) findViewById(R.id.s_shipment_date);
        orderNumber = (TextView) findViewById(R.id.s_order_number);
        orderDate= (TextView) findViewById(R.id.s_order_date);
        appShadow = (View) findViewById(R.id.app_shadow);
        to = (TextView) findViewById(R.id.s_site);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new Fade());
        }else {
            appShadow.setVisibility(View.VISIBLE);
        }

        if (getIntent() != null){
            shipmentJson = getIntent().getStringExtra("shipmentJson");

            Log.d(getClass().getSimpleName(), "data detail :" +shipmentJson);

            ObjectMapper mapper = SignageAppication.getObjectMapper();
            try {
                shipment = mapper.readValue(shipmentJson, Shipment.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy / hh:mm:ss");
        Date dateSend = new Date();
        dateSend.setTime(shipment.getLogInformation().getCreateDate().getTime());

        Date dateOrder = new Date();
        dateOrder.setTime(shipment.getOrder().getLogInformation().getCreateDate().getTime());

        to.setText("Ship to : "+shipment.getOrder().getSiteFrom().getName());
        shipDate.setText("Shipment date : " + simpleDateFormat.format(dateSend));
        orderNumber.setText("Order number : "+shipment.getReceiptNumber());
        orderDate.setText("Order date : "+simpleDateFormat.format(dateOrder));

        ShipmentDetailPagerAdapter shipmentDetailPagerAdapter = new ShipmentDetailPagerAdapter(getSupportFragmentManager(), shipmentJson);
        viewPager.setAdapter(shipmentDetailPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
