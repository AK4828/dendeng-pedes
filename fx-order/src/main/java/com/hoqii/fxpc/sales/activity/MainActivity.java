package com.hoqii.fxpc.sales.activity;

import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SlidingPaneLayout;
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
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageAppication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.adapter.OrderMenuAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.ContactDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.OrderDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.OrderMenuDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SiteDatabaseAdapter;
import com.hoqii.fxpc.sales.core.commons.Role;
import com.hoqii.fxpc.sales.core.commons.Site;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.fragment.OrderListFragment;
import com.hoqii.fxpc.sales.fragment.ProductFragmentGrid;
import com.hoqii.fxpc.sales.job.OrderMenuJob;
import com.hoqii.fxpc.sales.job.OrderUpdateJob;
import com.hoqii.fxpc.sales.job.RefreshTokenJob;
import com.hoqii.fxpc.sales.task.RequestOrderSyncTask;
import com.hoqii.fxpc.sales.util.AuthenticationCeck;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.EntypoModule;
import com.joanzapata.iconify.fonts.FontAwesomeIcons;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.joanzapata.iconify.fonts.IoniconsModule;
import com.joanzapata.iconify.fonts.MaterialCommunityModule;
import com.joanzapata.iconify.fonts.MaterialModule;
import com.joanzapata.iconify.fonts.MeteoconsModule;
import com.joanzapata.iconify.fonts.SimpleLineIconsModule;
import com.joanzapata.iconify.fonts.TypiconsIcons;
import com.joanzapata.iconify.fonts.TypiconsModule;
import com.joanzapata.iconify.fonts.WeathericonsModule;
import com.path.android.jobqueue.JobManager;

import org.meruvian.midas.core.service.TaskService;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by miftakhul on 11/13/15.
 */
public class MainActivity extends AppCompatActivity implements TaskService{

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
    private ProgressDialog dialog;
    private TextView textTotalItem,textTotalOrder,textOrderto;
    private OrderDatabaseAdapter orderDbAdapter;
    private OrderMenuDatabaseAdapter orderMenuDbAdapter;
    private SiteDatabaseAdapter siteDatabaseAdapter;
    private RequestOrderSyncTask requestOrderSyncTask;
    private List<OrderMenu> orderMenus = new ArrayList<OrderMenu>();
    private List<String> orderMenuIdes;
    private int orderMenuCount = 0, orderMenuFailedCount = 0;
    private int totalOrderMenus = 0;
    private String orderId = null;
    private DecimalFormat decimalFormat = new DecimalFormat("#,###");
    private RecyclerView orderListRecycle;
    private OrderMenuAdapter orderMenuAdapter;
    private SlidingPaneLayout slidingPaneLayout;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        jobManager = SignageAppication.getInstance().getJobManager();
        preferences = getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        EventBus.getDefault().register(this);

        if (authenticationCeck.isAccess()) {
            Log.d(getClass().getSimpleName(), "application access granted");
        } else {
            Log.d(getClass().getSimpleName(), "application access need to refresh");
            jobManager.addJobInBackground(new RefreshTokenJob());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isMinLoli = true;
        } else {
            isMinLoli = false;
        }

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        textTotalItem = (TextView) findViewById(R.id.text_total_item);
        textTotalOrder = (TextView) findViewById(R.id.text_total_order);
        textOrderto = (TextView) findViewById(R.id.text_orderto);
        orderListRecycle = (RecyclerView) findViewById(R.id.list_order);
//        slidingPaneLayout = (SlidingPaneLayout) findViewById(R.id.slidingLyaout);

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

        if (orderId != null){
            orderMenus = orderMenuDbAdapter.findOrderMenuByOrderId(orderId);
            orderMenuAdapter.addItems(orderMenus);
        }

        setNav();
        setFlot();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Mengirim data ...");
        dialog.setCancelable(false);

    }


    @Override
    protected void onResume() {
        super.onResume();
        updateInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.order_list, menu);

        menu.findItem(R.id.menu_add_site).setIcon(new IconDrawable(this, TypiconsIcons.typcn_business_card).colorRes(R.color.white).actionBarSize());
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
                    }else if (order.getSite().getId() == null){
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Peringatan");
                        builder.setMessage("Anda belum memilih tujuan order");
                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.show();
                    }
                    else {
                        saveOrder();
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
//                        OrderListFragment orderFragment = new OrderListFragment();
//                        FragmentTransaction orderList = getSupportFragmentManager().beginTransaction();
//                        orderList.replace(R.id.content_frame, orderFragment);
//                        orderList.addToBackStack(null);
//                        orderList.commit();
                        break;

                    case R.id.nav_order_purches_history:
                        Intent historyIntent = new Intent(MainActivity.this, SelfHistoryOrderListActivity.class);
                        if (isMinLoli) {
                            startActivity(historyIntent, ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                        } else {
                            startActivity(historyIntent);
                        }
                        break;

                    case R.id.nav_receiving:
                        Intent receiveIntent = new Intent(MainActivity.this, ReceiveListActivity.class);
                        if (isMinLoli) {
                            startActivity(receiveIntent, ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                        } else {
                            startActivity(receiveIntent);
                        }
                        break;

                    case R.id.seller_purchase_order_list:
                        Intent purchaseOrderIntent = new Intent(MainActivity.this, SellerOrderListActivity.class);
                        purchaseOrderIntent.putExtra("orderListType", "purchaseOrderList");
                        if (isMinLoli) {
                            startActivity(purchaseOrderIntent, ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                        } else {
                            startActivity(purchaseOrderIntent);
                        }
                        break;

                    case R.id.seller_shipment_history_list:
                        Intent shipmentHistoryIntent = new Intent(MainActivity.this, ShipmentHistoryListActivity.class);
                        if (isMinLoli) {
                            startActivity(shipmentHistoryIntent, ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                        } else {
                            startActivity(shipmentHistoryIntent);
                        }
                        break;

                    case R.id.nav_points:
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

    private void setFlot() {
        final FloatingActionButton floatingActionButton;
        floatingActionButton = (FloatingActionButton) findViewById(R.id.add_item);
        floatingActionButton.setImageDrawable(new IconDrawable(this, TypiconsIcons.typcn_pen).colorRes(R.color.white));

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int x = (floatingActionButton.getLeft() + floatingActionButton.getRight()) / 2;
                int y = (floatingActionButton.getTop() + floatingActionButton.getBottom()) / 2;
                orderOption(x, y);
            }
        });
    }

    public void updateInfo(){
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
                textOrderto.setText("Order ke : " + site.getName());
            }else {
                textOrderto.setText("Order ke : ");
            }

            textTotalItem.setText("Jumlah Item: " + totalItem);
            textTotalOrder.setText("Total Order: " + "Rp " + decimalFormat.format(totalPrice));
        }else {
            textOrderto.setText("Order ke : ");
            textTotalItem.setText("Jumlah Item: ");
            textTotalOrder.setText("Total Order: ");
        }
    }

    private void saveOrder() {
        orderId = orderDbAdapter.getOrderId();
        String receiptNumber = orderDbAdapter.getReceiptNumberByOrderId(orderId);
        if (receiptNumber.isEmpty() || receiptNumber.equalsIgnoreCase(null) || receiptNumber.equalsIgnoreCase("")){
            requestOrderSyncTask = new RequestOrderSyncTask(this,
                    this, orderId);
            requestOrderSyncTask.execute();
            Log.d(getClass().getSimpleName(), "[ receipt number from order id "+ orderId + " [is null] ]");
        }else {
            Log.d(getClass().getSimpleName(), "[ receipt number from order id "+ orderId + " [is not null / receiptNumber"+ receiptNumber +" ] ]");
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

    public void orderUpdate(String productId, int qty){
        Intent i = new Intent(this, OrderActivity.class);
        i.putExtra("productId", productId);
        i.putExtra("qtyUpdate", qty);
        startActivityForResult(i, ORDER_REQUEST);
    }


    public void orderOption(int x, int y) {
//        startActivityForResult(new Intent(this, MainActivityMaterialNew.class), ORDER_REQUEST_OPTIONS);
//        Intent i = new Intent(this, MainActivityMaterialNew.class);
        Intent i = new Intent(this, MainActivityMaterial.class);
        i.putExtra("xCoordinate", x);
        i.putExtra("yCoordinate", y);
        startActivityForResult(i, ORDER_REQUEST_OPTIONS, ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("result", "on=========================");

        if (requestCode == ORDER_REQUEST) {
            if (resultCode == RESULT_OK) {
//                OrderListFragment orderFragment = new OrderListFragment();
//                FragmentTransaction orderList = getSupportFragmentManager().beginTransaction();
//                orderList.replace(R.id.content_frame, orderFragment);
//                orderList.commitAllowingStateLoss();
                updateInfo();
                orderMenus = orderMenuDbAdapter.findOrderMenuByOrderId(orderId);
                orderMenuAdapter.addItems(orderMenus);

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
                        orderMenus = orderMenuDbAdapter.findOrderMenuByOrderId(orderId);
                        orderMenuAdapter.addItems(orderMenus);

                    }
//                    else if (type.equalsIgnoreCase("category")) {
//                        Bundle bundle = new Bundle();
//                        bundle.putString("parent_category", data.getExtras().getString("parent_category", null));
//                        bundle.putInt("tx_id", data.getExtras().getInt("tx_id"));
//                        ProductFragmentGrid productFragment = new ProductFragmentGrid();
//                        productFragment.setArguments(bundle);
//                        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, productFragment).addToBackStack(null).commitAllowingStateLoss();
//                    }
                }
            }
        }
//        else if (requestCode == BUSINESS_REQUEST) {
//            if (resultCode == RESULT_OK) {
//                Log.d(getClass().getSimpleName(), "business result");
//                OrderListFragment orderFragment = new OrderListFragment();
//
//                FragmentTransaction orderList = getSupportFragmentManager().beginTransaction();
//                orderList.replace(R.id.content_frame, orderFragment);
//                orderList.commitAllowingStateLoss();
//
//            }
//        }
        else if (requestCode == SITE_REQUEST) {
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
        Log.d(getClass().getSimpleName(), result + " succecc code "+code);

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
//        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        retryRequestSync();
        Log.e(getClass().getSimpleName(), message);

    }

    public void onEventMainThread(GenericEvent.RequestInProgress requestInProgress) {
        Log.d(getClass().getSimpleName(), "RequestInProgress: " + requestInProgress.getProcessId());
    }

    public void onEventMainThread(GenericEvent.RequestSuccess requestSuccess) {
        try {
            switch (requestSuccess.getProcessId()) {
                case OrderUpdateJob.PROCESS_ID: {
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
                }
                case OrderMenuJob.PROCESS_ID: {
                    orderMenuCount++;
                    Log.d(getClass().getSimpleName(), "Count OM: " + orderMenuCount + " <<>> "
                            + "Total OM: " + totalOrderMenus);

                    if (orderMenuCount == totalOrderMenus) {
                        dialog.dismiss();
                        dialogSuccessOrder();
                        Log.d(getClass().getSimpleName(), "Success ");
                    }

                    Log.d(getClass().getSimpleName(), "RequestSuccess OrderMenuId: "
                            + requestSuccess.getRefId());
                    break;
                }
            }

        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    public void onEventMainThread(GenericEvent.RequestFailed failed) {
        dialog.dismiss();
        Log.d(getClass().getSimpleName(), "request failed event, process id " + failed.getProcessId());
        switch (failed.getProcessId()){
            case OrderUpdateJob.PROCESS_ID: {
                Log.d(getClass().getSimpleName(), "request updateorder failed");
                retryRequestOrder();
                break;
            }
            case OrderMenuJob.PROCESS_ID:{
                orderMenuCount++;
                Log.d(getClass().getSimpleName(), "request update order menu failed");
                if (orderMenuCount == totalOrderMenus){
                    retryRequestOrderMenu();
                    Log.d(getClass().getSimpleName(), "request update order menu failed complate");
                }
                break;
            }
        }

        Log.e(getClass().getSimpleName(),
                failed.getResponse().getHttpResponse().getStatusLine().getStatusCode() + " :"
                        + failed.getResponse().getHttpResponse().getStatusLine().getReasonPhrase());
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
                orderMenuCount = 0;
                orderMenuAdapter = new OrderMenuAdapter(MainActivity.this);
                orderListRecycle.setAdapter(orderMenuAdapter);
                updateInfo();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

    }

    public void retryRequestSync(){
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

    public void retryRequestOrder(){
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

    public void retryRequestOrderMenu(){
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
