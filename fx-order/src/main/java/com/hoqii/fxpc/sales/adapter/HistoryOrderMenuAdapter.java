package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.content.Intent;
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
import com.hoqii.fxpc.sales.activity.ScannerActivityCustom;
import com.hoqii.fxpc.sales.activity.SellerOrderMenuListActivity;
import com.hoqii.fxpc.sales.content.database.adapter.SerialNumberDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.entity.SerialNumber;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 12/6/15.
 */
public class HistoryOrderMenuAdapter extends RecyclerView.Adapter<HistoryOrderMenuAdapter.ViewHolder> {


    private Context context;
    private List<OrderMenu> orderMenuList = new ArrayList<OrderMenu>();

    public HistoryOrderMenuAdapter(Context context,  List<OrderMenu> orderMenus) {
        this.context = context;
        this.orderMenuList = orderMenus;

    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_history_order_menu_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.productName.setText("Product : " + orderMenuList.get(position).getProduct().getName());
        holder.productCount.setText("Jumlah order : " + Integer.toString(orderMenuList.get(position).getQty()));

    }

    @Override
    public int getItemCount() {
        return orderMenuList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView productName, productCount;
        private ImageView imageStatus;
        private Spinner newCount;

        public ViewHolder(View itemView) {
            super(itemView);
            productName = (TextView) itemView.findViewById(R.id.om_name);
            productCount = (TextView) itemView.findViewById(R.id.om_count);
            imageStatus = (ImageView) itemView.findViewById(R.id.ol_img);
//            newCount = (Spinner) itemView.findViewById(R.id.om_spin_new_count);
        }
    }

    public void updateItem(List<OrderMenu> orderMenus){
        orderMenuList = orderMenus;
        notifyDataSetChanged();
    }





}
