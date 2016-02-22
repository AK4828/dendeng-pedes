package com.hoqii.fxpc.sales.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageAppication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.adapter.SelfHistoryOrderMenuAdapter;
import com.hoqii.fxpc.sales.adapter.SellerOrderMenuAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SerialNumberDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.entity.Product;
import com.hoqii.fxpc.sales.entity.SerialNumber;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.job.MenuUpdateJob;
import com.hoqii.fxpc.sales.job.OrderStatusUpdateJob;
import com.hoqii.fxpc.sales.job.OrderUpdateJob;
import com.hoqii.fxpc.sales.job.SerialJob;
import com.hoqii.fxpc.sales.job.ShipmentJob;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
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
import com.joanzapata.iconify.widget.IconTextView;
import com.path.android.jobqueue.JobManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.meruvian.midas.core.service.TaskService;
import org.meruvian.midas.core.util.ConnectionUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;


/**
 * Created by miftakhul on 12/8/15.
 */
public class SelfHistoryOrderMenuListActivity extends AppCompatActivity implements TaskService {
    private List<OrderMenu> orderMenuList = new ArrayList<OrderMenu>();
    private SharedPreferences preferences;
    private RecyclerView recyclerView;
    private SelfHistoryOrderMenuAdapter selfHistoryOrderMenuAdapter;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String orderId;
    private CoordinatorLayout coordinatorLayout;
    private TextView omDate, omReceipt, siteName, omBusinessPartner;
    private IconTextView mailSite;
    private String orderUrl = "/api/orderHistory/";
    private int page = 1, totalPage;
    private ProgressDialog loadProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_history_order_menu_list);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new Fade());
            getWindow().setExitTransition(new Fade());
        }

        preferences = getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        orderId = getIntent().getExtras().getString("orderId");
        Log.d(getClass().getSimpleName(), "order id geted:"+orderId);

        omDate = (TextView) findViewById(R.id.om_date);
        omReceipt = (TextView) findViewById(R.id.om_receipt);
        mailSite = (IconTextView) findViewById(R.id.om_email);
        siteName = (TextView) findViewById(R.id.om_siteName);

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(new IconDrawable(this, TypiconsIcons.typcn_chevron_left).colorRes(R.color.white).actionBarSize());

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordianotorLayout);

        selfHistoryOrderMenuAdapter = new SelfHistoryOrderMenuAdapter(this, orderId);

        recyclerView = (RecyclerView) findViewById(R.id.orderMenu_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(selfHistoryOrderMenuAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiptRefress);
        swipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.yellow, R.color.blue, R.color.red);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy / hh:mm:ss");
        Date date = new Date();
        date.setTime(getIntent().getLongExtra("orderDate", 0));

        omDate.setText("Tanggal " + simpleDateFormat.format(date).toString());
        omReceipt.setText("Nomor order : " + getIntent().getExtras().getString("orderReceipt"));
        mailSite.setText("{typcn-mail} "+getIntent().getStringExtra("siteEmail"));
        siteName.setText("Order ke : " +getIntent().getStringExtra("siteName"));

        loadProgress = new ProgressDialog(this);
        loadProgress.setMessage("Fetching data...");

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                OrderMenuSync orderMenuSync = new OrderMenuSync(SelfHistoryOrderMenuListActivity.this, SelfHistoryOrderMenuListActivity.this, false);
                orderMenuSync.execute(orderId, "0");
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onExecute(int code) {
//        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void onSuccess(int code, Object result) {
        swipeRefreshLayout.setRefreshing(false);
//        selfHistoryOrderMenuAdapter = new SelfHistoryOrderMenuAdapter(this, orderMenuList);
//        recyclerView.setAdapter(selfHistoryOrderMenuAdapter);
        selfHistoryOrderMenuAdapter.addItems(orderMenuList);
    }

    @Override
    public void onCancel(int code, String message) {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onError(int code, String message) {
        swipeRefreshLayout.setRefreshing(false);
    }

    class OrderMenuSync extends AsyncTask<String, Void, JSONObject> {

        private Context context;
        private TaskService taskService;
        private boolean isLoadMore = false;

        public OrderMenuSync(Context context, TaskService taskService, boolean isLoadMore) {
            this.context = context;
            this.taskService = taskService;
            this.isLoadMore = isLoadMore;
        }


        @Override
        protected JSONObject doInBackground(String... JsonObject) {
            Log.d(getClass().getSimpleName(), "?acces_token= " + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
            Log.d(getClass().getSimpleName(), " param : " + JsonObject[0]);
            return ConnectionUtil.get(preferences.getString("server_url", "") + orderUrl + JsonObject[0] + "/menus?access_token="
                    + AuthenticationUtils.getCurrentAuthentication().getAccessToken() +"&page="+JsonObject[1]);
        }

        @Override
        protected void onCancelled() {
            taskService.onCancel(SignageVariables.HISTORY_ORDER_MENU_GET_TASK, "Batal");
        }

        @Override
        protected void onPreExecute() {
            if (!isLoadMore){
                swipeRefreshLayout.setRefreshing(true);
            }
            taskService.onExecute(SignageVariables.HISTORY_ORDER_MENU_GET_TASK);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {

                    List<OrderMenu> orderMenus = new ArrayList<OrderMenu>();

                    totalPage = result.getInt("totalPages");
                    Log.d("result order menu =====", result.toString());
                    JSONArray jsonArray = result.getJSONArray("content");
                    for (int a = 0; a < jsonArray.length(); a++) {
                        JSONObject object = jsonArray.getJSONObject(a);

                        OrderMenu orderMenu = new OrderMenu();
                        orderMenu.setId(object.getString("id"));
                        orderMenu.setQty(object.getInt("qty"));
                        orderMenu.setQtyOrder(object.getInt("qtyOrder"));
                        orderMenu.setDescription(object.getString("description"));


                        JSONObject productObject = new JSONObject();
                        if (!object.isNull("product")) {
                            productObject = object.getJSONObject("product");

                            Product product = new Product();
                            product.setId(productObject.getString("id"));
                            product.setName(productObject.getString("name"));

                            orderMenu.setProduct(product);
                        }
                        orderMenus.add(orderMenu);
                    }
                    orderMenuList = orderMenus;
                    if (isLoadMore){
                        page ++;
                        loadProgress.dismiss();
                    }

                    taskService.onSuccess(SignageVariables.HISTORY_ORDER_MENU_GET_TASK, true);
                } else {
                    taskService.onError(SignageVariables.HISTORY_ORDER_MENU_GET_TASK, "Error");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                taskService.onError(SignageVariables.HISTORY_ORDER_MENU_GET_TASK, "Error");
            }
        }
    }

    private void refreshContent() {
        //clearing data
        selfHistoryOrderMenuAdapter = new SelfHistoryOrderMenuAdapter(this, orderId);
        recyclerView.setAdapter(selfHistoryOrderMenuAdapter);

        //adding new data
        for (int x = 0; x < page; x++){
            final int finalX = x;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    OrderMenuSync orderMenuSync = new OrderMenuSync(SelfHistoryOrderMenuListActivity.this, SelfHistoryOrderMenuListActivity.this, false);
                    orderMenuSync.execute(orderId, Integer.toString(finalX));
                }
            }, 500);
        }
    }

    public void loadMoreContent(){
        if (page != totalPage){
            //showing progress
            loadProgress.show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    OrderMenuSync orderMenuSync = new OrderMenuSync(SelfHistoryOrderMenuListActivity.this, SelfHistoryOrderMenuListActivity.this, true);
                    orderMenuSync.execute(orderId, Integer.toString(page));
                }
            }, 500);
        }
    }
}
