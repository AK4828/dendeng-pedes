package com.hoqii.fxpc.sales.activity;

import android.app.FragmentManager;
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
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageAppication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.core.commons.Role;
import com.hoqii.fxpc.sales.fragment.OrderListFragment;
import com.hoqii.fxpc.sales.fragment.ProductFragmentGrid;
import com.hoqii.fxpc.sales.job.RefreshTokenJob;
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

import java.util.List;

/**
 * Created by miftakhul on 11/13/15.
 */
public class MainActivity extends AppCompatActivity {

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        jobManager = SignageAppication.getInstance().getJobManager();

        preferences = getSharedPreferences(SignageVariables.PREFS_SERVER, 0);

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);

        setNav();
        setFlot();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        OrderListFragment orderFragment = new OrderListFragment();
        FragmentTransaction orderList = getSupportFragmentManager().beginTransaction();
        orderList.replace(R.id.content_frame, orderFragment);
        orderList.commit();

        Iconify.with(new FontAwesomeModule());
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
                        OrderListFragment orderFragment = new OrderListFragment();
                        FragmentTransaction orderList = getSupportFragmentManager().beginTransaction();
                        orderList.replace(R.id.content_frame, orderFragment);
                        orderList.addToBackStack(null);
                        orderList.commit();
                        break;

//                    case R.id.nav_order_purches:
//                        CategoryFragmentGrid category_purches = new CategoryFragmentGrid();
//                        Bundle bundle = new Bundle();
//                        category_purches.setArguments(bundle);
//
//                        FragmentTransaction transaction_purchse = getSupportFragmentManager().beginTransaction();
//                        transaction_purchse.replace(R.id.content_frame, category_purches);
//                        transaction_purchse.addToBackStack(null);
//                        transaction_purchse.commit();
//                        break;

                    case R.id.nav_order_purches_history:
//                        Intent historyIntent = new Intent(MainActivity.this, HistoryOrderListActivity.class);
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

//                    case R.id.seller_shipment_list:
//                        Intent shipmentIntent = new Intent(MainActivity.this, ShipmentListActivity.class);
//                        if (isMinLoli) {
//                            startActivity(shipmentIntent, ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this).toBundle());
//                        } else {
//                            startActivity(shipmentIntent);
//                        }
//                        break;

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

    public void openBusinessPartner() {
        startActivityForResult(new Intent(this, BusinessPartnerActivity.class), BUSINESS_REQUEST);
    }

    public void openSite() {
        if (isMinLoli) {
            startActivityForResult(new Intent(this, SiteListActivity.class), SITE_REQUEST, ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this).toBundle());
        } else {
            startActivityForResult(new Intent(this, SiteListActivity.class), SITE_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("result", "on=========================");

        if (requestCode == ORDER_REQUEST) {
            if (resultCode == RESULT_OK) {
                OrderListFragment orderFragment = new OrderListFragment();
                FragmentTransaction orderList = getSupportFragmentManager().beginTransaction();
                orderList.replace(R.id.content_frame, orderFragment);
                orderList.commitAllowingStateLoss();
                Log.d("result", "ok =========================");
            }
        } else if (requestCode == ORDER_REQUEST_OPTIONS) {
            if (resultCode == RESULT_OK) {
                Log.d("result", "ok orderOptions =========================");

                if (data != null) {
                    String type = data.getExtras().getString("type", null);
                    Log.d("result type", type);
                    if (type.equalsIgnoreCase("orderList")) {
                        OrderListFragment orderFragment = new OrderListFragment();
                        FragmentTransaction orderList = getSupportFragmentManager().beginTransaction();
                        orderList.replace(R.id.content_frame, orderFragment);
                        orderList.commitAllowingStateLoss();
                    } else if (type.equalsIgnoreCase("category")) {
                        Bundle bundle = new Bundle();
                        bundle.putString("parent_category", data.getExtras().getString("parent_category", null));
                        bundle.putInt("tx_id", data.getExtras().getInt("tx_id"));
                        ProductFragmentGrid productFragment = new ProductFragmentGrid();
                        productFragment.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, productFragment).addToBackStack(null).commitAllowingStateLoss();
                    }
                }
            }
        } else if (requestCode == BUSINESS_REQUEST) {
            if (resultCode == RESULT_OK) {
                Log.d(getClass().getSimpleName(), "business result");
                OrderListFragment orderFragment = new OrderListFragment();

                FragmentTransaction orderList = getSupportFragmentManager().beginTransaction();
                orderList.replace(R.id.content_frame, orderFragment);
                orderList.commitAllowingStateLoss();

            }
        } else if (requestCode == SITE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Log.d(getClass().getSimpleName(), "site result");
                OrderListFragment orderFragment = new OrderListFragment();

                FragmentTransaction orderList = getSupportFragmentManager().beginTransaction();
                orderList.replace(R.id.content_frame, orderFragment);
                orderList.commitAllowingStateLoss();
            }
        } else if (requestCode == PREFERENCE_REQUEST) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    private void clearBackStack() {
        final FragmentManager fragmentManager = getFragmentManager();
        while (fragmentManager.getBackStackEntryCount() != 0) {
            fragmentManager.popBackStackImmediate();
        }
    }

}
