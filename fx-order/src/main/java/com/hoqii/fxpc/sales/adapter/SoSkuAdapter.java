package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.activity.SalesOrderActivity;
import com.hoqii.fxpc.sales.activity.SalesOrderMenuSerialActivity;
import com.hoqii.fxpc.sales.entity.SalesOrderMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by miftakhul on 23/06/16.
 */
public class SoSkuAdapter extends RecyclerView.Adapter<SoSkuAdapter.ViewHolder>{

    private List<SalesOrderMenu> list = new ArrayList<>();
    private Context context;

    public SoSkuAdapter(Context context){
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.adapter_sales_order, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.sku.setText(list.get(position).getProduct().getName());

        holder.more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, SalesOrderMenuSerialActivity.class);
                i.putExtra("SoMenuId", list.get(position).getId());
                ((SalesOrderActivity)context).startActivityForResult(i, SalesOrderActivity.SERIAL_CODE);
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.remove(position);
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void addItem(String SoId, String sku){
        SalesOrderMenu s = new SalesOrderMenu();
        s.setId(UUID.randomUUID().toString());
        s.getProduct().setName(sku);
        s.getSalesOrder().setId(SoId);

        Log.d("sales order menu id ", s.getId());
        Log.d("sales order id ", s.getSalesOrder().getId());

        list.add(s);
        notifyDataSetChanged();
    }

    public List<SalesOrderMenu> getItems(){
        return list;
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView sku;
        TextView serial;
        Button more, delete;

        public ViewHolder(View itemView) {
            super(itemView);
            sku = (TextView) itemView.findViewById(R.id.eu_sku);
//            serial = (TextView) itemView.findViewById(R.id.eu_serial);
            more = (Button) itemView.findViewById(R.id.eu_serial_button);
            delete = (Button) itemView.findViewById(R.id.eu_sku_del);
        }
    }
}

