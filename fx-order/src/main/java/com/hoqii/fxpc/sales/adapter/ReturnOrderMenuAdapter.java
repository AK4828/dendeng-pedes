package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.content.database.adapter.ReturnDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SerialNumberDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.Retur;
import com.hoqii.fxpc.sales.entity.SerialNumber;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by akm on 19/04/16.
 */
public class ReturnOrderMenuAdapter extends RecyclerView.Adapter<ReturnOrderMenuAdapter.ViewHolder> {

    private Context context;
    private List<SerialNumber> serialNumberList = new ArrayList<SerialNumber>();
    private List<SerialNumber> serialNumbers = new ArrayList<SerialNumber>();
    private List<String> tempSerialNumberList = new ArrayList<String>();
    private SerialNumberDatabaseAdapter serialNumberDatabaseAdapter;
    private ReturnDatabaseAdapter returnDatabaseAdapter;
    private boolean verify = false;
    private boolean isMinLoli = false;
    private SerialNumber serialNumber;

    public ReturnOrderMenuAdapter(Context context, String orderId) {
        this.context = context;

        serialNumberDatabaseAdapter = new SerialNumberDatabaseAdapter(context);
        returnDatabaseAdapter = new ReturnDatabaseAdapter(context);
        List<SerialNumber> sn = serialNumberDatabaseAdapter.getSerialNumberListByOrderId(orderId);
        for (SerialNumber s : sn) {
            tempSerialNumberList.add(s.getSerialNumber());
        }

    }

    public ReturnOrderMenuAdapter(Context context, String orderId, boolean verify) {
        this.context = context;
        this.verify = verify;

        serialNumberDatabaseAdapter = new SerialNumberDatabaseAdapter(context);
        List<SerialNumber> sn = serialNumberDatabaseAdapter.getSerialNumberListByOrderId(orderId);
        for (SerialNumber s : sn) {
            tempSerialNumberList.add(s.getSerialNumber());
        }
    }

    @Override
    public ReturnOrderMenuAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_return_order_menu_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ReturnOrderMenuAdapter.ViewHolder holder, final int position) {
        holder.productName.setText(context.getString(R.string.holder_product) + serialNumberList.get(position).getOrderMenu().getProduct().getName());
        holder.productSerial.setText(context.getString(R.string.holder_serial) + serialNumberList.get(position).getSerialNumber());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isMinLoli = true;
        } else {
            isMinLoli = false;
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Retur");
                builder.setMessage("Are you sure to return this item ?");
                View view = LayoutInflater.from(context).inflate(R.layout.view_return_desc, null);
                final TextView orderDesc = (TextView) view.findViewById(R.id.order_desc);
                orderDesc.setText("");
                builder.setView(view);
                builder.setTitle("Return Items");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Retur retur = new Retur();
                        serialNumber = new SerialNumber();
                        retur.setStatus(Retur.ReturnStatus.RETURNED);
                        retur.setRecipient("");
                        retur.setSender("");
                        retur.setDescription(orderDesc.getText().toString());
                        serialNumber.setSerialNumber("HAHAHAHAHAHAH");
                        retur.setSerialNumber(serialNumber);
                        returnDatabaseAdapter.saveReturn(retur);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
        if (tempSerialNumberList.contains(serialNumberList.get(position).getSerialNumber())) {
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
        return serialNumberList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
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

    public void addItems(List<SerialNumber> serialNumbers) {
        for (SerialNumber s : serialNumbers) {
            if (!serialNumberList.contains(s)) {
                serialNumberList.add(s);
            }
        }
        if (tempSerialNumberList.size() == serialNumberList.size()) {
            this.verify = true;
        }
        notifyDataSetChanged();
    }

    public void addItem(SerialNumber serialNumber){
        serialNumbers.add(serialNumber);
        Log.d("CEK VALUE", serialNumber.getOrderMenu().getProduct().getName());
    }

}
