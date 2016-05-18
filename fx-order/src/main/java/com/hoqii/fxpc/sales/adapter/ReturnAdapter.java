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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.activity.ReturnDetailActivity;
import com.hoqii.fxpc.sales.activity.ReturnListActivity;
import com.hoqii.fxpc.sales.entity.Retur;
import com.hoqii.fxpc.sales.entity.Shipment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by akm on 14/04/16.
 */
public class ReturnAdapter extends RecyclerView.Adapter<ReturnAdapter.ViewHolder> {

    private Context context;
    private List<Retur> returList = new ArrayList<Retur>();

    public ReturnAdapter(Context context) {
        this.context = context;

    }

    public ReturnAdapter(Context context, List<Retur> returs) {
        this.context = context;
        this.returList = returs;
    }


    @Override
    public ReturnAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_return_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ReturnAdapter.ViewHolder holder, final int position) {

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy / hh:mm:ss");
        Date date = new Date();
        date.setTime(returList.get(position).getLogInformation().getCreateDate().getTime());

        Date orderDate = new Date();
        orderDate.setTime(returList.get(position).getSerialNumber().getOrderMenu().getOrder().getLogInformation().getCreateDate().getTime());

        holder.site.setText(context.getResources().getString(R.string.text_receive_from)+ returList.get(position).getSerialNumber().getOrderMenu().getOrder().getSite().getName());
        holder.shipmentDate.setText(context.getResources().getString(R.string.text_date)+ simpleDateFormat.format(date));
        holder.orderNumber.setText(context.getResources().getString(R.string.text_order_receipt)+ returList.get(position).getSerialNumber().getOrderMenu().getOrder().getReceiptNumber());
        holder.orderDate.setText(context.getResources().getString(R.string.text_order_date)+ simpleDateFormat.format(orderDate));

        switch (returList.get(position).getSerialNumber().getShipment().getStatus()){
            case WAIT:
                holder.statusDelivery.setVisibility(View.GONE);
                break;
            case DELIVERED:
                holder.statusDelivery.setVisibility(View.VISIBLE);
                break;
            default:
                holder.statusDelivery.setVisibility(View.GONE);
                break;
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ReturnDetailActivity.class);
                intent.putExtra("receiveId", returList.get(position).getId());
                intent.putExtra("receiveDate", returList.get(position).getLogInformation().getCreateDate().getTime());
                intent.putExtra("orderId", returList.get(position).getSerialNumber().getOrderMenu().getOrder().getId());
                intent.putExtra("orderReceipt", returList.get(position).getSerialNumber().getOrderMenu().getOrder().getReceiptNumber());
                intent.putExtra("orderDate", returList.get(position).getSerialNumber().getOrderMenu().getOrder().getLogInformation().getCreateDate().getTime());
                intent.putExtra("shipmentId", returList.get(position).getSerialNumber().getShipment().getId());
                intent.putExtra("site", returList.get(position).getSerialNumber().getOrderMenu().getOrder().getSite().getName());
                intent.putExtra("siteDescription", returList.get(position).getSerialNumber().getOrderMenu().getOrder().getSite().getDescription());

                ObjectMapper om = SignageApplication.getObjectMapper();
                try {
                    String jsonReceive = om.writeValueAsString(returList.get(position));
                    intent.putExtra("jsonReceive", jsonReceive);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                ((ReturnListActivity)context).openReceiveDetail(intent);
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

        if (position == returList.size() -1){
            ((ReturnListActivity)context).loadMoreContent();
        }

    }

    @Override
    public int getItemCount() {
        return returList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView orderNumber, orderDate, shipmentDate, site, statusDelivery;
        private LinearLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
            orderNumber = (TextView) itemView.findViewById(R.id.rl_number);
            orderDate = (TextView) itemView.findViewById(R.id.rl_tgl);
            shipmentDate = (TextView) itemView.findViewById(R.id.rl_tgl_receive);
            site = (TextView) itemView.findViewById(R.id.rl_site);
            statusDelivery = (TextView) itemView.findViewById(R.id.rl_delivery);
            layout = (LinearLayout) itemView.findViewById(R.id.layout);
        }
    }

    public void addItems(List<Retur> returs){
        for (Retur r : returs){
            if (!returList.contains(r)){
                returList.add(r);
                notifyDataSetChanged();
            }
        }
    }

    public void updateStatusDelivered(String receiveId){
        Log.d(getClass().getSimpleName(), "[ mencari receive berdasarkan id "+receiveId+" ]");
        for (int x = 0; x < returList.size(); x++){
            if (returList.get(x).getId().equalsIgnoreCase(receiveId)){
                Log.d(getClass().getSimpleName(), "[ data id "+receiveId+" ditemukan]");
                returList.get(x).getSerialNumber().getShipment().setStatus(Shipment.ShipmentStatus.DELIVERED);
                notifyItemChanged(x);
            }
        }
    }
}
