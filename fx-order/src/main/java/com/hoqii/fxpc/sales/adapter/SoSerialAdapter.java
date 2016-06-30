package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.activity.SalesOrderMenuSerialActivity;
import com.hoqii.fxpc.sales.content.database.adapter.SalesOrderSerialDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.SalesOrderMenuSerial;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 23/06/16.
 */
public class SoSerialAdapter extends RecyclerView.Adapter<SoSerialAdapter.ViewHolder> {

    private List<SalesOrderMenuSerial> serials = new ArrayList<>();
    private Context context;
    private SalesOrderSerialDatabaseAdapter serialDatabaseAdapter;

    public SoSerialAdapter(Context context) {
        this.context = context;
        serialDatabaseAdapter = new SalesOrderSerialDatabaseAdapter(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.adapter_sales_order_serial, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.serial.setText(serials.get(position).getSerialNumber());
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serialDatabaseAdapter.deleteBySerialNumber(serials.get(position).getSerialNumber());
                serials.remove(position);
                notifyDataSetChanged();
                ((SalesOrderMenuSerialActivity)context).skuInfoUpdate();
            }
        });
    }

    @Override
    public int getItemCount() {
        return serials.size();
    }

    public void addItem(SalesOrderMenuSerial salesOrderMenuSerial) {
        if (!serials.contains(serials)) {
            serials.add(salesOrderMenuSerial);
            notifyDataSetChanged();
        }
    }

    public void clear(){
        serials.clear();
        notifyDataSetChanged();
    }

    public List<SalesOrderMenuSerial> getSerials() {
        return serials;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView serial;
        Button delete;

        public ViewHolder(View itemView) {
            super(itemView);
            serial = (TextView) itemView.findViewById(R.id.eu_serial);
            delete = (Button) itemView.findViewById(R.id.eu_del);
        }
    }
}
