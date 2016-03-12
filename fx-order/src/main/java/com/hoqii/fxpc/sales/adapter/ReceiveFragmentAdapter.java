package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.activity.ReceiveDetailActivity;
import com.hoqii.fxpc.sales.entity.Receive;
import com.hoqii.fxpc.sales.entity.Shipment;
import com.hoqii.fxpc.sales.fragment.ReceiveListFragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by miftakhul on 12/6/15.
 */
public class ReceiveFragmentAdapter extends RecyclerView.Adapter<ReceiveFragmentAdapter.ViewHolder> {


    private Context context;
    private List<Receive> receiveList = new ArrayList<Receive>();
    private Fragment fragment;

    public ReceiveFragmentAdapter(Context context, Fragment fragment) {
        this.context = context;
        this.fragment = fragment;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_receive_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy / hh:mm:ss");
        Date date = new Date();
        date.setTime(receiveList.get(position).getLogInformation().getCreateDate().getTime());

        Date orderDate = new Date();
        orderDate.setTime(receiveList.get(position).getOrder().getLogInformation().getCreateDate().getTime());

        holder.site.setText(context.getResources().getString(R.string.text_receive_from)+ receiveList.get(position).getOrder().getSite().getName());
        holder.shipmentDate.setText(context.getResources().getString(R.string.text_date)+ simpleDateFormat.format(date));
        holder.orderNumber.setText(context.getResources().getString(R.string.text_order_receipt)+ receiveList.get(position).getOrder().getReceiptNumber());
        holder.orderDate.setText(context.getResources().getString(R.string.text_order_date)+ simpleDateFormat.format(orderDate));

        switch (receiveList.get(position).getShipment().getStatus()){
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
                Intent intent = new Intent(context, ReceiveDetailActivity.class);
                intent.putExtra("receiveId", receiveList.get(position).getId());
                intent.putExtra("receiveDate", receiveList.get(position).getLogInformation().getCreateDate().getTime());
                intent.putExtra("orderId", receiveList.get(position).getOrder().getId());
                intent.putExtra("orderReceipt", receiveList.get(position).getOrder().getReceiptNumber());
                intent.putExtra("orderDate", receiveList.get(position).getOrder().getLogInformation().getCreateDate().getTime());
                intent.putExtra("shipmentId", receiveList.get(position).getShipment().getId());
                intent.putExtra("site", receiveList.get(position).getOrder().getSite().getName());
                intent.putExtra("siteDescription", receiveList.get(position).getOrder().getSite().getDescription());

                ObjectMapper om = SignageApplication.getObjectMapper();
                try {
                    String jsonReceive = om.writeValueAsString(receiveList.get(position));
                    intent.putExtra("jsonReceive", jsonReceive);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }


//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//
//                    View layoutView = holder.itemView.findViewById(R.id.card_layout);
//                    String transitionName = context.getString(R.string.transition_layout);
//
//                    ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((ReceiveListActivity) context, layoutView, transitionName);
//                    context.startActivity(intent, optionsCompat.toBundle());
//
//                } else {
//                    context.startActivity(intent);
//                }
                ((ReceiveListFragment)fragment).openReceiveDetail(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return receiveList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView orderNumber, orderDate, shipmentDate, site, statusDelivery;

        public ViewHolder(View itemView) {
            super(itemView);
            orderNumber = (TextView) itemView.findViewById(R.id.rl_number);
            orderDate = (TextView) itemView.findViewById(R.id.rl_tgl);
            shipmentDate = (TextView) itemView.findViewById(R.id.rl_tgl_receive);
            site = (TextView) itemView.findViewById(R.id.rl_site);
            statusDelivery = (TextView) itemView.findViewById(R.id.rl_delivery);
        }
    }


    public void removeItem(String receiveId){
        Log.d(getClass().getSimpleName(), "Searching for " + receiveId);
        for (int x = 0; x < receiveList.size(); x++){
            if (receiveList.get(x).getId().equalsIgnoreCase(receiveId)){
                Log.d(getClass().getSimpleName(), "data contains " + receiveId);
                receiveList.remove(x);
                notifyItemRemoved(x);
                notifyDataSetChanged();
            }else {
                Log.d(getClass().getSimpleName(), "data not contains " + receiveId);
            }
        }
    }

    public void addItems(List<Receive> receives){
        for (Receive r : receives){
            if (!receiveList.contains(r)){
                receiveList.add(r);
                notifyDataSetChanged();
            }
        }
    }

    public void updateStatusDelivered(String receiveId){
        Log.d(getClass().getSimpleName(), "[ mencari receive berdasarkan id "+receiveId+" ]");
        for (int x= 0; x < receiveList.size(); x++){
            if (receiveList.get(x).getId().equalsIgnoreCase(receiveId)){
                Log.d(getClass().getSimpleName(), "[ data id "+receiveId+" ditemukan]");
                receiveList.get(x).getShipment().setStatus(Shipment.ShipmentStatus.DELIVERED);
                notifyItemChanged(x);
            }
        }
    }

}
