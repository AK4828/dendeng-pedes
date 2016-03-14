package com.hoqii.fxpc.sales.activity;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.adapter.MainFragmentStateAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.OrderDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.entity.Stock;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.event.LoginEvent;
import com.hoqii.fxpc.sales.job.RefreshTokenJob;
import com.hoqii.fxpc.sales.task.StockSync;
import com.hoqii.fxpc.sales.util.AuthenticationCeck;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.EntypoIcons;
import com.path.android.jobqueue.JobManager;

import org.meruvian.midas.core.service.TaskService;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by miftakhul on 11/13/15.
 */
public class MainActivityMaterial extends AppCompatActivity implements TaskService{
    public static final int REFRESH_TOKEN_MAINMATERIAL = 400;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private boolean isMinLoli = false;
    private static final int ORDER_REQUEST = 300;
    private List<Stock> stocks = new ArrayList<Stock>();
    private ProgressDialog progress;
    private AuthenticationCeck authenticationCeck = new AuthenticationCeck();
    private OrderDatabaseAdapter orderDatabaseAdapter;
    private Order order = new Order();
    private String siteId = null;


    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_material);
        EventBus.getDefault().register(this);
        tabLayout = (TabLayout) findViewById(R.id.main_tab);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isMinLoli = true;
            getWindow().setEnterTransition(new Fade());
        } else {
            isMinLoli = false;
        }

        orderDatabaseAdapter = new OrderDatabaseAdapter(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeAsUpIndicator(new IconDrawable(this, EntypoIcons.entypo_chevron_left).colorRes(R.color.white).actionBarSize());
        viewPager = (ViewPager) findViewById(R.id.main_viewPager);

        progress = new ProgressDialog(this);
        progress.setMessage(getResources().getString(R.string.message_wait));
        progress.setCancelable(false);

        if (getIntent() != null){
            siteId = getIntent().getStringExtra("siteId");
            if (authenticationCeck.isNetworkAvailable()){
                if (authenticationCeck.isAccess()){
                    StockSync stockSync = new StockSync(this, this, StockSync.StockUri.bySiteUri.name());
                    stockSync.execute(siteId);
                }else {
                    authenticationCeck.refreshToken(this, REFRESH_TOKEN_MAINMATERIAL);
                }
            }else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivityMaterial.this);
                builder.setTitle(getResources().getString(R.string.message_title_internet_access));
                builder.setMessage(getResources().getString(R.string.message_no_internet));
                builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void order(Intent intent, View image, View title, View price){
        if (isMinLoli) {
            Pair<View, String> pariImage = Pair.create(image, getString(R.string.transition_image));
            Pair<View, String> pairTitle = Pair.create(title, getString(R.string.transition_title));
            Pair<View, String> pariPrice = Pair.create(price, getString(R.string.transition_price));
            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, pariImage, pairTitle);
            startActivityForResult(intent, ORDER_REQUEST, optionsCompat.toBundle());
        } else {
            startActivityForResult(intent, ORDER_REQUEST);
        }
    }

    public void order(Intent intent){
        startActivityForResult(intent, ORDER_REQUEST);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home :
                super.onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("result", "on=========================");
        if (requestCode == ORDER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Intent intent = new Intent();
                intent.putExtra("type", "orderList");

                setResult(RESULT_OK, intent);
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onExecute(int code) {
        progress.show();
    }

    @Override
    public void onSuccess(int code, Object result) {
        progress.dismiss();
        stocks = (List<Stock>)result;
        if (stocks.size() > 0){
            MainFragmentStateAdapter viewPagerAdapter = new MainFragmentStateAdapter(getSupportFragmentManager(), this, stocks);
            viewPager.setAdapter(viewPagerAdapter);
            tabLayout.setupWithViewPager(viewPager);

        }
    }

    @Override
    public void onCancel(int code, String message) {
        progress.dismiss();
    }

    @Override
    public void onError(int code, String message) {
        progress.dismiss();
    }

    public void onEventMainThread(GenericEvent.RequestSuccess requestSuccess){
        Log.d(getClass().getSimpleName(), "request success ");
        switch (requestSuccess.getProcessId()){
            case REFRESH_TOKEN_MAINMATERIAL:
                StockSync stockSync = new StockSync(this, this, StockSync.StockUri.bySiteUri.name());
                stockSync.execute(siteId);
                Log.d(getClass().getSimpleName(), "syc running");
                break;
        }
    }

}
