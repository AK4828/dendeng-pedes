package com.hoqii.fxpc.sales.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.adapter.ReceiveOrderMenuAdapter;
import com.hoqii.fxpc.sales.adapter.ReturnOrderMenuAdapter;
import com.hoqii.fxpc.sales.adapter.ReturnOrderMenuDetailAdapter;
import com.hoqii.fxpc.sales.entity.OrderMenuSerial;
import com.hoqii.fxpc.sales.entity.Product;
import com.hoqii.fxpc.sales.entity.Receive;
import com.hoqii.fxpc.sales.entity.Retur;
import com.hoqii.fxpc.sales.job.ReturnOrderMenuJob;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.TypiconsIcons;
import com.path.android.jobqueue.JobManager;

import org.meruvian.midas.core.event.Event;
import org.meruvian.midas.core.service.TaskService;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by akm on 25/05/16.
 */
public class ReturnDetalListActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private ReturnOrderMenuDetailAdapter returnOrderMenuDetailAdapter;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView orderNumber, orderDate, site;
    private String returnId, returDate, siteName, siteDescription, description;
    private String returUrl = "/api/order/returns/";
    private boolean isLoli = false;
    private JobManager jobManager;
    private ProgressDialog progressDialog;
    private ProgressDialog loadProgress, progress;
    private RecyclerView recyclerView;
    private Product product;
    private Retur retur;
    private List<Product> products = new ArrayList<Product>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_list_detail);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isLoli = true;
        } else {
            isLoli = false;
        }

        preferences = getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        jobManager = SignageApplication.getInstance().getJobManager();

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(new IconDrawable(this, TypiconsIcons.typcn_chevron_left).colorRes(R.color.white).actionBarSize());

        orderNumber = (TextView) findViewById(R.id.rl_number);
        orderDate = (TextView) findViewById(R.id.rl_tgl);
        site = (TextView) findViewById(R.id.rl_site);
        recyclerView = (RecyclerView) findViewById(R.id.return_list);

        if (getIntent().getExtras() != null) {
            returnId = getIntent().getStringExtra("returId");
            siteName = getIntent().getStringExtra("site");
            description = getIntent().getStringExtra("description");
            returDate = getIntent().getStringExtra("returDate");

            site.setText("Return From : " + siteName);
            orderDate.setText(returDate);
            orderNumber.setText(description);
        }

        returnOrderMenuDetailAdapter = new ReturnOrderMenuDetailAdapter(this);
        recyclerView = (RecyclerView) findViewById(R.id.return_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(returnOrderMenuDetailAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiptRefress);
        swipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.yellow, R.color.blue, R.color.red);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });

        loadProgress = new ProgressDialog(this);
        loadProgress.setMessage(getString(R.string.message_fetch_data));
        progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.please_wait));
        progress.setCancelable(false);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                jobManager.addJobInBackground(new ReturnOrderMenuJob(returnId, false));
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEventMainThread(ReturnOrderMenuJob.ReturnOrderMenuEvent event) {
        int status = event.getStatus();

        if (status == ReturnOrderMenuJob.ReturnOrderMenuEvent.SUCCESS) {
            retur = event.getRetur();
            product = new Product();
            product.setName(retur.getOrderMenuSerial().getOrderMenu().getProduct().getName());
            product.setSerialNumber(retur.getOrderMenuSerial().getSerialNumber());

            products.add(product);
            returnOrderMenuDetailAdapter.addProducts(products);

        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startAnimateRevealColorFromCoordinate(View view, int x, int y) {
        float finalRadius = (float) Math.hypot(view.getWidth(), view.getHeight());

        Animator anim = ViewAnimationUtils.createCircularReveal(view, x, y, 0, finalRadius);
        view.setBackgroundColor(getResources().getColor(R.color.green));
        anim.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void backAnimateRevealColorFromCoordinate(final View view, int x, int y) {

        int initialRadius = view.getWidth();

        Animator anim = ViewAnimationUtils.createCircularReveal(view, x, y, initialRadius, 0);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setBackgroundColor(getResources().getColor(R.color.light_grey));
            }
        });

        anim.start();
    }

    private void refreshContent() {

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                jobManager.addJobInBackground(new ReturnOrderMenuJob(returnId, false));
            }
        });
    }
}
