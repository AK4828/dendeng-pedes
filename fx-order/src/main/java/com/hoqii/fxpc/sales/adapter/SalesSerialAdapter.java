package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.activity.ScannerActivityCustom;
import com.hoqii.fxpc.sales.activity.ScannerReceiveActivityCustom;
import com.hoqii.fxpc.sales.activity.ScannerSalesActivityCustom;
import com.hoqii.fxpc.sales.content.database.adapter.SalesOrderSerialDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SerialNumberDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.OrderMenuSerial;
import com.hoqii.fxpc.sales.entity.SalesOrderMenuSerial;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 12/6/15.
 */
public class SalesSerialAdapter extends RecyclerView.Adapter<SalesSerialAdapter.ViewHolder>{

    private Context context;
    private List<SalesOrderMenuSerial> orderMenuSerialNumbersList = new ArrayList<SalesOrderMenuSerial>();
    private String productName;
    private SalesOrderSerialDatabaseAdapter serialNumberDatabaseAdapter;

    public SalesSerialAdapter(Context context, String productName){
        this.context = context;
        this.productName = productName;

        serialNumberDatabaseAdapter = new SalesOrderSerialDatabaseAdapter(context);
    }



    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_serial_number, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if (productName != null ){
            holder.productName.setText(productName);
        }else {
            holder.productName.setText(orderMenuSerialNumbersList.get(position).getSalesOrderMenu().getProduct().getName());
        }

        holder.productSerialNumber.setText(orderMenuSerialNumbersList.get(position).getSerialNumber());

        holder.delteSerial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serialNumberDatabaseAdapter.deleteBySerialNumber(orderMenuSerialNumbersList.get(position).getSerialNumber());
                orderMenuSerialNumbersList.remove(position);
                notifyItemRemoved(position);
                notifyDataSetChanged();

                if (productName != null){
                    ((ScannerSalesActivityCustom) context).scannedCount();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderMenuSerialNumbersList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        private TextView productName, productSerialNumber;
        private ImageButton delteSerial;

        public ViewHolder(View itemView) {
            super(itemView);

            productName = (TextView)itemView.findViewById(R.id.product_name);
            productSerialNumber = (TextView)itemView.findViewById(R.id.product_serial_number);
            delteSerial = (ImageButton) itemView.findViewById(R.id.delete_serial);
        }
    }


    public void addSerialNumber(SalesOrderMenuSerial orderMenuSerial){
        orderMenuSerialNumbersList.add(orderMenuSerial);
        notifyItemInserted(orderMenuSerialNumbersList.size());
    }

    public List<SalesOrderMenuSerial> getSerialNumber(){
        return orderMenuSerialNumbersList;
    }
}
