package com.hoqii.fxpc.sales.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.adapter.ShipmentHistoryMenuAdapter;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.entity.OrderMenuSerial;
import com.hoqii.fxpc.sales.entity.Product;
import com.hoqii.fxpc.sales.entity.Shipment;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.meruvian.midas.core.service.TaskService;
import org.meruvian.midas.core.util.ConnectionUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 2/10/16.
 */
public class ShipmentMenuListFragment extends Fragment implements TaskService{

    private LinearLayout dataNull, dataFailed;
    private RecyclerView recyclerView;
    private ShipmentHistoryMenuAdapter shipmentMenuAdapter;
    private List<OrderMenuSerial> orderMenuSerialList = new ArrayList<OrderMenuSerial>();
    private SharedPreferences preferences;
    private int page = 1, totalPage;
    private String shipmentDetailUrl = "/api/shipmentHistory/";
    private ProgressDialog loadProgress;
    private Shipment shipment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shipment_menu, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.shipment_menu_list);
        dataNull = (LinearLayout) view.findViewById(R.id.dataNull);
        dataFailed = (LinearLayout) view.findViewById(R.id.dataFailed);
        preferences = getActivity().getSharedPreferences(SignageVariables.PREFS_SERVER, 0);

        shipmentMenuAdapter = new ShipmentHistoryMenuAdapter(getActivity(), this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(shipmentMenuAdapter);

        loadProgress = new ProgressDialog(getActivity());
        loadProgress.setMessage("Fetching data...");

        Bundle b = getArguments();
        String shipmentJson = b.getString("shipmentJson");

        ObjectMapper mapper = SignageApplication.getObjectMapper();
        try {
            shipment = mapper.readValue(shipmentJson, Shipment.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                SerialOrderMenuSync serialOrderMenuSync = new SerialOrderMenuSync(getActivity(), ShipmentMenuListFragment.this, false);
                serialOrderMenuSync.execute(shipment.getId(), "0");
            }
        });

        return view;
    }



    @Override
    public void onExecute(int code) {

    }

    @Override
    public void onSuccess(int code, Object result) {

        shipmentMenuAdapter.addItems(orderMenuSerialList);
        Log.d(getClass().getSimpleName(), "jumlah serial "+ orderMenuSerialList.size());

        dataFailed.setVisibility(View.GONE);

        if (orderMenuSerialList.size() > 0) {
            dataNull.setVisibility(View.GONE);
        } else {
            dataNull.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCancel(int code, String message) {

    }

    @Override
    public void onError(int code, String message) {
        dataFailed.setVisibility(View.VISIBLE);
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
            return ConnectionUtil.get(preferences.getString("server_url", "") + shipmentDetailUrl + JsonObject[0] + "/menus?access_token="
                    + AuthenticationUtils.getCurrentAuthentication().getAccessToken() + "&page=" + JsonObject[1]);
        }

        @Override
        protected void onCancelled() {
            taskService.onCancel(SignageVariables.SERIAL_ORDER_MENU_GET_TASK, "Batal");
        }

        @Override
        protected void onPreExecute() {
            if (!isLoadMore){
//                swipeRefreshLayout.setRefreshing(true);
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
                    Log.d(getClass().getSimpleName(), "serial menu : "+result.toString());
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
                                product.setDescription(productObject.getString("description"));
                                orderMenu.setProduct(product);
                            }
                            orderMenuSerial.setOrderMenu(orderMenu);
                        }
                        orderMenuSerials.add(orderMenuSerial);
                    }
                    orderMenuSerialList = orderMenuSerials;

                    if (isLoadMore){
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

    public void loadMoreContent(){
        if (page < totalPage){
            loadProgress.show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    SerialOrderMenuSync receiveSync = new SerialOrderMenuSync(getActivity(), ShipmentMenuListFragment.this, true);
                    receiveSync.execute(shipment.getId(), Integer.toString(page));
                }
            }, 500);
        }
    }
}
