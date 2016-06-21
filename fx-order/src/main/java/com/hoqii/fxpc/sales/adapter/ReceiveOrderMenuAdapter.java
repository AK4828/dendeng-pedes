package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.content.database.adapter.SerialNumberDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.OrderMenuSerial;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 12/6/15.
 */
public class ReceiveOrderMenuAdapter extends RecyclerView.Adapter<ReceiveOrderMenuAdapter.ViewHolder> {


    private Context context;
    private List<OrderMenuSerial> orderMenuSerialList = new ArrayList<OrderMenuSerial>();
    private List<String> tempSerialNumberList = new ArrayList<String>();
    private SerialNumberDatabaseAdapter serialNumberDatabaseAdapter;
    private boolean verify = false;
    private boolean isMinLoli = false;

    public ReceiveOrderMenuAdapter(Context context, String orderId) {
        this.context = context;

        serialNumberDatabaseAdapter = new SerialNumberDatabaseAdapter(context);
        List<OrderMenuSerial> sn = serialNumberDatabaseAdapter.getSerialNumberListByOrderId(orderId);
        for (OrderMenuSerial s : sn) {
            tempSerialNumberList.add(s.getSerialNumber());
        }

    }

    public ReceiveOrderMenuAdapter(Context context, String orderId, boolean verify) {
        this.context = context;
        this.verify = verify;

        serialNumberDatabaseAdapter = new SerialNumberDatabaseAdapter(context);
        List<OrderMenuSerial> sn = serialNumberDatabaseAdapter.getSerialNumberListByOrderId(orderId);
        for (OrderMenuSerial s : sn) {
            tempSerialNumberList.add(s.getSerialNumber());
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_receive_order_menu_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.productName.setText(context.getString(R.string.holder_product) + orderMenuSerialList.get(position).getOrderMenu().getProduct().getName());
        holder.productSerial.setText(context.getString(R.string.holder_serial) + orderMenuSerialList.get(position).getSerialNumber());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isMinLoli = true;
        } else {
            isMinLoli = false;
        }
        if (tempSerialNumberList.contains(orderMenuSerialList.get(position).getSerialNumber())) {
            holder.imageStatus.setVisibility(View.VISIBLE);
        } else {
            holder.imageStatus.setVisibility(View.GONE);
        }

        if (verify == true) {
            holder.imageStatus.setVisibility(View.VISIBLE);
        }

        if (position == 0) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.setMargins(10, 24, 10, 0);
            holder.layout.setLayoutParams(params);
        } else {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.setMargins(10, 0, 10, 0);
            holder.layout.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() {
        return orderMenuSerialList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView productName, productSerial;
        private ImageView imageStatus;
        private LinearLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
            productName = (TextView) itemView.findViewById(R.id.om_name);
            productSerial = (TextView) itemView.findViewById(R.id.om_serial);
            imageStatus = (ImageView) itemView.findViewById(R.id.ol_img);
            layout = (LinearLayout) itemView.findViewById(R.id.layout);
        }
    }

    public void addItems(List<OrderMenuSerial> orderMenuSerials) {
        for (OrderMenuSerial s : orderMenuSerials) {
            if (!orderMenuSerialList.contains(s)) {
                orderMenuSerialList.add(s);
            }
        }
        if (tempSerialNumberList.size() == orderMenuSerialList.size()) {
            this.verify = true;
            Log.d(getClass().getSimpleName(), "verify true");
        }
        notifyDataSetChanged();
        Log.d(getClass().getSimpleName(), "adapter updated");
    }

    public boolean isVerify() {
        return verify;
    }

    public void setVerify(boolean verify) {
        this.verify = verify;
    }

}
