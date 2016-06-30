package com.hoqii.fxpc.sales.activity;

import android.app.SearchManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.adapter.SKUAdapter;
import com.hoqii.fxpc.sales.adapter.StockAdapter;
import com.hoqii.fxpc.sales.entity.Stock;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.task.StockSync;
import com.hoqii.fxpc.sales.util.AuthenticationCeck;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.TypiconsIcons;

import org.meruvian.midas.core.service.TaskService;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.greenrobot.event.EventBus;

public class SKUListActivity extends AppCompatActivity implements TaskService {

    @InjectView(R.id.main_toolbar)
    Toolbar mainToolbar;
    @InjectView(R.id.stock_list)
    RecyclerView stockList;
    @InjectView(R.id.swiptRefress)
    SwipeRefreshLayout swiptRefress;
    @InjectView(R.id.dataNull)
    LinearLayout dataNull;
    @InjectView(R.id.dataFailed)
    LinearLayout dataFailed;
    private SharedPreferences preferences;
    private SKUAdapter skuAdapter;
    private AuthenticationCeck authenticationCeck = new AuthenticationCeck();
    private static final int REFRESH_TOKEN_SKU = 322;
    private List<Stock> stocks = new ArrayList<Stock>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sku_list);
        ButterKnife.inject(this);
        EventBus.getDefault().register(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new Explode());
            getWindow().setExitTransition(new Explode());
        }
        preferences = getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        setSupportActionBar(mainToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.nav_sku_list);
        actionBar.setHomeAsUpIndicator(new IconDrawable(this, TypiconsIcons.typcn_chevron_left).colorRes(R.color.white).actionBarSize());

        skuAdapter = new SKUAdapter(this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        stockList.setLayoutManager(layoutManager);
        stockList.setItemAnimator(new DefaultItemAnimator());
        stockList.setAdapter(skuAdapter);

        swiptRefress.setColorSchemeResources(R.color.green, R.color.yellow, R.color.blue, R.color.red);
        swiptRefress.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (authenticationCeck.isAccess()) {
                    StockSync sync = new StockSync(SKUListActivity.this, SKUListActivity.this, StockSync.StockUri.defaultUri.name());
                    sync.execute();
                    Log.d(getClass().getSimpleName(), "[ acces true / refreshing token not needed]");
                } else {
                    Log.d(getClass().getSimpleName(), "[ acces false / refreshing token]");
                    authenticationCeck.refreshToken(SKUListActivity.this, REFRESH_TOKEN_SKU);
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search, menu);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                List<Stock> tempList = new ArrayList<Stock>();
                for (Stock s : stocks) {
                    if (s.getProduct().getName().trim().toString().toLowerCase().contains(query.trim().toLowerCase())) {
                        tempList.add(s);
                    }
                    skuAdapter = new SKUAdapter(SKUListActivity.this);
                    skuAdapter.addItems(tempList);
                    stockList.setAdapter(skuAdapter);
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                skuAdapter = new SKUAdapter(SKUListActivity.this);
                skuAdapter.addItems(stocks);
                stockList.setAdapter(skuAdapter);
                return true;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                refreshContent();
                return false;
            }
        });
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

    private void refreshContent() {
        skuAdapter = new SKUAdapter(this);
        stockList.setAdapter(skuAdapter);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                StockSync sync = new StockSync(SKUListActivity.this, SKUListActivity.this, StockSync.StockUri.defaultUri.name());
                sync.execute();
            }
        });
    }

    @Override
    public void onExecute(int cOverrideode) {

    }

    @Override
    public void onSuccess(int code, Object result) {
        swiptRefress.setRefreshing(false);
        stocks = (List<Stock>) result;

        Log.d(getClass().getSimpleName(), "shipment list size " + stocks.size());
        skuAdapter.addItems(stocks);
        dataFailed.setVisibility(View.GONE);
        if (stocks.size() > 0) {
            dataNull.setVisibility(View.GONE);
        } else {
            dataNull.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCancel(int code, String message) {
        swiptRefress.setRefreshing(false);
    }

    @Override
    public void onError(int code, String message) {

    }

    public void onEventMainThread(GenericEvent.RequestSuccess requestSuccess) {
        Log.d(getClass().getSimpleName(), "RequestSuccess: " + requestSuccess.getProcessId());
        switch (requestSuccess.getProcessId()) {
            case REFRESH_TOKEN_SKU:
                StockSync sync = new StockSync(SKUListActivity.this, SKUListActivity.this, StockSync.StockUri.defaultUri.name());
                sync.execute();
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
