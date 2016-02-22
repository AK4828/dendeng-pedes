package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.activity.HistoryOrderMenuListActivity;
import com.hoqii.fxpc.sales.activity.SellerOrderListActivity;
import com.hoqii.fxpc.sales.activity.SellerOrderMenuListActivity;
import com.hoqii.fxpc.sales.entity.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 12/6/15.
 */
public class HistoryOrderAdapter extends RecyclerView.Adapter<HistoryOrderAdapter.ViewHolder> {


    private Context context;
    private List<Order> orderList = new ArrayList<Order>();

    public HistoryOrderAdapter(Context context, List<Order> orders) {
        this.context = context;
        this.orderList = orders;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_history_order_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.orderNumber.setText("Order number : "+orderList.get(position).getReceiptNumber());
        holder.orderDate.setText("Tanggal : "+orderList.get(position).getLogInformation().getCreateDate().toString());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, HistoryOrderMenuListActivity.class);
                intent.putExtra("orderId", orderList.get(position).getId());

                context.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView orderNumber, orderDate;

        public ViewHolder(View itemView) {
            super(itemView);
            orderNumber = (TextView) itemView.findViewById(R.id.ol_number);
            orderDate = (TextView) itemView.findViewById(R.id.ol_tgl);
        }
    }


    public void updateItem(List<Order> orders){
        orderList = orders;
        notifyDataSetChanged();
    }


}
