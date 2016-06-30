package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.entity.SalesOrderMenu;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by akm on 30/06/16.
 */
public class SalesReportAdapter extends RecyclerView.Adapter<SalesReportAdapter.ViewHolder> {
    private Context context;
    private List<SalesOrderMenu> salesOrderMenus = new ArrayList<SalesOrderMenu>();

    public SalesReportAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sales_report_adapter, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.textName.setText(salesOrderMenus.get(position).getSalesOrder().getName());
        holder.textReceiptNumber.setText(salesOrderMenus.get(position).getSalesOrder().getReceiptNumber());
        holder.textOrderTo.setText(salesOrderMenus.get(position).getSalesOrder().getSiteFrom().getName());
        holder.textProductName.setText(salesOrderMenus.get(position).getProduct().getName());
        holder.textQty.setText(String.valueOf(salesOrderMenus.get(position).getQty()));
        holder.textTotalPrice.setText(String.valueOf(salesOrderMenus.get(position).getSellPrice()));
        if (salesOrderMenus.get(position).getDescription() != null) {
            holder.textDescription.setText(salesOrderMenus.get(position).getDescription());
        } else {
            holder.textDescription.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return salesOrderMenus.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.text_name)
        TextView textName;
        @InjectView(R.id.text_receipt_number)
        TextView textReceiptNumber;
        @InjectView(R.id.text_orderTo)
        TextView textOrderTo;
        @InjectView(R.id.text_product_name)
        TextView textProductName;
        @InjectView(R.id.text_qty)
        TextView textQty;
        @InjectView(R.id.text_total_price)
        TextView textTotalPrice;
        @InjectView(R.id.text_description)
        TextView textDescription;
        @InjectView(R.id.linear_layout)
        LinearLayout linearLayout;
        @InjectView(R.id.layout)
        LinearLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

    public void addItems(List<SalesOrderMenu> salesOrderMenus) {
        this.salesOrderMenus = salesOrderMenus;
        notifyDataSetChanged();
    }
}
