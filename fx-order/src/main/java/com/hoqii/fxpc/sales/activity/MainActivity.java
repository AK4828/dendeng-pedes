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
import com.hoqii.fxpc.sales.SignageApplication;
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
import com.hoqii.fxpc.sales.fragment.ReceiveListFragment;
import com.hoqii.fxpc.sales.fragment.SellerOrderListFragment;
import com.hoqii.fxpc.sales.job.OrderMenuJob;
import com.hoqii.fxpc.sales.job.OrderUpdateJob;
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
    private static final int PREFERENCE_REQUEST = 302;
    private static final int SITE_REQUEST = 303;
    private static final int REFRESH_TOKEN_SUBMIT_ORDER = 310;
    public static final int REFRESH_TOKEN_ORDER_LIST_FRAGMENT_ORDER = 311;
    public static final int REFRESH_TOKEN_RECEIVE_LIST_FRAGMENT_ORDER = 312;
    public static final int REFRESH_TOKEN_UPDATE_ORDERMENU = 313;
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
    private DecimalFormat decimalFormat = new DecimalFormat("#,###");
    private RecyclerView orderListRecycle;
    private OrderMenuAdapter orderMenuAdapter;
    private ViewPager slideViewPager;
    private TabLayout slideTablayout;
    private SlidingUpPanelLayout slidingUpPanel;
    private SellerOrderListFragment sellerOrderListFragment;
    private ReceiveListFragment receiveListFragment;
    private LinearLayout dataNull;
    private View appShadow;
    private int orderMenuCount = 0;
    private int totalOrderMenus = 0;
    private String orderId = null;
    private Intent tempIntentUpdateOrder = null;

    private boolean menuItemVisibility = true;
    private boolean orderMenuError = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        jobManager = SignageApplication.getInstance().getJobManager();
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

        MainSlidePagerAdapter mainSlidePagerAdapter = new MainSlidePagerAdapter(getSupportFragmentManager(), this);
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
        dialog.setMessage(getString(R.string.message_send_order));
        dialog.setCancelable(false);

        dialogRefresh = new ProgressDialog(this);
        dialogRefresh.setMessage(getString(R.string.message_wait));
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_pay_order:
                if (orderId != null) {
                    Order order = orderDbAdapter.findOrderById(orderId);
                    if (orderMenuDbAdapter.findOrderMenuByOrderId(orderId).size() == 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle(getString(R.string.message_title_warning));
                        builder.setMessage(getString(R.string.message_item_have_not_selected));
                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.show();
                    } else if (order.getSite().getId() == null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle(getString(R.string.message_title_warning));
                        builder.setMessage(getString(R.string.message_order_destination));
                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.show();
                    } else {
                        if (authenticationCeck.isNetworkAvailable()) {
                            if (authenticationCeck.isAccess()) {
                                Log.d(getClass().getSimpleName(), "[ application access granted ]");
                                Log.d(getClass().getSimpleName(), "[ submiting order run ]");
                                saveOrder();
                            } else {
                                Log.d(getClass().getSimpleName(), "[ application access need to refresh ]");
                                Log.d(getClass().getSimpleName(), "[ refreshing refersh param submit ]");
//                                refreshToken(refreshTokenStatus.submitOrder.name());


                                /**
                                 * try new refresh token :) **/
                                refreshToken(REFRESH_TOKEN_SUBMIT_ORDER);
                            }
                        } else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle(getString(R.string.message_title_internet_access));
                            builder.setMessage(getString(R.string.message_no_internet));
                            builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                        }
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getResources().getString(R.string.message_title_warning));
                    builder.setMessage(getString(R.string.message_item_have_not_selected));
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
                forceUnRegisterWhenExist();
                orderOption();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * unregister eventbus
     **/
    private void forceUnRegisterWhenExist() {
        EventBus.getDefault().unregister(this);
    }


    /**
     * set navigation
     **/
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

                    case R.id.nav_stocks:
                        forceUnRegisterWhenExist();
                        Intent stockIntent = new Intent(MainActivity.this, StockListActivity.class);
                        if (isMinLoli) {
                            startActivity(stockIntent, ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this).toBundle());
                        } else {
                            startActivity(stockIntent);
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
                        forceUnRegisterWhenExist();
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


    /**
     * update info
     **/
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
                textOrderto.setText(getString(R.string.text_order_destination) + site.getName());
            } else {
                textOrderto.setText(getString(R.string.text_order_destination));
            }

            orderMenus = orderMenuDbAdapter.findOrderMenuByOrderId(orderId);
            if (orderMenus.size() > 0) {
                dataNull.setVisibility(View.GONE);
            } else {
                dataNull.setVisibility(View.VISIBLE);
            }
            orderMenuAdapter.addItems(orderMenus);

            textTotalItem.setText(getString(R.string.text_items) + totalItem);
            textTotalOrder.setText(getString(R.string.text_price) + getString(R.string.text_currency) + decimalFormat.format(totalPrice));
        } else {
            textOrderto.setText(getResources().getString(R.string.text_order_destination));
            textTotalItem.setText(getResources().getText(R.string.text_items));
            textTotalOrder.setText(getResources().getString(R.string.text_price));
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
            orderMenuIdes = orderMenuDbAdapter.findOrderMenuIdesByOrderIdActive(orderId);
            totalOrderMenus = orderMenuIdes.size();

            Log.d(getClass().getSimpleName(), "Order Menu Ides Size : " + orderMenuIdes.size());

            for (String id : orderMenuIdes) {
                jobManager.addJobInBackground(new OrderMenuJob(order.getRefId(), id,
                        preferences.getString("server_url", "")));
            }
        }
        dialog.show();
    }


    /**
     * show ar hide actionbar menu
     **/
    private void setMenuItemVisibility(boolean visibility) {
        this.menuItemVisibility = visibility;
        invalidateOptionsMenu();
    }

    /**
     * update order menu.
     * if refresh token failed
     * * save intent to variable then run refresh token
     * * when refresh token finish run intent
     **/
    public void orderUpdate(String jsonProduct, int qty) {
        if (authenticationCeck.isNetworkAvailable()) {
            if (authenticationCeck.isAccess()) {
                Log.d(getClass().getSimpleName(), "[ application access granted ]");
                Log.d(getClass().getSimpleName(), "[ update order run ]");
                Intent i = new Intent(this, OrderActivity.class);
                i.putExtra("jsonProduct", jsonProduct);
                i.putExtra("qtyUpdate", qty);
                startActivityForResult(i, ORDER_REQUEST);
            } else {
                Log.d(getClass().getSimpleName(), "[ application access need to refresh ]");
                Log.d(getClass().getSimpleName(), "[ refreshing refresh param submit ]");
                refreshToken(REFRESH_TOKEN_UPDATE_ORDERMENU);
                tempIntentUpdateOrder = new Intent(this, OrderActivity.class);
                tempIntentUpdateOrder.putExtra("jsonProduct", jsonProduct);
                tempIntentUpdateOrder.putExtra("qtyUpdate", qty);
                Log.d(getClass().getSimpleName(), "[ saving intent update order to temoprary ]");
            }
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle(getResources().getString(R.string.message_title_internet_access));
            builder.setMessage(getResources().getString(R.string.message_no_internet));
            builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    public void orderOption() {
        orderId = orderDbAdapter.getOrderId();
        if (orderId != null) {
            Order order = orderDbAdapter.findOrderById(orderId);
            if (order.getSite().getId() != null) {
                Intent i = new Intent(this, MainActivityMaterial.class);
                i.putExtra("siteId", order.getSite().getId());
                startActivityForResult(i, ORDER_REQUEST_OPTIONS, ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle());
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.message_title_warning));
                builder.setMessage(getString(R.string.message_order_destination));
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        }else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.message_title_warning));
            builder.setMessage(getString(R.string.message_order_destination));
            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }


    /**
     * refresh token,
     * give processid to run
     **/

    public void refreshToken(int processId) {
        authenticationCeck.refreshToken(this, processId);
    }


    private void dialogSuccessOrder() {
        View view = View.inflate(this, R.layout.view_add_to_cart, null);

        Order o = orderDbAdapter.findOrderById(orderId);

        TextView textItem = (TextView) view.findViewById(R.id.text_item_cart);
        TextView textReceipt = (TextView) view.findViewById(R.id.text_item_cart_receipt);

        textItem.setText(R.string.message_order_processed);
        textReceipt.setText(getString(R.string.text_receipt_number) + o.getReceiptNumber());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setTitle(R.string.order_success);
        builder.setPositiveButton(getString(R.string.continue_shopping), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                orderDbAdapter.updateSyncStatusById(orderId);
                orderId = null;
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

    public void retryRequestSync() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.message_title_confirmation));
        builder.setMessage(getString(R.string.message_request_failed_repeat));
        builder.setCancelable(false);
        builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveOrder();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void retryRequestOrder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.message_title_confirmation));
        builder.setMessage(getString(R.string.message_send_order_failed_repeat));
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
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
        builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
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
        builder.setTitle(getResources().getString(R.string.message_title_confirmation));
        builder.setMessage(getString(R.string.message_send_order_menu_failed_repeat));
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
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
        builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("result", "on=========================");

        if (requestCode == ORDER_REQUEST) {
            if (resultCode == RESULT_OK) {
                updateInfo();
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
                if (data != null){
                    boolean reCreateUi = data.getBooleanExtra("reCreateUi", false);
                    if (reCreateUi == true){
                        Log.d(getClass().getSimpleName(), "refresh true");
                        recreate();
                    }
                }else {
                    finish();
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onExecute(int code) {
    }

    @Override
    public void onSuccess(int code, Object result) {
        Log.d(getClass().getSimpleName(), result + " success code " + code);

        if (result != null) {
            if (code == SignageVariables.REQUEST_ORDER) {
                Log.d(getClass().getSimpleName(), result + ">> RequestOrderMenuSyncTask * Success");
                Order order = orderDbAdapter.findOrderById(orderId);
                orderMenuIdes = orderMenuDbAdapter.findOrderMenuIdesByOrderIdActive(orderId);
                totalOrderMenus = orderMenuIdes.size();

                Log.d(getClass().getSimpleName(), "Order Menu Ides Size : " + orderMenuIdes.size());
                for (String id : orderMenuIdes) {
                    jobManager.addJobInBackground(new OrderMenuJob(order.getRefId(), id,
                            preferences.getString("server_url", "")));
                }

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

    }

    public void onEventMainThread(GenericEvent.RequestSuccess requestSuccess) {
        try {
            switch (requestSuccess.getProcessId()) {
                case OrderUpdateJob.PROCESS_ID:
                    dialogSuccessOrder();
                    break;
                case OrderMenuJob.PROCESS_ID:
                    orderMenuCount++;
                    Log.d(getClass().getSimpleName(), "Count OM: " + orderMenuCount + " <<>> "
                            + "Total OM: " + totalOrderMenus);
                    if (orderMenuCount == totalOrderMenus) {
                        dialog.dismiss();
                        if (orderMenuError) {
                            retryRequestOrderMenu();
                        } else {
                            Order order = orderDbAdapter.findOrderById(orderId);

                            jobManager.addJobInBackground(new OrderUpdateJob(order.getRefId(), order.getId(),
                                    preferences.getString("server_url", "")));
                        }
                        Log.d(getClass().getSimpleName(), "Success ");
                    }

                    Log.d(getClass().getSimpleName(), "RequestSuccess OrderMenuId: "
                            + requestSuccess.getRefId());
                    break;
                case REFRESH_TOKEN_SUBMIT_ORDER:
                    saveOrder();
                    break;
                case REFRESH_TOKEN_ORDER_LIST_FRAGMENT_ORDER:
                    Log.d(getClass().getSimpleName(), "[ reload order ]");
                    sellerOrderListFragment.reloadOrder();
                    break;
                case REFRESH_TOKEN_RECEIVE_LIST_FRAGMENT_ORDER:
                    Log.d(getClass().getSimpleName(), "[ reload receive ]");
                    receiveListFragment.reloadReceive();
                    break;
                case REFRESH_TOKEN_UPDATE_ORDERMENU:
                    Log.d(getClass().getSimpleName(), "[ reload update ordermenu ]");
                    startActivityForResult(tempIntentUpdateOrder, ORDER_REQUEST);
                    tempIntentUpdateOrder = null;
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
        }
    }

}
