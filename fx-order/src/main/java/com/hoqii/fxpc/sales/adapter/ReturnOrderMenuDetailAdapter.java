package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.entity.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by akm on 31/05/16.
 */
public class ReturnOrderMenuDetailAdapter extends RecyclerView.Adapter<ReturnOrderMenuDetailAdapter.ViewHolder>{

    private Context context;
    private Product product;
    private boolean isMinLoli = false;
    private List<Product> productList = new ArrayList<Product>();

    public ReturnOrderMenuDetailAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ReturnOrderMenuDetailAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_return_detail_list_activity, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ReturnOrderMenuDetailAdapter.ViewHolder holder, int position) {
        holder.productName.setText("Product : " + productList.get(position).getName());
        holder.productSerialNumber.setText("Serial Number : " + productList.get(position).getSerialNumber());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isMinLoli = true;
        } else {
            isMinLoli = false;
        }

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView productName, productSerialNumber;

        public ViewHolder(View itemView) {
            super(itemView);
            productName = (TextView) itemView.findViewById(R.id.om_name);
            productSerialNumber = (TextView) itemView.findViewById(R.id.om_serial);

        }
    }

    public void addProducts(List<Product> products) {
        productList = products;
        notifyDataSetChanged();
    }
}
