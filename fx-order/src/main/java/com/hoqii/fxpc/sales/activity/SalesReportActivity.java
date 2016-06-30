package com.hoqii.fxpc.sales.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.adapter.SalesReportAdapter;
import com.hoqii.fxpc.sales.entity.SalesOrderMenu;
import com.hoqii.fxpc.sales.job.SalesReportJob;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.TypiconsIcons;
import com.path.android.jobqueue.JobManager;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class SalesReportActivity extends AppCompatActivity {

    @InjectView(R.id.main_toolbar)
    Toolbar mainToolbar;
    @InjectView(R.id.report_list)
    RecyclerView reportList;
    @InjectView(R.id.text_startDate)
    TextView textStartDate;
    @InjectView(R.id.text_endDate)
    TextView textEndDate;
    private SalesReportAdapter reportAdapter;
    private List<SalesOrderMenu> salesOrderMenus = new ArrayList<SalesOrderMenu>();
    private JobManager jobManager;
    private String productId;
    private long startDate;
    private long endDate;
    private SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
    private String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_report);
        ButterKnife.inject(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new Explode());
            getWindow().setExitTransition(new Explode());
        }

        setSupportActionBar(mainToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Sales Report");
        actionBar.setHomeAsUpIndicator(new IconDrawable(this, TypiconsIcons.typcn_chevron_left).colorRes(R.color.white).actionBarSize());

        reportAdapter = new SalesReportAdapter(this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        reportList.setLayoutManager(layoutManager);
        reportList.setItemAnimator(new DefaultItemAnimator());
        reportList.setAdapter(reportAdapter);

        productId = getIntent().getStringExtra("productId");

        jobManager = SignageApplication.getInstance().getJobManager();

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

    @OnClick(R.id.btn_startDate)
    public void onDateStart() {
        Calendar startDate = Calendar.getInstance();
        DatePickerDialog dialog = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        onStartDateSet(view, year, monthOfYear, dayOfMonth);
                    }
                },
                startDate.get(Calendar.YEAR),
                startDate.get(Calendar.MONTH),
                startDate.get(Calendar.DAY_OF_MONTH));
        dialog.setTitle("Start Date");
        dialog.show(getFragmentManager(), "DatePickerDialog");
    }

    private void onStartDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, monthOfYear);
        date.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        String dt = format.format(date.getTime());
        textStartDate.setText(dt);
        startDate = date.getTime().getTime();
        Log.d("DAT", dt);
    }

    @OnClick(R.id.btn_endDate)
    public void onDateEnd() {
        Calendar endDate = Calendar.getInstance();
        DatePickerDialog dialog = DatePickerDialog.newInstance(
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                        onEndDateSet(view, year, monthOfYear, dayOfMonth);
                    }
                },
                endDate.get(Calendar.YEAR),
                endDate.get(Calendar.MONTH),
                endDate.get(Calendar.DAY_OF_MONTH));
        dialog.setTitle("End Date");
        dialog.show(getFragmentManager(), "DatePickerDialog");
    }

    private void onEndDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
        Calendar date = Calendar.getInstance();
        date.set(Calendar.YEAR, year);
        date.set(Calendar.MONTH, monthOfYear);
        date.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        String dt = format.format(date.getTime());
        textEndDate.setText(dt);
        endDate = date.getTime().getTime();
    }

    @OnClick(R.id.btn_search)
    public void goReport() {
        jobManager.addJobInBackground(new SalesReportJob(productId, startDate, endDate));
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

    public void onEventMainThread(SalesReportJob.SalesReportEvent event) {
        int status = event.getStatus();

        if (status == SalesReportJob.SalesReportEvent.SUCCESS) {
            salesOrderMenus = event.getSalesOrderMenus();

            reportAdapter.addItems(salesOrderMenus);

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
}
