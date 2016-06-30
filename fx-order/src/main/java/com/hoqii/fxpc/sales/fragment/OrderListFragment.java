package com.hoqii.fxpc.sales.fragment;

//import android.app.AlertDialog;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.adapter.OrderListActAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.ContactDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.OrderDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.OrderMenuDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SiteDatabaseAdapter;
import com.hoqii.fxpc.sales.core.commons.Site;
import com.hoqii.fxpc.sales.entity.Contact;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.job.OrderMenuJob;
import com.hoqii.fxpc.sales.job.OrderUpdateJob;
import com.hoqii.fxpc.sales.task.RequestOrderSyncTask;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.TypiconsIcons;
import com.path.android.jobqueue.JobManager;

import org.meruvian.midas.core.service.TaskService;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by miftakhul on 12/4/15.
 */
public class OrderListFragment extends Fragment implements TaskService {

    private Toolbar toolbar;
    private ListView listOrder;
    private TextView textTotalItem;
    private TextView textTotalOrder;
    private TextView textShipto;
    private TextView textOrderto;
    private OrderDatabaseAdapter orderDbAdapter;
    private OrderMenuDatabaseAdapter orderMenuDbAdapter;
    private ContactDatabaseAdapter contactDbAdapter;
    private OrderListActAdapter orderListAdapter;
    private SiteDatabaseAdapter siteDatabaseAdapter;
    private RequestOrderSyncTask requestOrderSyncTask;
    private JobManager jobManager;
    private SharedPreferences preferences;
    private ProgressDialog dialog;
    private MenuItem item;
    private List<OrderMenu> orderMenus = new ArrayList<OrderMenu>();
    private List<Contact> contacts = new ArrayList<Contact>();
    private List<String> orderMenuIdes;
    private Contact contact = null;
    private int orderMenuCount;
    private int totalOrderMenus = 0;
    private int positionItem = -1;
    private String orderId = null, contactId = null, addressId = null;
    private DecimalFormat decimalFormat = new DecimalFormat("#,###");
    private long totalPrice;
    private int totalItem;

    private TextView text_total_item;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Mengirim data ...");
        dialog.setCancelable(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_list, container, false);

        orderMenuCount = 0;

        listOrder = (ListView) view.findViewById(R.id.list_order);
        textTotalItem = (TextView) view.findViewById(R.id.text_total_item);
        textTotalOrder = (TextView) view.findViewById(R.id.text_total_order);
//        textShipto = (TextView) view.findViewById(R.id.text_shipto);
        textOrderto = (TextView) view.findViewById(R.id.text_orderto);


        jobManager = SignageApplication.getInstance().getJobManager();
        preferences = getActivity().getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        EventBus.getDefault().register(this);

        orderDbAdapter = new OrderDatabaseAdapter(getActivity());
        orderMenuDbAdapter = new OrderMenuDatabaseAdapter(getActivity());
        contactDbAdapter = new ContactDatabaseAdapter(getActivity());
        siteDatabaseAdapter = new SiteDatabaseAdapter(getActivity());

        orderId = orderDbAdapter.getOrderId();

        if (orderId != null) {
            Order order = orderDbAdapter.findOrderById(orderId);

            if (order.getSite().getId() != null) {
                Site site = siteDatabaseAdapter.findSiteById(order.getSite().getId());
                textOrderto.setText("Order ke : " + site.getName());
            } else {
                textOrderto.setText("Order ke : ");
            }
        }

        orderListAdapter = new OrderListActAdapter(getActivity(), R.layout.adapter_order_list,
                new ArrayList<OrderMenu>(), this);


        if (orderId != null) {
            totalPrice = 0;
            totalItem = 0;

            Log.d(getClass().getSimpleName(), "Order Id = " + orderId);
            orderMenus = orderMenuDbAdapter.findOrderMenuByOrderId(orderId);

            for (OrderMenu om : orderMenus) {
                totalPrice += om.getProduct().getSellPrice() * om.getQty();
                totalItem += om.getQty();
            }

            textTotalItem.setText("Jumlah Item: " + totalItem);
            textTotalOrder.setText("Total Order: " + "Rp " + decimalFormat.format(totalPrice));

            orderListAdapter.addAll(orderMenus);
        }

        listOrder.setAdapter(orderListAdapter);

        return view;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.order_list, menu);


        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        this.item = item;
        switch (item.getItemId()) {

            case R.id.menu_pay_order:
                if (orderId != null) {
                    Order order = orderDbAdapter.findOrderById(orderId);
                    if (orderMenuDbAdapter.findOrderMenuByOrderId(orderId).size() == 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Peringatan");
                        builder.setMessage("Anda belum memilih barang");
                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        builder.show();
                    } else if (order.getSite().getId() == null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Peringatan");
                        builder.setMessage("Anda belum memilih tujuan order");
                        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.show();
                    } else {
                        saveOrder();
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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

//            case R.id.menu_add_order:
//                ((MainActivity) getActivity()).orderOption();
//
//                EventBus.getDefault().unregister(this);
//                return true;

//            case R.id.menu_add_businessPartner:
//                ((MainActivity) getActivity()).openBusinessPartner();
//                EventBus.getDefault().unregister(this);
//                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        setTotalOrderTab();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

//    @Override
//    public void onPause() {
//        super.onPause();
//        EventBus.getDefault().unregister(this);
//    }


    public void setTotalOrderTab() {
        if (orderId != null) {
            totalPrice = 0;
            totalItem = 0;
            orderMenus = orderMenuDbAdapter.findOrderMenuByOrderId(orderId);

            for (OrderMenu om : orderMenus) {
                totalPrice += om.getProduct().getSellPrice() * om.getQty();
                totalItem += om.getQty();
            }

            textTotalItem.setText("Jumlah Item: " + totalItem);
            textTotalOrder.setText("Total Order: " + "Rp " + decimalFormat.format(totalPrice));
        }
    }

    private void saveOrder() {
        orderId = orderDbAdapter.getOrderId();
        requestOrderSyncTask = new RequestOrderSyncTask(getActivity(),
                this, orderId);
        requestOrderSyncTask.execute();
        dialog.show();
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

                    orderMenuIdes = orderMenuDbAdapter.findOrderMenuIdesByOrderId(requestSuccess.getEntityId());
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
//        Toast.makeText(getActivity(), "Gagal mengirim order", Toast.LENGTH_SHORT).show();
//        setEnabledMenuItem(item, true);
        Log.d(getClass().getSimpleName(), "request failed event, process id " + failed.getProcessId());
        switch (failed.getProcessId()) {
            case OrderUpdateJob.PROCESS_ID: {
                Log.d(getClass().getSimpleName(), "request updateorder failed");
                retryRequestOrder();
                break;
            }
            case OrderMenuJob.PROCESS_ID: {
                Log.d(getClass().getSimpleName(), "request update order menu failed");
                retryRequestOrderMenu();
                break;
            }
        }

        Log.e(getClass().getSimpleName(),
                failed.getResponse().getHttpResponse().getStatusLine().getStatusCode() + " :"
                        + failed.getResponse().getHttpResponse().getStatusLine().getReasonPhrase());
    }

    private void dialogSuccessOrder() {
        View view = View.inflate(getActivity(), R.layout.view_add_to_cart, null);

        Order o = orderDbAdapter.findOrderById(orderId);

        TextView textItem = (TextView) view.findViewById(R.id.text_item_cart);
        TextView textReceipt = (TextView) view.findViewById(R.id.text_item_cart_receipt);

        textItem.setText("Pesanan Anda sedang kami proses");
        textReceipt.setText("No Pesanan : " + o.getReceiptNumber());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        builder.setTitle(R.string.order_success);
        builder.setPositiveButton(getString(R.string.continue_shopping), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                orderDbAdapter.updateSyncStatusById(orderId);

//                OrderListFragment orderFragment = new OrderListFragment();
//                FragmentTransaction orderList = getFragmentManager().beginTransaction();
//                orderList.replace(R.id.content_frame, orderFragment);
//                orderList.addToBackStack(null);
//                orderList.commitAllowingStateLoss();
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

    }


    @Override
    public void onExecute(int code) {
    }

    @Override
    public void onSuccess(int code, Object result) {
        Log.d(getClass().getSimpleName(), result + "");

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

    public void retryRequestSync() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.message_title_confirmation));
        builder.setMessage(getResources().getString(R.string.message_request_failed_repeat));
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Konfirmasi");
        builder.setMessage("Gagal mengirim order...\nUlangi proses ?");
        builder.setCancelable(false);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int which) {
                dialog.show();

                Order o = orderDbAdapter.findOrderById(orderId);
                orderMenuIdes = orderMenuDbAdapter.findOrderMenuIdesByOrderId(orderId);
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

        AlertDialog d = builder.create();
        if (!d.isShowing()) {
            d.show();
        }
    }
}
