package com.hoqii.fxpc.sales.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
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
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageAppication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.adapter.MainSlidePagerAdapter;
import com.hoqii.fxpc.sales.adapter.OrderMenuAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.OrderDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.OrderMenuDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SiteDatabaseAdapter;
import com.hoqii.fxpc.sales.core.commons.Site;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.event.LoginEvent;
import com.hoqii.fxpc.sales.fragment.ReceiveListFragment;
import com.hoqii.fxpc.sales.fragment.SellerOrderListFragment;
import com.hoqii.fxpc.sales.job.OrderMenuJob;
import com.hoqii.fxpc.sales.job.OrderUpdateJob;
import com.hoqii.fxpc.sales.job.RefreshTokenJob;
import com.hoqii.fxpc.sales.task.RequestOrderSyncTask;
import com.hoqii.fxpc.sales.util.AuthenticationCeck;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.TypiconsIcons;
import com.path.android.jobqueue.JobManager;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.meruvian.midas.core.service.TaskService;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by miftakhul on 11/13/15.
 */
public class MainActivity extends AppCompatActivity implements TaskService {
    private DrawerLayout drawerLayout;
    private boolean isMinLoli = false;
    private static final int ORDER_REQUEST = 300;
    private static final int ORDER_REQUEST_OPTIONS = 301;
    private static final int BUSINESS_REQUEST = 302;
    private static final int PREFERENCE_REQUEST = 303;
    private static final int SITE_REQUEST = 304;
    private AuthenticationCeck authenticationCeck = new AuthenticationCeck();
    private JobManager jobManager;
    private SharedPreferences preferences;
    private ProgressDialog dialog, dialogRefresh;
    private TextView textTotalItem, textTotalOrder, textOrderto;
    private OrderDatabaseAdapter orderDbAdapter;
    private OrderMenuDatabaseAdapter orderMenuDbAdapter;
    private SiteDatabaseAdapter siteDatabaseAdapter;
    private RequestOrderSyncTask requestOrderSyncTask;
    private List<OrderMenu> orderMenus = new ArrayList<OrderMenu>();
    private List<String> orderMenuIdes;
    private int orderMenuCount = 0;
    private int totalOrderMenus = 0;
    private String orderId = null;
    private DecimalFormat decimalFormat = new DecimalFormat("#,###");
    private RecyclerView orderListRecycle;
    private OrderMenuAdapter orderMenuAdapter;
    private ViewPager slideViewPager;
    private TabLayout slideTablayout;
    private boolean menuItemVisibility = true;
    private SlidingUpPanelLayout slidingUpPanel;
    private SellerOrderListFragment sellerOrderListFragment;
    private ReceiveListFragment receiveListFragment;
    private LinearLayout dataNull;
    private View appShadow;

    private boolean orderMenuError = false;
    private refreshTokenStatus refreshStatus = null;

    public enum refreshTokenStatus {
        submitOrder, orderFragment, receiveFragment
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        jobManager = SignageAppication.getInstance().getJobManager();
        preferences = getSharedPreferences(SignageVariables.PREFS_SERVER, 0);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        textTotalItem = (TextView) findViewById(R.id.text_total_item);
        textTotalOrder = (TextView) findViewById(R.id.text_total_order);
        textOrderto = (TextView) findViewById(R.id.text_orderto);
        orderListRecycle = (RecyclerView) findViewById(R.id.list_order);
        slideViewPager = (ViewPager) findViewById(R.id.slideViewPager);
        slideTablayout = (TabLayout) findViewById(R.id.slideTab);
        slidingUpPanel = (SlidingUpPanelLayout) findViewById(R.id.slidingUp);
        dataNull = (LinearLayout) findViewById(R.id.dataOrderNull);
        appShadow = (View) findViewById(R.id.app_shadow);

        orderMenuDbAdapter = new OrderMenuDatabaseAdapter(this);
        siteDatabaseAdapter = new SiteDatabaseAdapter(this);
        orderDbAdapter = new OrderDatabaseAdapter(this);
        orderMenuAdapter = new OrderMenuAdapter(this);

        orderId = orderDbAdapter.getOrderId();
        Log.d(getClass().getSimpleName(), "[ order id app run " + orderId + " ]");

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        orderListRecycle.setLayoutManager(layoutManager);
        orderListRecycle.setItemAnimator(new DefaultItemAnimator());
        orderListRecycle.setAdapter(orderMenuAdapter);

        MainSlidePagerAdapter mainSlidePagerAdapter = new MainSlidePagerAdapter(getSupportFragmentManager());
        slideViewPager.setAdapter(mainSlidePagerAdapter);
        slideTablayout.setupWithViewPager(slideViewPager);

        sellerOrderListFragment = (SellerOrderListFragment) mainSlidePagerAdapter.getItem(0);
        receiveListFragment = (ReceiveListFragment) mainSlidePagerAdapter.getItem(1);

        slidingUpPanel.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelCollapsed(View panel) {
                setMenuItemVisibility(true);
            }

            @Override
            public void onPanelExpanded(View panel) {
                setMenuItemVisibility(false);
            }

            @Override
            public void onPanelAnchored(View panel) {
            }

            @Override
            public void onPanelHidden(View panel) {
            }
        });

        dialog = new ProgressDialog(this);
        dialog.setMessage("Send order ...");
        dialog.setCancelable(false);

        dialogRefresh = new ProgressDialog(this);
        dialogRefresh.setMessage("Pleace wait ...");
        dialogRefresh.setCancelable(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isMinLoli = true;
            appShadow.setVisibility(View.GONE);
        } else {
            isMinLoli = false;
            appShadow.setVisibility(View.VISIBLE);
        }

        setNav();
    }


    @Override
    protected void onStart() {
        super.onStart();
        updateInfo();
        EventBus.getDefault().register(this);
        Log.d(getClass().getSimpleName(), "[ on start ]");
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void forceUnRegisterWhenExist() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.order_list, menu);

        MenuItem addOrder = menu.findItem(R.id.menu_add_order);
        MenuItem addDestionation = menu.findItem(R.id.menu_add_site);
        MenuItem pay = menu.findItem(R.id.menu_pay_order);

        addOrder.setIcon(new IconDrawable(this, TypiconsIcons.typcn_pen).colorRes(R.color.white).actionBarSize());
        addDestionation.setIcon(new IconDrawable(this, TypiconsIcons.typcn_business_card).colorRes(R.color.white).actionBarSize());

        if (!menuItemVisibility) {
            addOrder.setVisible(false);
            addDestionation.setVisible(false);
            pay.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_pay_order:
                if (orderId != null) {
                    Order order = orderDbAdapter.findOrderById(orderId);
                    if (orderMenuDbAdapter.findOrderMenuByOrderId(orderId).size() == 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Peringatan");
                        builder.setMessage("Anda belum memilih barang");
                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.show();
                    } else if (order.getSite().getId() == null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Peringatan");
                        builder.setMessage("Anda belum memilih tujuan order");
                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.show();
                    } else {
                        if (authenticationCeck.isNetworkAvailable()){
                            if (authenticationCeck.isAccess()) {
                                Log.d(getClass().getSimpleName(), "[ application access granted ]");
                                Log.d(getClass().getSimpleName(), "[ submiting order run ]");
                                saveOrder();
                            } else {
                                Log.d(getClass().getSimpleName(), "[ application access need to refresh ]");
                                Log.d(getClass().getSimpleName(), "[ refreshing refersh param submit ]");
                                refreshToken(refreshTokenStatus.submitOrder.name());
                            }
                        }else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Internet access");
                            builder.setMessage("No internet connection");
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            builder.show();
                        }

                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Peringatan");
                    builder.setMessage("Anda belum memilih barang");
                    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.show();
                }
                return true;

            case R.id.menu_add_site:
                if (isMinLoli) {
                    startActivityForResult(new Intent(this, SiteListActivity.class), SITE_REQUEST, ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                } else {
                    startActivityForResult(new Intent(this, SiteListActivity.class), SITE_REQUEST);
                }
                return true;

            case R.id.menu_add_order:
                orderOption();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setNav() {
        TextView parentName = (TextView) findViewById(R.id.siteName);
        parentName.setText(AuthenticationUtils.getCurrentAuthentication().getSite().getDescription());
        NavigationView navi = (NavigationView) findViewById(R.id.nav_view);
        navi.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                drawerLayout.closeDrawers();

                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                        updateInfo();
                        break;

                    case R.id.nav_order_purches_history:
                        forceUnRegisterWhenExist();
                        Intent historyIntent = new Intent(MainActivity.this, SelfHistoryOrderListActivity.class);
                        if (isMinLoli) {
                            startActivity(historyIntent, ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                        } else {
                            startActivity(historyIntent);
                        }
                        break;

                    case R.id.nav_receiving:
                        forceUnRegisterWhenExist();
                        Intent receiveIntent = new Intent(MainActivity.this, ReceiveListActivity.class);
                        if (isMinLoli) {
                            startActivity(receiveIntent, ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                        } else {
                            startActivity(receiveIntent);
                        }
                        break;

                    case R.id.seller_purchase_order_list:
                        forceUnRegisterWhenExist();
                        Intent purchaseOrderIntent = new Intent(MainActivity.this, SellerOrderListActivity.class);
                        if (isMinLoli) {
                            startActivity(purchaseOrderIntent, ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                        } else {
                            startActivity(purchaseOrderIntent);
                        }
                        break;

                    case R.id.seller_shipment_history_list:
                        forceUnRegisterWhenExist();
                        Intent shipmentHistoryIntent = new Intent(MainActivity.this, ShipmentHistoryListActivity.class);
                        if (isMinLoli) {
                            startActivity(shipmentHistoryIntent, ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                        } else {
                            startActivity(shipmentHistoryIntent);
                        }
                        break;

                    case R.id.nav_points:
                        forceUnRegisterWhenExist();
                        Intent point = new Intent(MainActivity.this, PointActivity.class);
                        if (isMinLoli) {
                            startActivity(point, ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                        } else {
                            startActivity(point);
                        }
                        break;

                    case R.id.preference:
                        Intent pref = new Intent(MainActivity.this, PreferenceActivity.class);
                        if (isMinLoli) {
                            startActivityForResult(pref, PREFERENCE_REQUEST, ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                        } else {
                            startActivityForResult(pref, PREFERENCE_REQUEST);
                        }
                        break;

                }
                return false;
            }
        });
    }

    public void updateInfo() {
        orderId = orderDbAdapter.getOrderId();
        Log.d(getClass().getSimpleName(), "[ order id app update info " + orderId + " ]");

        if (orderId != null) {
            long totalPrice = 0;
            int totalItem = 0;

            Log.d(getClass().getSimpleName(), "Order Id = " + orderId);
            orderMenus = orderMenuDbAdapter.findOrderMenuByOrderId(orderId);
            Order order = orderDbAdapter.findOrderById(orderId);

            for (OrderMenu om : orderMenus) {
                totalPrice += om.getProduct().getSellPrice() * om.getQty();
                totalItem += om.getQty();
            }

            if (order.getSite().getId() != null) {
                Site site = siteDatabaseAdapter.findSiteById(order.getSite().getId());
                textOrderto.setText("Order destination : " + site.getName());
            } else {
                textOrderto.setText("Order destination : ");
            }

            orderMenus = orderMenuDbAdapter.findOrderMenuByOrderId(orderId);
            if (orderMenus.size() > 0){
                dataNull.setVisibility(View.GONE);
            }else {
                dataNull.setVisibility(View.VISIBLE);
            }
            orderMenuAdapter.addItems(orderMenus);

            textTotalItem.setText("Items : " + totalItem);
            textTotalOrder.setText("Price : " + "Rp " + decimalFormat.format(totalPrice));
        } else {
            textOrderto.setText("Order destination : ");
            textTotalItem.setText("Items : ");
            textTotalOrder.setText("Price :");
            dataNull.setVisibility(View.VISIBLE);
        }

    }

    private void saveOrder() {
        orderId = orderDbAdapter.getOrderId();
        String receiptNumber = orderDbAdapter.getReceiptNumberByOrderId(orderId);
        if (receiptNumber.isEmpty() || receiptNumber.equalsIgnoreCase(null) || receiptNumber.equalsIgnoreCase("")) {
            requestOrderSyncTask = new RequestOrderSyncTask(this,
                    this, orderId);
            requestOrderSyncTask.execute();
            Log.d(getClass().getSimpleName(), "[ receipt number from order id " + orderId + " [is null] ]");
        } else {
            Log.d(getClass().getSimpleName(), "[ receipt number from order id " + orderId + " [is not null / receiptNumber" + receiptNumber + " ] ]");
            Order order = orderDbAdapter.findOrderById(orderId);
            Log.d(getClass().getSimpleName(), "Order ID : " + orderId
                    + " Update Parameter : >> RefId " + order.getRefId()
                    + " \n>> EntittyOrderId : " + order.getId() + " | "
                    + preferences.getString("server_url", ""));

            jobManager.addJobInBackground(new OrderUpdateJob(order.getRefId(), order.getId(),
                    preferences.getString("server_url", "")));
        }

        dialog.show();
    }

    private void setMenuItemVisibility(boolean visibility) {
        this.menuItemVisibility = visibility;
        invalidateOptionsMenu();
    }

    public void order(Intent intent, View image, View title, View price) {
        if (isMinLoli) {
//            String transitionName = getString(R.string.transition_string);

            Pair<View, String> pariImage = Pair.create(image, getString(R.string.transition_image));
            Pair<View, String> pairTitle = Pair.create(title, getString(R.string.transition_title));
            Pair<View, String> pariPrice = Pair.create(price, getString(R.string.transition_price));

            ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this, pariImage, pairTitle);

            startActivityForResult(intent, ORDER_REQUEST, optionsCompat.toBundle());
        } else {
            startActivityForResult(intent, ORDER_REQUEST);
        }
    }

    public void orderUpdate(String productId, int qty) {
        Intent i = new Intent(this, OrderActivity.class);
        i.putExtra("productId", productId);
        i.putExtra("qtyUpdate", qty);
        startActivityForResult(i, ORDER_REQUEST);
    }

    public void orderOption() {
        Intent i = new Intent(this, MainActivityMaterial.class);
        startActivityForResult(i, ORDER_REQUEST_OPTIONS, ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle());
    }

    public void refreshToken(String refresh) {
        refreshStatus = refreshTokenStatus.valueOf(refresh);
        jobManager.addJobInBackground(new RefreshTokenJob());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("result", "on=========================");

        if (requestCode == ORDER_REQUEST) {
            if (resultCode == RESULT_OK) {
                updateInfo();
//                orderMenus = orderMenuDbAdapter.findOrderMenuByOrderId(orderId);
//                orderMenuAdapter.addItems(orderMenus);

                Log.d("result", "ok =========================");
            }
        } else if (requestCode == ORDER_REQUEST_OPTIONS) {
            if (resultCode == RESULT_OK) {
                Log.d("result", "ok orderOptions =========================");

                if (data != null) {
                    String type = data.getExtras().getString("type", null);
                    Log.d("result type", type);
                    if (type.equalsIgnoreCase("orderList")) {
                        updateInfo();
//                        orderMenus = orderMenuDbAdapter.findOrderMenuByOrderId(orderId);
//                        orderMenuAdapter.addItems(orderMenus);

                    }
                }
            }
        } else if (requestCode == SITE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Log.d(getClass().getSimpleName(), "site result");
                updateInfo();
            }
        } else if (requestCode == PREFERENCE_REQUEST) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onExecute(int code) {
    }

    @Override
    public void onSuccess(int code, Object result) {
        Log.d(getClass().getSimpleName(), result + " succecc code " + code);

        if (result != null) {
            if (code == SignageVariables.REQUEST_ORDER) {
                Log.d(getClass().getSimpleName(), result + ">> RequestOrderSyncTask * Success");
                Order order = orderDbAdapter.findOrderById(orderId);

                Log.d(getClass().getSimpleName(), "Order ID : " + orderId
                        + " Update Parameter : >> RefId " + order.getRefId()
                        + " \n>> EntittyOrderId : " + order.getId() + " | "
                        + preferences.getString("server_url", ""));

                jobManager.addJobInBackground(new OrderUpdateJob(order.getRefId(), order.getId(),
                        preferences.getString("server_url", "")));
            }
        }
    }

    @Override
    public void onCancel(int code, String message) {
        Log.d(getClass().getSimpleName(), message);
    }

    @Override
    public void onError(int code, String message) {
        dialog.dismiss();
        retryRequestSync();
        Log.e(getClass().getSimpleName(), message);

    }

    public void onEventMainThread(GenericEvent.RequestInProgress requestInProgress) {
        Log.d(getClass().getSimpleName(), "RequestInProgress: " + requestInProgress.getProcessId());
        switch (requestInProgress.getProcessId()) {
            case RefreshTokenJob.PROCESS_ID:
                dialogRefresh.show();
                break;
        }
    }

    public void onEventMainThread(GenericEvent.RequestSuccess requestSuccess) {
        try {
            switch (requestSuccess.getProcessId()) {
                case OrderUpdateJob.PROCESS_ID:
                    Log.d(getClass().getSimpleName(), "RequestSuccess OrderUpdateJob: >> RefId : "
                            + requestSuccess.getRefId() + "\n >> Entity id: " + requestSuccess.getEntityId());

                    orderMenuIdes = orderMenuDbAdapter.findOrderMenuIdesByOrderIdActive(requestSuccess.getEntityId());
                    totalOrderMenus = orderMenuIdes.size();

                    Log.d(getClass().getSimpleName(), "Order Menu Ides Size : " + orderMenuIdes.size());

                    for (String id : orderMenuIdes) {
                        Log.d(getClass().getSimpleName(), "ORDER MENU ID : " + id);
                        Log.d(getClass().getSimpleName(), "ORDER REF ID : " + requestSuccess.getRefId());
                        Log.d("ORDER UPDATE", "================================================================ ");
                        jobManager.addJobInBackground(new OrderMenuJob(requestSuccess.getRefId(), id,
                                preferences.getString("server_url", "")));
                    }

                    break;
                case OrderMenuJob.PROCESS_ID:
                    orderMenuCount++;
                    Log.d(getClass().getSimpleName(), "Count OM: " + orderMenuCount + " <<>> "
                            + "Total OM: " + totalOrderMenus);

                    if (orderMenuCount == totalOrderMenus) {
                        dialog.dismiss();
                        if (orderMenuError){
                            retryRequestOrderMenu();
                        }else {
                            dialogSuccessOrder();
                        }
                        Log.d(getClass().getSimpleName(), "Success ");
                    }

                    Log.d(getClass().getSimpleName(), "RequestSuccess OrderMenuId: "
                            + requestSuccess.getRefId());
                    break;
                case RefreshTokenJob.PROCESS_ID:
                    Log.d(getClass().getSimpleName(), "[ refresh job success, status set :" + refreshStatus.name() + " ]");
                    break;

            }

        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    public void onEventMainThread(GenericEvent.RequestFailed failed) {
        Log.d(getClass().getSimpleName(), "request failed event, process id " + failed.getProcessId());
        switch (failed.getProcessId()) {
            case OrderUpdateJob.PROCESS_ID:
                Log.d(getClass().getSimpleName(), "request updateorder failed");
                retryRequestOrder();
                break;
            case OrderMenuJob.PROCESS_ID:
                orderMenuCount++;
                orderMenuError = true;
                Log.d(getClass().getSimpleName(), "request update order menu failed");
                if (orderMenuCount == totalOrderMenus) {
                    dialog.dismiss();
                    retryRequestOrderMenu();
                    Log.d(getClass().getSimpleName(), "request update order menu failed complate");
                }
                break;
            case RefreshTokenJob.PROCESS_ID:
                dialogRefresh.dismiss();
                refreshStatus = null;
                AlertMessage("Refresh token failed");
                break;
        }

    }

    public void onEventMainThread(LoginEvent.LoginSuccess loginSuccess) {
        dialogRefresh.dismiss();
        Log.d(getClass().getSimpleName(), "[ refresh status " + refreshStatus.name() + " ]");
        if (refreshStatus != null) {
            switch (refreshStatus) {
                case submitOrder:
                    Log.d(getClass().getSimpleName(), "[ re running submit order ]");
                    saveOrder();
                    break;
                case orderFragment:
                    Log.d(getClass().getSimpleName(), "[ reload order ]");
                    sellerOrderListFragment.reloadOrder();
                    break;
                case receiveFragment:
                    Log.d(getClass().getSimpleName(), "[ reload receive ]");
                    receiveListFragment.reloadReceive();
                    break;
            }
        }
        refreshStatus = null;
    }

    public void onEventMainThread(LoginEvent.LoginFailed loginFailed) {
        dialogRefresh.dismiss();
        Log.d(getClass().getSimpleName(), "[ refresh status failed ]");
        if (refreshStatus != null) {
            switch (refreshStatus) {
                case submitOrder:
                    Log.d(getClass().getSimpleName(), "[ refresh token submit order failed ]");
                    reloadRefreshToken();
                    break;
                case orderFragment:
                    Log.d(getClass().getSimpleName(), "[ refresh token orderfragment failed ]");
                    sellerOrderListFragment.reloadRefreshToken();
                    break;
                case receiveFragment:
                    Log.d(getClass().getSimpleName(), "[ refresh token receive failed ]");
                    receiveListFragment.reloadRefreshToken();
            }
        }

        refreshStatus = null;
    }

    private void AlertMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Refresh Token");
        builder.setMessage(message);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    private void dialogSuccessOrder() {
        View view = View.inflate(this, R.layout.view_add_to_cart, null);

        Order o = orderDbAdapter.findOrderById(orderId);

        TextView textItem = (TextView) view.findViewById(R.id.text_item_cart);
        TextView textReceipt = (TextView) view.findViewById(R.id.text_item_cart_receipt);

        textItem.setText("Pesanan Anda sedang kami proses");
        textReceipt.setText("No Pesanan : " + o.getReceiptNumber());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setTitle(R.string.order_success);
        builder.setPositiveButton(getString(R.string.continue_shopping), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                orderDbAdapter.updateSyncStatusById(orderId);
                orderId = null;
                refreshStatus = null;
                orderMenuCount = 0;
                orderMenuError = false;
                orderMenuAdapter = new OrderMenuAdapter(MainActivity.this);
                orderListRecycle.setAdapter(orderMenuAdapter);
                updateInfo();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

    }

    private void reloadRefreshToken() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Refresh Token");
        builder.setMessage("Process failed\nRepeat process ?");
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (refreshStatus != null) {
//                    jobManager.addJobInBackground(new RefreshTokenJob(refreshStatus.name()));
                    refreshToken(refreshTokenStatus.submitOrder.name());
                } else {
                    jobManager.addJobInBackground(new RefreshTokenJob());
                }
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                refreshStatus = null;
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void retryRequestSync() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi");
        builder.setMessage("Request gagal\nUlangi proses ?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveOrder();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void retryRequestOrder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi");
        builder.setMessage("Gagal mengirim order\nUlangi proses ?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int which) {
                dialog.show();

                Order order = orderDbAdapter.findOrderById(orderId);

                Log.d(getClass().getSimpleName(), "Order ID : " + orderId
                        + " Update Parameter : >> RefId " + order.getRefId()
                        + " \n>> EntittyOrderId : " + order.getId() + " | "
                        + preferences.getString("server_url", ""));

                jobManager.addJobInBackground(new OrderUpdateJob(order.getRefId(), order.getId(),
                        preferences.getString("server_url", "")));
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void retryRequestOrderMenu() {
        orderMenuError = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi");
        builder.setMessage("Gagal mengirim order...\nUlangi proses ?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int which) {
                dialog.show();

                Order o = orderDbAdapter.findOrderById(orderId);
                orderMenuIdes = orderMenuDbAdapter.findOrderMenuIdesByOrderIdActive(orderId);
                totalOrderMenus = orderMenuIdes.size();
                orderMenuCount = 0;

                Log.d(getClass().getSimpleName(), "Order Menu Ides Size : " + orderMenuIdes.size());

                for (String id : orderMenuIdes) {
                    jobManager.addJobInBackground(new OrderMenuJob(o.getRefId(), id,
                            preferences.getString("server_url", "")));
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }


}
