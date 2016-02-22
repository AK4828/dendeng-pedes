package com.hoqii.fxpc.sales.activity;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.adapter.SiteAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SiteDatabaseAdapter;
import com.hoqii.fxpc.sales.core.commons.Site;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_site_list);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new Explode());
            getWindow().setExitTransition(new Explode());
        }

        preferences = getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        siteDatabaseAdapter = new SiteDatabaseAdapter(this);

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home :
                super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshContent(){
        new Handler().postDelayed(new Runnable() {
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
        }, 2000);

    }

    public void finalChoise(){
        setResult(RESULT_OK);
        finish();
    }


}
