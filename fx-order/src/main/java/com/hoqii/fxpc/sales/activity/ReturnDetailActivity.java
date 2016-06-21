package com.hoqii.fxpc.sales.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.adapter.ReturnOrderMenuAdapter;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.entity.OrderMenuSerial;
import com.hoqii.fxpc.sales.entity.Product;
import com.hoqii.fxpc.sales.entity.Receive;
import com.hoqii.fxpc.sales.entity.Retur;
import com.hoqii.fxpc.sales.entity.Shipment;
import com.hoqii.fxpc.sales.job.ReturnJob;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.TypiconsIcons;
import com.path.android.jobqueue.JobManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.meruvian.midas.core.service.TaskService;
import org.meruvian.midas.core.util.ConnectionUtil;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by akm on 15/04/16.
 */
public class ReturnDetailActivity extends AppCompatActivity implements TaskService {
    private int requestScannerCode = 124;

    private List<OrderMenuSerial> orderMenuSerialList = new ArrayList<OrderMenuSerial>();
    private SharedPreferences preferences;
    private RecyclerView recyclerView;
    private ReturnOrderMenuAdapter returnOrderMenuAdapter;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView receiveDate, orderNumber, orderDate, site;
    private String shipmentId, orderId;
    private ProgressDialog loadProgress;
    private int page = 1, totalPage;
    private String receiveUrl = "/api/order/receives/";
    private boolean isLoli = false;
    private Receive receive;
    private JobManager jobManager;
    private ProgressDialog progressDialog;
    private boolean statusDelivery = false;
    private Retur retur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_detail);

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
            shipmentId = getIntent().getStringExtra("shipmentId");

            orderNumber.setText(getIntent().getStringExtra("orderReceipt"));

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy / hh:mm:ss");
            Date date = new Date();
            date.setTime(getIntent().getLongExtra("receiveDate", 0));

            Date oDate = new Date();
            oDate.setTime(getIntent().getLongExtra("orderDate", 0));
            orderId = getIntent().getStringExtra("orderId");
            site.setText(getString(R.string.text_receive_from) + getIntent().getStringExtra("siteDescription"));
            receiveDate.setText(simpleDateFormat.format(date));
            orderDate.setText(simpleDateFormat.format(oDate));

            ObjectMapper mapper = SignageApplication.getObjectMapper();

            String jsonReceive = getIntent().getStringExtra("jsonReceive");
            try {
                receive = mapper.readValue(jsonReceive, Receive.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (receive.getShipment().getStatus() == Shipment.ShipmentStatus.DELIVERED) {
                statusDelivery = true;
            }
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.message_confirm_sipment));

        if (statusDelivery == true) {
            returnOrderMenuAdapter = new ReturnOrderMenuAdapter(this, orderId, true);
        } else {
            returnOrderMenuAdapter = new ReturnOrderMenuAdapter(this, orderId);
        }

        recyclerView = (RecyclerView) findViewById(R.id.receive_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(returnOrderMenuAdapter);

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

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                SerialOrderMenuSync serialOrderMenuSync = new SerialOrderMenuSync(ReturnDetailActivity.this, ReturnDetailActivity.this, false);
                serialOrderMenuSync.execute(shipmentId);
            }
        });
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

    @Override
    public void onExecute(int code) {

    }

    @Override
    public void onSuccess(int code, Object result) {
        if (code == SignageVariables.SERIAL_ORDER_MENU_GET_TASK) {
            swipeRefreshLayout.setRefreshing(false);
            if (statusDelivery == true) {
                returnOrderMenuAdapter = new ReturnOrderMenuAdapter(this, orderId, true);
            } else {
                returnOrderMenuAdapter = new ReturnOrderMenuAdapter(this, orderId);
            }
            recyclerView.setAdapter(returnOrderMenuAdapter);
            returnOrderMenuAdapter.addItems(orderMenuSerialList, getIntent().getStringExtra("siteToId"));

        }
    }

    @Override
    public void onCancel(int code, String message) {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onError(int code, String message) {

    }

    private void refreshContent() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                SerialOrderMenuSync serialOrderMenuSync = new SerialOrderMenuSync(ReturnDetailActivity.this, ReturnDetailActivity.this, false);
                serialOrderMenuSync.execute(shipmentId);
            }
        });
    }

    public void onEventMainThread(ReturnJob.ReturnEvent event) {
        int status = event.getStatus();
        if (status == ReturnJob.ReturnEvent.SUCCESS) {
            Toast.makeText(this, "Returned", Toast.LENGTH_LONG).show();
        }
    }

    private void AlertMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.message_title_warning));
        builder.setMessage(message);
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
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


    @Override
    public void onBackPressed() {
        finishDetail();
    }

    private void finishDetail() {
        if (statusDelivery == true) {
            Intent i = new Intent();
            i.putExtra("receiveId", receive.getId());
            setResult(RESULT_OK, i);
        } else {
            setResult(RESULT_OK);
        }
        finish();
    }

    class SerialOrderMenuSync extends AsyncTask<String, Void, JSONObject> {

        private Context context;
        private TaskService taskService;
        private boolean isLoadMore = false;

        public SerialOrderMenuSync(Context context, TaskService taskService, boolean isLoadMore) {
            this.context = context;
            this.taskService = taskService;
            this.isLoadMore = isLoadMore;
        }


        @Override
        protected JSONObject doInBackground(String... JsonObject) {
            Log.d(getClass().getSimpleName(), "?acces_token= " + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
            Log.d(getClass().getSimpleName(), " param : " + JsonObject[0]);
            return ConnectionUtil.get(preferences.getString("server_url", "") + receiveUrl + JsonObject[0] + "/menus?access_token="
                    + AuthenticationUtils.getCurrentAuthentication().getAccessToken() + "&max=" + Integer.MAX_VALUE);
        }

        @Override
        protected void onCancelled() {
            taskService.onCancel(SignageVariables.SERIAL_ORDER_MENU_GET_TASK, "Batal");
        }

        @Override
        protected void onPreExecute() {
            if (isLoadMore == false) {
                swipeRefreshLayout.setRefreshing(true);
            }
            taskService.onExecute(SignageVariables.SERIAL_ORDER_MENU_GET_TASK);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {
                    List<OrderMenuSerial> orderMenuSerials = new ArrayList<OrderMenuSerial>();
                    JSONArray jsonArray = result.getJSONArray("content");

                    totalPage = result.getInt("totalPages");
                    Log.d(getClass().getSimpleName(), "serial menu : " + result.toString());
                    for (int a = 0; a < jsonArray.length(); a++) {
                        JSONObject object = jsonArray.getJSONObject(a);
                        OrderMenuSerial orderMenuSerial = new OrderMenuSerial();
                        orderMenuSerial.setId(object.getString("id"));
                        orderMenuSerial.setSerialNumber(object.getString("serialNumber"));

                        JSONObject orderMenuObject = new JSONObject();
                        if (!object.isNull("orderMenu")) {
                            orderMenuObject = object.getJSONObject("orderMenu");

                            OrderMenu orderMenu = new OrderMenu();
                            orderMenu.setId(orderMenuObject.getString("id"));
                            orderMenu.setQty(orderMenuObject.getInt("qty"));
                            orderMenu.setQtyOrder(orderMenuObject.getInt("qtyOrder"));
                            orderMenu.setDescription(orderMenuObject.getString("description"));

                            JSONObject productObject = new JSONObject();
                            if (!orderMenuObject.isNull("product")) {
                                productObject = orderMenuObject.getJSONObject("product");

                                Product product = new Product();
                                product.setId(productObject.getString("id"));
                                product.setName(productObject.getString("name"));
                                orderMenu.setProduct(product);
                            }
                            orderMenuSerial.setOrderMenu(orderMenu);
                        }
                        orderMenuSerials.add(orderMenuSerial);
                    }
                    orderMenuSerialList = orderMenuSerials;

                    if (isLoadMore == true) {
                        page++;
                        loadProgress.dismiss();
                    }
                    taskService.onSuccess(SignageVariables.SERIAL_ORDER_MENU_GET_TASK, true);
                } else {
                    taskService.onError(SignageVariables.SERIAL_ORDER_MENU_GET_TASK, "Error");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                taskService.onError(SignageVariables.SERIAL_ORDER_MENU_GET_TASK, "Error");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == requestScannerCode) {
            returnOrderMenuAdapter = new ReturnOrderMenuAdapter(this, orderId);
            recyclerView.setAdapter(returnOrderMenuAdapter);
            returnOrderMenuAdapter.addItems(orderMenuSerialList, getIntent().getStringExtra("siteToId"));
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
}
