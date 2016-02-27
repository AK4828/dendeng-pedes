package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.activity.SellerOrderListActivity;
import com.hoqii.fxpc.sales.activity.SellerOrderMenuListActivity;
import com.hoqii.fxpc.sales.entity.Order;
import com.joanzapata.iconify.widget.IconTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by miftakhul on 12/6/15.
 */
public class SellerOrderAdapter extends RecyclerView.Adapter<SellerOrderAdapter.ViewHolder> {


    private Context context;
    private List<Order> orderList = new ArrayList<Order>();
    private String orderType;


    public SellerOrderAdapter(Context context) {
        this.context = context;
    }

    public SellerOrderAdapter(Context context, String orderType) {
        this.context = context;
        this.orderType = orderType;

        Log.d(getClass().getSimpleName(), orderType + "===================");
    }

    public SellerOrderAdapter(Context context, List<Order> orders, String orderType) {
        this.context = context;
        this.orderList = orders;
        this.orderType = orderType;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_seller_order_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy / hh:mm:ss");
        Date date = new Date();
        date.setTime(orderList.get(position).getLogInformation().getCreateDate().getTime());

        holder.siteFrom.setText(orderList.get(position).getSiteFrom().getName());
        holder.orderNumber.setText("{typcn-tag} Order number : " + orderList.get(position).getReceiptNumber());
        holder.orderDate.setText("{typcn-time} Date : " + simpleDateFormat.format(date));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SellerOrderMenuListActivity.class);
                intent.putExtra("orderId", orderList.get(position).getId());
                intent.putExtra("orderDate", orderList.get(position).getLogInformation().getCreateDate().getTime());
                Log.d("date send ", Long.toString(orderList.get(position).getLogInformation().getCreateDate().getTime()));
//                intent.putExtra("orderBusinessPartnerName", orderList.get(position).getContact().getBusinessPartner().getName());
//                intent.putExtra("orderParentSiteName", orderList.get(position).getParentSite().getName());
                intent.putExtra("orderReceipt", orderList.get(position).getReceiptNumber());
                intent.putExtra("siteFromName", orderList.get(position).getSiteFrom().getName());
                intent.putExtra("siteFromEmail", orderList.get(position).getSiteFrom().getEmail());

                View orderNumber = holder.orderNumber;
                View orderDate = holder.orderDate;

                ((SellerOrderListActivity) context).openOrderMenuActivity(intent, orderNumber, orderDate);

            }
        });

        if (position == 0){
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.setMargins(10,24,10,0);
            holder.layout.setLayoutParams(params);
        }else{
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.setMargins(10,0,10,0);
            holder.layout.setLayoutParams(params);
        }

        if (position == orderList.size() - 1){
            ((SellerOrderListActivity) context).loadMoreContent();
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private IconTextView orderNumber, orderDate;
        private TextView siteFrom;
        private LinearLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
            siteFrom = (TextView) itemView.findViewById(R.id.siteFrom);
            orderNumber = (IconTextView) itemView.findViewById(R.id.ol_number);
            orderDate = (IconTextView) itemView.findViewById(R.id.ol_tgl);
            layout = (LinearLayout) itemView.findViewById(R.id.layout);
        }
    }

    public void removeItem(int position) {
        orderList.remove(position);
        notifyItemRemoved(position);
    }

    public void addItems(List<Order> orders){
        for (Order o : orders){
            if (!orderList.contains(o)){
                orderList.add(o);
                notifyDataSetChanged();
            }
        }
    }


}
