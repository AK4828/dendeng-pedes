package com.hoqii.fxpc.sales.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.hoqii.fxpc.sales.R;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.TypiconsIcons;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by akm on 29/06/16.
 */
public class SaleDestinationActivity extends AppCompatActivity {

    @InjectView(R.id.main_toolbar) Toolbar mainToolbar;
    @InjectView(R.id.btn_so) AppCompatButton btnSo;
    @InjectView(R.id.btn_st) AppCompatButton btnSt;
    @InjectView(R.id.app_shadow) View appShadow;
    private boolean isMinLoli = false;
    private static final int SITE_REQUEST = 303;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_destination);
        ButterKnife.inject(this);

        setSupportActionBar(mainToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(new IconDrawable(this, TypiconsIcons.typcn_chevron_left).colorRes(R.color.white).actionBarSize());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isMinLoli = true;
            appShadow.setVisibility(View.GONE);
        } else {
            isMinLoli = false;
            appShadow.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.btn_so)
    public void onClickSalesOrder() {
        Intent so = new Intent(this, SalesOrderActivity.class);
        if (isMinLoli) {
            startActivity(so, ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle());
        } else {
            startActivity(so);
        }
    }

    @OnClick(R.id.btn_st)
    public void onClickOrder() {
        if (isMinLoli) {
            startActivityForResult(new Intent(this, SiteListActivity.class), SITE_REQUEST, ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle());
        } else {
            startActivityForResult(new Intent(this, SiteListActivity.class), SITE_REQUEST);
        }
    }
}
