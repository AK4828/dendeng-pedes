package com.hoqii.fxpc.sales.activity;

import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityOptionsCompat;
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
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.adapter.SelfHistoryOrderAdapter;
import com.hoqii.fxpc.sales.adapter.SiteAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.OrderDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.OrderMenuDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SiteDatabaseAdapter;
import com.hoqii.fxpc.sales.core.commons.Site;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.TypiconsIcons;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 12/8/15.
 */
public class SiteListActivity extends AppCompatActivity {
    private List<Site> siteList = new ArrayList<Site>();
    private SharedPreferences preferences;
    private RecyclerView recyclerView;
    private SiteDatabaseAdapter siteDatabaseAdapter;
    private SiteAdapter siteAdapter;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout dataNull;
    private static final int ORDER_REQUEST_OPTIONS = 301;
    private String orderId = null;
    private OrderDatabaseAdapter orderDbAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_list);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new Fade());
        }

        preferences = getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        siteDatabaseAdapter = new SiteDatabaseAdapter(this);
        orderDbAdapter = new OrderDatabaseAdapter(this);

        toolbar = (Toolbar)findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(new IconDrawable(this, TypiconsIcons.typcn_chevron_left).colorRes(R.color.white).actionBarSize());

        recyclerView = (RecyclerView) findViewById(R.id.site_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        List<Site> tempSite = siteDatabaseAdapter.getSite();

        for (Site s : tempSite){
            if (s.getType().equalsIgnoreCase("MASTER") || s.getId().equalsIgnoreCase(AuthenticationUtils.getCurrentAuthentication().getSite().getId())){
                Log.d(getClass().getSimpleName(), "found type " + s.getType() + " name " + s.getName());
            }else {
                siteList.add(s);
            }
        }

        siteAdapter = new SiteAdapter(this, siteList);
        recyclerView.setAdapter(siteAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiptRefress);
        swipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.yellow, R.color.blue, R.color.red);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });

        dataNull = (LinearLayout) findViewById(R.id.dataNull);

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

                List<Site> temp = siteDatabaseAdapter.getSiteByNameorDescription(query);
                siteList.clear();
                for (Site s : temp){
                    if (s.getType().equalsIgnoreCase("MASTER") || s.getId().equalsIgnoreCase(AuthenticationUtils.getCurrentAuthentication().getSite().getId())){
                        Log.d(getClass().getSimpleName(), "found type " + s.getType() + " name " + s.getName());
                    }else {
                        siteList.add(s);
                    }
                }
                siteAdapter = new SiteAdapter(SiteListActivity.this, siteList);
                recyclerView.setAdapter(siteAdapter);

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
                refreshContent();
                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home :
                super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshContent(){
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                siteList.clear();

                List<Site> tempSite = siteDatabaseAdapter.getSite();
                for (Site s : tempSite){
                    if (s.getType().equalsIgnoreCase("MASTER") || s.getId().equalsIgnoreCase(AuthenticationUtils.getCurrentAuthentication().getSite().getId())){
                        Log.d(getClass().getSimpleName(), "found type " + s.getType() + " name " + s.getName());
                    }else {
                        siteList.add(s);
                    }
                }

                siteAdapter = new SiteAdapter(SiteListActivity.this, siteList);
                recyclerView.setAdapter(siteAdapter);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    public void finalChoise(){
        setResult(RESULT_OK);
        orderId = orderDbAdapter.getOrderId();
        Order order = orderDbAdapter.findOrderById(orderId);
        if (order.getSite().getId() != null) {
            Intent i = new Intent(this, MainActivityMaterial.class);
            i.putExtra("siteId", order.getSite().getId());
            startActivityForResult(i, ORDER_REQUEST_OPTIONS, ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle());
        }
    }
}
