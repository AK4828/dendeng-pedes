package com.hoqii.fxpc.sales.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.adapter.BusinessPartnerAdapter;
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

/**
 * Created by miftakhul on 12/6/15.
 */
public class BusinessPartnerActivity extends AppCompatActivity{

    private Toolbar toolbar;
    private RecyclerView recyclerView;

    private BusinessPartnerAdapter businessPartnerAdapter;
    private static final int CONTACT_REQUEST = 400;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_business_partner_list);

        Iconify
                .with(new FontAwesomeModule())
                .with(new EntypoModule())
                .with(new TypiconsModule())
                .with(new MaterialModule())
                .with(new MaterialCommunityModule())
                .with(new MeteoconsModule())
                .with(new WeathericonsModule())
                .with(new SimpleLineIconsModule())
                .with(new IoniconsModule());

        init();
        initSet();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home :
                super.onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void init(){
        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.business_recycler);

        businessPartnerAdapter = new BusinessPartnerAdapter(this);
    }

    private void initSet(){
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.businesPartner);
        actionBar.setHomeAsUpIndicator(new IconDrawable(this, TypiconsIcons.typcn_chevron_left).colorRes(R.color.white).actionBarSize());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(businessPartnerAdapter);

    }

    public void openContact(String businessPartnerId){
        Intent intent = new Intent(this, ContactsActivity.class);
        intent.putExtra("businessPartnerId", businessPartnerId);

        Log.d(getClass().getSimpleName(), "busines partner id " + businessPartnerId);
        startActivityForResult(intent, CONTACT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("result", "on");
        if (requestCode == CONTACT_REQUEST){
            if (resultCode == RESULT_OK){
                setResult(RESULT_OK);
                finish();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
