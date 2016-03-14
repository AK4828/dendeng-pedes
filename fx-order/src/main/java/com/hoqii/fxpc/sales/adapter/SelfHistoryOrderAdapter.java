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
import com.hoqii.fxpc.sales.activity.SelfHistoryOrderListActivity;
import com.hoqii.fxpc.sales.activity.SelfHistoryOrderMenuListActivity;
import com.hoqii.fxpc.sales.entity.Order;
import com.joanzapata.iconify.widget.IconTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by miftakhul on 12/6/15.
 */
public class SelfHistoryOrderAdapter extends RecyclerView.Adapter<SelfHistoryOrderAdapter.ViewHolder> {


    private Context context;
    private List<Order> orderList = new ArrayList<Order>();

    public SelfHistoryOrderAdapter(Context context) {
        this.context = context;
    }

    public SelfHistoryOrderAdapter(Context context, List<Order> orders) {
        this.context = context;
        this.orderList = orders;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_self_history_order_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy / hh:mm:ss");
        Date date = new Date();
        date.setTime(orderList.get(position).getLogInformation().getCreateDate().getTime());

        holder.siteTo.setText(orderList.get(position).getSite().getName());
        holder.orderNumber.setText(orderList.get(position).getReceiptNumber());
        holder.orderDate.setText(context.getResources().getString(R.string.adapter_date) + simpleDateFormat.format(date));
        holder.email.setText(orderList.get(position).getSite().getEmail());

        switch (orderList.get(position).getStatus()){
            case PROCESSED:
                holder.statusProcessed.setTextColor(context.getResources().getColor(R.color.colorAccent));
                holder.statusSending.setTextColor(context.getResources().getColor(R.color.grey));
                holder.statusReceived.setTextColor(context.getResources().getColor(R.color.grey));
                break;
            case SENDING:
                holder.statusProcessed.setTextColor(context.getResources().getColor(R.color.colorAccent));
                holder.statusSending.setTextColor(context.getResources().getColor(R.color.colorAccent));
                holder.statusReceived.setTextColor(context.getResources().getColor(R.color.grey));
                break;
            case RECEIVED:
                holder.statusProcessed.setTextColor(context.getResources().getColor(R.color.colorAccent));
                holder.statusSending.setTextColor(context.getResources().getColor(R.color.colorAccent));
                holder.statusReceived.setTextColor(context.getResources().getColor(R.color.colorAccent));
                break;
            default:
                holder.statusProcessed.setTextColor(context.getResources().getColor(R.color.grey));
                holder.statusSending.setTextColor(context.getResources().getColor(R.color.grey));
                holder.statusReceived.setTextColor(context.getResources().getColor(R.color.grey));
                break;
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SelfHistoryOrderMenuListActivity.class);
                intent.putExtra("orderId", orderList.get(position).getId());
                intent.putExtra("orderDate", orderList.get(position).getLogInformation().getCreateDate().getTime());
                Log.d("date send ", Long.toString(orderList.get(position).getLogInformation().getCreateDate().getTime()));
                intent.putExtra("orderReceipt", orderList.get(position).getReceiptNumber());
                intent.putExtra("siteEmail", orderList.get(position).getSite().getEmail());
                intent.putExtra("siteName", orderList.get(position).getSite().getName());

                View orderNumber = holder.orderNumber;
                View orderDate = holder.orderDate;

                ((SelfHistoryOrderListActivity) context).openOrderMenuActivity(intent, orderNumber, orderDate);

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
            ((SelfHistoryOrderListActivity)context).loadMoreContent();
        }
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private IconTextView orderNumber, orderDate, email, statusProcessed, statusSending, statusReceived;
        private TextView siteTo;
        private LinearLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
            siteTo = (TextView) itemView.findViewById(R.id.siteTo);
            orderNumber = (IconTextView) itemView.findViewById(R.id.ol_number);
            orderDate = (IconTextView) itemView.findViewById(R.id.ol_tgl);
            email = (IconTextView) itemView.findViewById(R.id.ol_email);
            statusProcessed = (IconTextView) itemView.findViewById(R.id.ol_processed);
            statusSending = (IconTextView) itemView.findViewById(R.id.ol_sending);
            statusReceived = (IconTextView) itemView.findViewById(R.id.ol_received);
            layout = (LinearLayout) itemView.findViewById(R.id.layout);
        }
    }

    public void removeItem(int position) {
        orderList.remove(position);
        notifyItemRemoved(position);
    }

    public void addItems(List<Order> order){
        for (Order o : order){
            if (!orderList.contains(o)){
                orderList.add(o);
                notifyDataSetChanged();
            }
        }
    }


}
