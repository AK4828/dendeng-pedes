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
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.activity.MainActivity;
import com.hoqii.fxpc.sales.activity.ReceiveListActivity;
import com.hoqii.fxpc.sales.adapter.ReceiveFragmentAdapter;
import com.hoqii.fxpc.sales.core.LogInformation;
import com.hoqii.fxpc.sales.core.commons.Site;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.entity.Receive;
import com.hoqii.fxpc.sales.entity.Shipment;
import com.hoqii.fxpc.sales.util.AuthenticationCeck;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.path.android.jobqueue.JobManager;

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
public class ReceiveListFragment extends Fragment implements TaskService {
    private int requestDetailCode = 127;

    private List<Receive> receiveList = new ArrayList<Receive>();
    private SharedPreferences preferences;
    private RecyclerView recyclerView;
    private ReceiveFragmentAdapter receiveAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout dataNull, dataFailed, dataCheck;
    private boolean isMinLoli = false;
    private String receiveUrl = "/api/order/receives/status";
    private JobManager jobManager;
    private AuthenticationCeck authenticationCeck = new AuthenticationCeck();
    private Button checkButton, reloadButton, showMoreButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getActivity().getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        jobManager = SignageApplication.getInstance().getJobManager();
        receiveAdapter = new ReceiveFragmentAdapter(getActivity(), this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isMinLoli = true;
        } else {
            isMinLoli = false;
        }

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receive_list, container, false);

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
        recyclerView.setAdapter(receiveAdapter);

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
                    ReceiveSync receiveSync = new ReceiveSync(getActivity(), ReceiveListFragment.this);
                    receiveSync.execute();
                } else {
//                    ((MainActivity) getActivity()).refreshToken(MainActivity.refreshTokenStatus.receiveFragment.name());
                    ((MainActivity) getActivity()).refreshToken(MainActivity.REFRESH_TOKEN_RECEIVE_LIST_FRAGMENT_ORDER);
                }
            }
        });

        showMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent receiveIntent = new Intent(getActivity(), ReceiveListActivity.class);
                if (isMinLoli) {
                    getActivity().startActivity(receiveIntent, ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity()).toBundle());
                } else {
                    startActivity(receiveIntent);
                }
            }
        });

        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReceiveSync receiveSync = new ReceiveSync(getActivity(), ReceiveListFragment.this);
                receiveSync.execute();
            }
        });

        return view;
    }

    public void openReceiveDetail(Intent intent) {
        startActivityForResult(intent, requestDetailCode);
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

        if (receiveList.size() > 5) {
            List<Receive> tempList = new ArrayList<Receive>();
            for (int x = 0; x < 5; x++) {
                tempList.add(receiveList.get(x));
            }
            receiveAdapter.addItems(tempList);
            showMoreButton.setVisibility(View.VISIBLE);
        } else {
            receiveAdapter.addItems(receiveList);
            showMoreButton.setVisibility(View.GONE);
        }

        if (receiveList.size() > 0) {
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


    class ReceiveSync extends AsyncTask<String, Void, JSONObject> {

        private Context context;
        private TaskService taskService;

        public ReceiveSync(Context context, TaskService taskService) {
            this.context = context;
            this.taskService = taskService;
        }


        @Override
        protected JSONObject doInBackground(String... JsonObject) {
            Log.d(getClass().getSimpleName(), "?acces_token= " + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
            return ConnectionUtil.get(preferences.getString("server_url", "") + receiveUrl + "?access_token="
                    + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
        }

        @Override
        protected void onCancelled() {
            taskService.onCancel(SignageVariables.RECEIVE_GET_TASK, "Batal");
        }

        @Override
        protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
            taskService.onExecute(SignageVariables.RECEIVE_GET_TASK);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {

                    List<Receive> receives = new ArrayList<Receive>();

                    JSONArray jsonArray = result.getJSONArray("content");
                    for (int a = 0; a < jsonArray.length(); a++) {
                        JSONObject object = jsonArray.getJSONObject(a);

                        Receive receive = new Receive();
                        receive.setId(object.getString("id"));

                        JSONObject logInformationObject = new JSONObject();
                        if (!object.isNull("logInformation")) {
                            logInformationObject = object.getJSONObject("logInformation");

                            LogInformation logInformation = new LogInformation();
                            logInformation.setCreateDate(new Date(logInformationObject.getLong("createDate")));

                            receive.setLogInformation(logInformation);
                        }

                        JSONObject shipmentObject = new JSONObject();
                        if (!object.isNull("shipment")) {
                            shipmentObject = object.getJSONObject("shipment");
                            Shipment shipment = new Shipment();
                            shipment.setId(shipmentObject.getString("id"));
                            shipment.setReceiptNumber(shipmentObject.getString("receiptNumber"));
                            shipment.setDeliveryServiceName(shipmentObject.getString("deliveryServiceName"));
                            if (shipmentObject.getString("status").equalsIgnoreCase("WAIT")) {
                                shipment.setStatus(Shipment.ShipmentStatus.WAIT);
                            } else if (shipmentObject.getString("status").equalsIgnoreCase("DELIVERED")) {
                                shipment.setStatus(Shipment.ShipmentStatus.DELIVERED);
                            } else if (shipmentObject.getString("status").equalsIgnoreCase("FAILED")) {
                                shipment.setStatus(Shipment.ShipmentStatus.FAILED);
                            }

                            JSONObject shipmentLogInformationObject = new JSONObject();
                            if (!shipmentObject.isNull("logInformation")) {
                                shipmentLogInformationObject = shipmentObject.getJSONObject("logInformation");

                                LogInformation logInformation = new LogInformation();
                                logInformation.setCreateDate(new Date(shipmentLogInformationObject.getLong("createDate")));

                                shipment.setLogInformation(logInformation);
                            }

                            receive.setShipment(shipment);
                        }

                        if (object.getString("status").equalsIgnoreCase("WAIT")) {
                            receive.setStatus(Receive.ReceiveStatus.WAIT);
                        } else if (object.getString("status").equalsIgnoreCase("RECEIVED")) {
                            receive.setStatus(Receive.ReceiveStatus.RECEIVED);
                        } else if (object.getString("status").equalsIgnoreCase("FAILED")) {
                            receive.setStatus(Receive.ReceiveStatus.FAILED);
                        }

                        receive.setRecipient(object.getString("recipient"));

                        JSONObject orderObject = new JSONObject();
                        if (!object.isNull("order")) {
                            orderObject = object.getJSONObject("order");

                            Order order = new Order();
                            order.setId(orderObject.getString("id"));
                            order.setReceiptNumber(orderObject.getString("receiptNumber"));

                            JSONObject orderLogInformationObject = new JSONObject();
                            if (!orderObject.isNull("logInformation")) {
                                orderLogInformationObject = orderObject.getJSONObject("logInformation");

                                LogInformation logInformation = new LogInformation();
                                logInformation.setCreateDate(new Date(orderLogInformationObject.getLong("createDate")));

                                order.setLogInformation(logInformation);
                            }

                            JSONObject siteObject = new JSONObject();
                            if (!orderObject.isNull("site")) {
                                siteObject = orderObject.getJSONObject("site");

                                Site site = new Site();
                                site.setId(siteObject.getString("id"));
                                site.setName(siteObject.getString("name"));
                                site.setDescription(siteObject.getString("description"));
                                order.setSite(site);
                            }
                            receive.setOrder(order);
                        }
                        receives.add(receive);
                    }

                    receiveList = receives;
                    taskService.onSuccess(SignageVariables.RECEIVE_GET_TASK, true);
                } else {
                    taskService.onError(SignageVariables.RECEIVE_GET_TASK, "Error");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                taskService.onError(SignageVariables.RECEIVE_GET_TASK, "Error");
            }


        }
    }

    public void refreshContent() {
        //clean data
        receiveAdapter = new ReceiveFragmentAdapter(getActivity(), this);
        recyclerView.setAdapter(receiveAdapter);
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                ReceiveSync receiveSync = new ReceiveSync(getActivity(), ReceiveListFragment.this);
                receiveSync.execute();
            }
        });
    }

//    public void reloadRefreshToken() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle("Refresh Token");
//        builder.setMessage("Process failed\nRepeat process ?");
//        builder.setCancelable(false);
//        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                ((MainActivity) getActivity()).refreshToken(MainActivity.refreshTokenStatus.receiveFragment.name());
//            }
//        });
//        builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        builder.show();
//    }

    public void reloadReceive() {
        ReceiveSync receiveSync = new ReceiveSync(getActivity(), ReceiveListFragment.this);
        receiveSync.execute();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestDetailCode) {
            receiveAdapter = new ReceiveFragmentAdapter(getActivity(), ReceiveListFragment.this);
            recyclerView.setAdapter(receiveAdapter);
            ReceiveSync receiveSync = new ReceiveSync(getActivity(), ReceiveListFragment.this);
            receiveSync.execute();
        }

    }
}
