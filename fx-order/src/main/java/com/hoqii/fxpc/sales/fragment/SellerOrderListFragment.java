package com.hoqii.fxpc.sales.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.activity.MainActivity;
import com.hoqii.fxpc.sales.activity.SellerOrderListActivity;
import com.hoqii.fxpc.sales.adapter.SellerOrderFragmentAdapter;
import com.hoqii.fxpc.sales.core.LogInformation;
import com.hoqii.fxpc.sales.core.commons.Site;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.util.AuthenticationCeck;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.meruvian.midas.core.service.TaskService;
import org.meruvian.midas.core.util.ConnectionUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by miftakhul on 12/8/15.
 */
public class SellerOrderListFragment extends Fragment implements TaskService {
    private int requestOrderMenuCode = 122;

    private List<Order> orderList = new ArrayList<Order>();
    private SharedPreferences preferences;
    private RecyclerView recyclerView;
    private SellerOrderFragmentAdapter sellerOrderAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout dataNull, dataFailed, dataCheck;
    private boolean isMinLoli = false;
    private String orderUrl = "/api/purchaseOrders";
    private AuthenticationCeck authenticationCeck = new AuthenticationCeck();
    private Button checkButton, reloadButton, showMoreButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getActivity().getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        sellerOrderAdapter = new SellerOrderFragmentAdapter(getActivity(), this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isMinLoli = true;
        } else {
            isMinLoli = false;
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seller_order_list, container, false);

        checkButton = (Button) view.findViewById(R.id.btn_check);
        reloadButton = (Button) view.findViewById(R.id.btn_reload);
        showMoreButton = (Button) view.findViewById(R.id.btn_showmore);

        if (isMinLoli == false){
            checkButton.setTextColor(getResources().getColor(R.color.colorAccent));
            reloadButton.setTextColor(getResources().getColor(R.color.colorAccent));
            showMoreButton.setTextColor(getResources().getColor(R.color.colorAccent));
        }

        recyclerView = (RecyclerView) view.findViewById(R.id.order_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(sellerOrderAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiptRefress);
        swipeRefreshLayout.setColorSchemeResources(R.color.green, R.color.yellow, R.color.blue, R.color.red);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });

        dataNull = (LinearLayout) view.findViewById(R.id.dataNull);
        dataFailed = (LinearLayout) view.findViewById(R.id.dataFailed);
        dataCheck = (LinearLayout) view.findViewById(R.id.dataCheck);

        checkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (authenticationCeck.isAccess()) {
                    OrderSync orderSync = new OrderSync(getActivity(), SellerOrderListFragment.this);
                    orderSync.execute();
                } else {
                    ((MainActivity)getActivity()).refreshToken(MainActivity.refreshTokenStatus.orderFragment.name());
                }
            }
        });

        showMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent purchaseOrderIntent = new Intent(getActivity(), SellerOrderListActivity.class);
                purchaseOrderIntent.putExtra("orderListType", "purchaseOrderList");
                if (isMinLoli) {
                    getActivity().startActivity(purchaseOrderIntent, ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity()).toBundle());
                } else {
                    startActivity(purchaseOrderIntent);
                }
            }
        });

        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrderSync orderSync = new OrderSync(getActivity(), SellerOrderListFragment.this);
                orderSync.execute();
            }
        });

        return view;
    }


    @Override
    public void onExecute(int code) {
//        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void onSuccess(int code, Object result) {
        swipeRefreshLayout.setRefreshing(false);
        dataFailed.setVisibility(View.GONE);
        dataCheck.setVisibility(View.GONE);

        if (orderList.size() > 5) {
            List<Order> tempList = new ArrayList<Order>();
            for (int x = 0; x < 5; x++) {
                tempList.add(orderList.get(x));
            }
            sellerOrderAdapter.addItems(tempList);
            showMoreButton.setVisibility(View.VISIBLE);
        }else {
            sellerOrderAdapter.addItems(orderList);
            showMoreButton.setVisibility(View.GONE);
        }

        if (orderList.size() > 0) {
            dataNull.setVisibility(View.GONE);
        } else {
            dataNull.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onCancel(int code, String message) {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onError(int code, String message) {
        swipeRefreshLayout.setRefreshing(false);
        dataFailed.setVisibility(View.VISIBLE);
        dataCheck.setVisibility(View.GONE);
        dataNull.setVisibility(View.GONE);
    }


    class OrderSync extends AsyncTask<String, Void, JSONObject> {

        private Context context;
        private TaskService taskService;
        private boolean isLoadMore = false;

        public OrderSync(Context context, TaskService taskService) {
            this.context = context;
            this.taskService = taskService;
        }


        @Override
        protected JSONObject doInBackground(String... JsonObject) {
            Log.d(getClass().getSimpleName(), "?acces_token= " + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
            return ConnectionUtil.get(preferences.getString("server_url", "") + orderUrl + "?access_token="
                    + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
        }

        @Override
        protected void onCancelled() {
            taskService.onCancel(SignageVariables.SELLER_ORDER_GET_TASK, "Batal");
        }

        @Override
        protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
            taskService.onExecute(SignageVariables.SELLER_ORDER_GET_TASK);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {
                    List<Order> orders = new ArrayList<Order>();

                    JSONArray jsonArray = result.getJSONArray("content");
                    for (int a = 0; a < jsonArray.length(); a++) {
                        JSONObject object = jsonArray.getJSONObject(a);

                        Order order = new Order();
                        order.setId(object.getString("id"));
                        order.setReceiptNumber(object.getString("receiptNumber"));

                        JSONObject logInformationObject = new JSONObject();
                        if (!object.isNull("logInformation")) {
                            logInformationObject = object.getJSONObject("logInformation");

                            LogInformation logInformation = new LogInformation();
                            logInformation.setCreateDate(new Date(logInformationObject.getLong("createDate")));

                            order.setLogInformation(logInformation);
                        }

                        JSONObject siteObject = new JSONObject();
                        if (!object.isNull("siteFrom")) {
                            siteObject = object.getJSONObject("siteFrom");
                            Site siteFrom = new Site();
                            siteFrom.setId(siteObject.getString("id"));
                            siteFrom.setName(siteObject.getString("name"));
                            siteFrom.setEmail(siteObject.getString("email"));

                            order.setSiteFrom(siteFrom);
                        }

                        orders.add(order);
                    }
                    orderList = orders;
                    taskService.onSuccess(SignageVariables.SELLER_ORDER_GET_TASK, true);
                } else {
                    taskService.onError(SignageVariables.SELLER_ORDER_GET_TASK, "Error");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                taskService.onError(SignageVariables.SELLER_ORDER_GET_TASK, "Error");
            }

        }
    }

    public void refreshContent() {
        //clean data
        sellerOrderAdapter = new SellerOrderFragmentAdapter(getActivity(), this);
        recyclerView.setAdapter(sellerOrderAdapter);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                OrderSync orderSync = new OrderSync(getActivity(), SellerOrderListFragment.this);
                orderSync.execute();
            }
        });
    }

    public void openOrderMenuActivity(Intent data, View orderNumber, View orderDate) {
        Pair<View, String> pairOrderNumber = Pair.create(orderNumber, getString(R.string.transition_number));
        Pair<View, String> pairOrderDate = Pair.create(orderDate, getString(R.string.transition_date));

        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), pairOrderNumber, pairOrderDate);
        if (isMinLoli) {
            getActivity().startActivityForResult(data, requestOrderMenuCode, optionsCompat.toBundle());
        } else {
            startActivityForResult(data, requestOrderMenuCode);
        }
    }


    public void reloadRefreshToken(){
        Log.d(getClass().getSimpleName(), "[ refresh token orderfragment failed call ]");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Refresh Token");
        builder.setMessage("Process failed\nRepeat process ?");
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ((MainActivity) getActivity()).refreshToken(MainActivity.refreshTokenStatus.orderFragment.name());
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

    public void reloadOrder(){
        OrderSync orderSync = new OrderSync(getActivity(), SellerOrderListFragment.this);
        orderSync.execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestOrderMenuCode){
            sellerOrderAdapter = new SellerOrderFragmentAdapter(getActivity(), SellerOrderListFragment.this);
            recyclerView.setAdapter(sellerOrderAdapter);
            OrderSync orderSync = new OrderSync(getActivity(), SellerOrderListFragment.this);
            orderSync.execute();
        }

    }
}
