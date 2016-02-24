package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.activity.ScannerActivityCustom;
import com.hoqii.fxpc.sales.activity.SellerOrderMenuListActivity;
import com.hoqii.fxpc.sales.content.database.adapter.SerialNumberDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.entity.SerialNumber;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.joanzapata.iconify.Icon;
import com.joanzapata.iconify.widget.IconTextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 12/6/15.
 */
public class SellerOrderMenuAdapter extends RecyclerView.Adapter<SellerOrderMenuAdapter.ViewHolder> {


    private Context context;
    private String orderId;
    private List<OrderMenu> orderMenuList = new ArrayList<OrderMenu>();
    private List<String> orderMenuListSerial = new ArrayList<String>();
    private List<SerialNumber> serialNumbers = new ArrayList<SerialNumber>();
    private SerialNumberDatabaseAdapter serialNumberDatabaseAdapter;
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private SharedPreferences preferences;

    public SellerOrderMenuAdapter(Context context, String orderId) {
        this.context = context;
        this.orderId = orderId;

        serialNumberDatabaseAdapter = new SerialNumberDatabaseAdapter(context);
        serialNumbers = serialNumberDatabaseAdapter.getSerialNumberListByOrderId(orderId);
        for (int x = 0; x < serialNumbers.size(); x++) {
            String id = serialNumbers.get(x).getOrderMenu().getId();
            if (!orderMenuListSerial.contains(id)){
                orderMenuListSerial.add(id);
            }
        }

        if (!imageLoader.isInited()) {
            imageLoader.init(ImageLoaderConfiguration.createDefault(context));
        }

        preferences = context.getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
    }


    public SellerOrderMenuAdapter(Context context, String orderId, List<OrderMenu> orderMenus) {
        this.context = context;
        this.orderId = orderId;
        this.orderMenuList = orderMenus;

        orderMenuListSerial.add("default");
        serialNumberDatabaseAdapter = new SerialNumberDatabaseAdapter(context);
        serialNumbers = serialNumberDatabaseAdapter.getSerialNumberListByOrderId(orderId);
        for (int x = 0; x < serialNumbers.size(); x++) {
            String id = serialNumbers.get(x).getOrderMenu().getId();
            if (!orderMenuListSerial.contains(id)){
                orderMenuListSerial.add(id);
            }
        }
    }

    public SellerOrderMenuAdapter(Context context, String orderId, List<OrderMenu> orderMenus, List<String> orderMenuListSerial) {
        this.context = context;
        this.orderId = orderId;
        this.orderMenuList = orderMenus;
        this.orderMenuListSerial = orderMenuListSerial;

        orderMenuListSerial.add("default");
        serialNumberDatabaseAdapter = new SerialNumberDatabaseAdapter(context);
        serialNumbers = serialNumberDatabaseAdapter.getSerialNumberListByOrderId(orderId);
        for (int x = 0; x < serialNumbers.size(); x++) {
            orderMenuListSerial.add(serialNumbers.get(x).getOrderMenu().getId());
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_seller_order_menu_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.productName.setText(orderMenuList.get(position).getProduct().getName());
        holder.description.setText(orderMenuList.get(position).getDescription());
        holder.productCount.setText("{typcn-shopping-cart} " + Integer.toString(orderMenuList.get(position).getQty()));
        holder.scanCount.setText("{typcn-tick-outline} 0");

        String imageUrl = preferences.getString("server_url", "")+"/api/products/"+orderMenuList.get(position).getProduct().getId() + "/image?access_token="+ AuthenticationUtils.getCurrentAuthentication().getAccessToken();

        for (String orderMenuId : orderMenuListSerial){
            if (orderMenuList.get(position).getId().equalsIgnoreCase(orderMenuId)) {
                Log.d(getClass().getSimpleName(), "same id detected in position " + position);
//                holder.imageStatus.setVisibility(View.VISIBLE);

                List<SerialNumber> serials = serialNumberDatabaseAdapter.getSerialNumberListByOrderIdAndOrderMenuId(orderId, orderMenuId);
                holder.scanCount.setText("{typcn-tick-outline} " + serials.size());
                holder.scanCount.setTextColor(context.getResources().getColor(R.color.colorAccent));
                break;
            } else {
//                holder.imageStatus.setVisibility(View.GONE);
                holder.scanCount.setText("{typcn-tick-outline} 0");
                holder.scanCount.setTextColor(context.getResources().getColor(R.color.grey));
            }
        }

        imageLoader.displayImage(imageUrl, holder.preview, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                holder.preview.setImageResource(R.drawable.no_image);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                holder.preview.setImageResource(R.drawable.no_image);
            }
        });

//        Log.d(getClass().getSimpleName(), "first order menu id  size" + orderMenuList.size());
//        Log.d(getClass().getSimpleName(), "first order menu id " + orderMenuList.get(position).getId());
//
//        for (int x = 0; x < orderMenuListSerial.size(); x++) {
//            Log.d(getClass().getSimpleName(), "first order menu id size" + orderMenuList.size());
//            Log.d(getClass().getSimpleName(), "order menu serial size==== " + orderMenuListSerial.size());
//            Log.d(getClass().getSimpleName(), "order menu id " + orderMenuList.get(position).getId());
//            Log.d(getClass().getSimpleName(), "order menu serial id " + orderMenuListSerial.get(x).toString());
//
//            if (orderMenuList.get(position).getId().equals(orderMenuListSerial.get(x).toString())) {
//                Log.d(getClass().getSimpleName(), "same id detected ");
////                holder.imageStatus.setColorFilter(R.color.colorPrimaryDark);
//                holder.imageStatus.setVisibility(View.VISIBLE);
//            } else {
//                holder.imageStatus.setVisibility(View.GONE);
//            }
//        }

        final List<Integer> count = new ArrayList<Integer>();
        int maxCount = orderMenuList.get(position).getQty();

        for (int x = 1; x <= maxCount; x++) {
            count.add(x);
        }

        ArrayAdapter<Integer> newCountAdapter = new ArrayAdapter<Integer>(context, android.R.layout.simple_spinner_dropdown_item, count);
//        holder.newCount.setAdapter(newCountAdapter);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent(context, ScannerActivityCustom.class);
                data.putExtra("productName", orderMenuList.get(position).getProduct().getName());
                data.putExtra("productQty", orderMenuList.get(position).getQty());
                data.putExtra("orderMenuId", orderMenuList.get(position).getId());
                data.putExtra("position", position);

                ((SellerOrderMenuListActivity) context).openScanner(data);
            }
        });

        if (position == orderMenuList.size() - 1){
            ((SellerOrderMenuListActivity)context).loadMoreContent();
        }
    }

    @Override
    public int getItemCount() {
        return orderMenuList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView productName, description;
        private IconTextView productCount, scanCount;
        private ImageView preview;
//        private ImageView imageStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            productName = (TextView) itemView.findViewById(R.id.om_name);
            productCount = (IconTextView) itemView.findViewById(R.id.om_count);
            scanCount = (IconTextView) itemView.findViewById(R.id.om_count_scan);
//            imageStatus = (ImageView) itemView.findViewById(R.id.ol_img);
            description = (TextView) itemView.findViewById(R.id.om_description);
            preview = (ImageView) itemView.findViewById(R.id.om_preview);
        }
    }

    public void addItems(List<OrderMenu> orderMenus){
        for (OrderMenu o : orderMenus){
            if (!orderMenuList.contains(o)){
                orderMenuList.add(o);
                notifyDataSetChanged();
            }
        }
    }

    public void addOrderMenuSerial(String orderMenuId) {
        if (!orderMenuListSerial.contains(orderMenuId)) {
            Log.d(getClass().getSimpleName(), "bevore added " + orderMenuListSerial.size());
            for (String s : orderMenuListSerial) {
                Log.d(getClass().getSimpleName(), "bevore added serial" + s);
            }
            orderMenuListSerial.add(orderMenuId);
            Log.d(getClass().getSimpleName(), "after added " + orderMenuListSerial.size());
            for (String s : orderMenuListSerial) {
                Log.d(getClass().getSimpleName(), "bevore added serial" + s);
            }

            Log.d(getClass().getSimpleName(), "item serial added");
        }
    }

    public void removeOrderMenuSerial(String orderMenuId) {

        for (int x = 0; x < orderMenuListSerial.size(); x++){
            Log.d(getClass().getSimpleName(), "Serching contains order menu id");
            if (orderMenuListSerial.get(x).equalsIgnoreCase(orderMenuId)){
                orderMenuListSerial.remove(x);
                Log.d(getClass().getSimpleName(), "item serial removed");
                break;
            }else {
                Log.d(getClass().getSimpleName(), "item serial not found, so not removed");
            }
        }

    }


}