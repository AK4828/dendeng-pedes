package com.hoqii.fxpc.sales.activity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.adapter.ReturnOrderMenuAdapter;
import com.hoqii.fxpc.sales.entity.OrderMenuSerial;
import com.hoqii.fxpc.sales.entity.Receive;
import com.hoqii.fxpc.sales.entity.Retur;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.TypiconsIcons;
import com.path.android.jobqueue.JobManager;

import org.meruvian.midas.core.service.TaskService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by akm on 25/05/16.
 */
public class ReturnDetalListActivity extends AppCompatActivity implements TaskService {

    private List<OrderMenuSerial> orderMenuSerialList = new ArrayList<OrderMenuSerial>();
    private SharedPreferences preferences;
    private RecyclerView recyclerView;
    private ReturnOrderMenuAdapter returnOrderMenuAdapter;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView receiveDate, orderNumber, orderDate, site;
    private String returnId, returDate, siteName, siteDescription, description;
    private ProgressDialog loadProgress;
    private int page = 1, totalPage;
    private String returUrl = "/api/order/returns/";
    private boolean isLoli = false;
    private Receive receive;
    private JobManager jobManager;
    private ProgressDialog progressDialog;
    private boolean statusDelivery = false;
    private Retur retur;

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

        receiveDate = (TextView) findViewById(R.id.rl_tgl_receive);
        orderNumber = (TextView) findViewById(R.id.rl_number);
        orderDate = (TextView) findViewById(R.id.rl_tgl);
        site = (TextView) findViewById(R.id.rl_site);

        if (getIntent().getExtras() != null) {
            returnId = getIntent().getStringExtra("returnId");
            returDate = getIntent().getStringExtra("returDate");
            siteName = getIntent().getStringExtra("site");
            siteDescription = getIntent().getStringExtra("siteDescription");
            description = getIntent().getStringExtra("description");

            receiveDate.setText(returDate);

        }
    }

    @Override
    public void onExecute(int code) {

    }

    @Override
    public void onSuccess(int code, Object result) {

    }

    @Override
    public void onCancel(int code, String message) {

    }

    @Override
    public void onError(int code, String message) {

    }
}
