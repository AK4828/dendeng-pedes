package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.activity.MainActivity;
import com.hoqii.fxpc.sales.activity.SalesOrderActivity;
import com.hoqii.fxpc.sales.activity.SalesOrderMenuActivity;
import com.hoqii.fxpc.sales.activity.SalesOrderMenuSerialActivity;
import com.hoqii.fxpc.sales.content.database.adapter.SalesOrderMenuDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.SalesOrderSerialDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.entity.SalesOrderMenu;
import com.hoqii.fxpc.sales.entity.SalesOrderMenuSerial;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by miftakhul on 23/06/16.
 */
public class SoSkuAdapter extends RecyclerView.Adapter<SoSkuAdapter.ViewHolder>{

    private List<SalesOrderMenu> list = new ArrayList<>();
    private Context context;
    private SharedPreferences preferences;
    private SalesOrderMenuDatabaseAdapter databaseAdapter;
    private SalesOrderSerialDatabaseAdapter serialDatabaseAdapter;

    public SoSkuAdapter(Context context){
        this.context = context;
        preferences = context.getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        databaseAdapter = new SalesOrderMenuDatabaseAdapter(context);
        serialDatabaseAdapter = new SalesOrderSerialDatabaseAdapter(context);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.adapter_sales_order, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        String imageUrl = preferences.getString("server_url", "") + "/api/products/" + list.get(position).getProduct().getId() + "/image?access_token=" + AuthenticationUtils.getCurrentAuthentication().getAccessToken();
        Glide.with(context).load(imageUrl).error(R.drawable.ic_description_24dp).into(holder.preview);

        holder.sku.setText(list.get(position).getProduct().getName());
        holder.qty.setText("Quantity : "+Integer.toString(list.get(position).getQtySalesOrder()));

        int qty = list.get(position).getQtySalesOrder();
        List<SalesOrderMenuSerial> sn = serialDatabaseAdapter.getSerialNumberListBySalesOrderMenuId(list.get(position).getId());
        final List<SalesOrderMenuSerial> allSerial = sn;
        holder.serialQty.setText(
                context.getString(R.string.text_already_inputted)+allSerial.size()+
                        context.getResources().getString(R.string.text_verivy_of_total)+qty);

        holder.btnSerial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                optionDialog(position, list.get(position).getId());
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void addItems(SalesOrderMenu salesOrderMenu){
        if (!list.contains(salesOrderMenu)){
            list.add(salesOrderMenu);
        }
        notifyDataSetChanged();
    }

    public void clear(){
        list.clear();
        notifyDataSetChanged();
    }

    public List<SalesOrderMenu> getItems(){
        return list;
    }

    private void confirmDialog(final int location, final String salesOrderMenuId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.message_title_confirmation));
        builder.setMessage(context.getString(R.string.message_delete_order));
        builder.setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                list.remove(location);
                notifyItemRemoved(location);
                databaseAdapter.deleteSalesOrderMenu(salesOrderMenuId);
                ((SalesOrderMenuActivity)context).refresh();
            }
        });
        builder.setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void optionDialog(final int location, final String salesOrderMenuId){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.message_title_confirmation));
        builder.setItems(new String[]{context.getString(R.string.text_add_serial_number), context.getString(R.string.text_edit), context.getString(R.string.text_delete)}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0){
                    Intent i = new Intent(context, SalesOrderMenuSerialActivity.class);
                    i.putExtra("salesOrderMenuId", list.get(location).getId());
                    i.putExtra("productId", list.get(location).getProduct().getId());
                    i.putExtra("productName", list.get(location).getProduct().getName());
                    i.putExtra("productQty", list.get(location).getQty());
                    ((SalesOrderMenuActivity)context).startActivityForResult(i, SalesOrderActivity.SERIAL_CODE);
                } else if (which == 1) {
                    SalesOrderMenu salesOrderMenu = databaseAdapter.getSalesOrderMenuById(salesOrderMenuId);
                    String jsonProduct = null;
                    ObjectMapper mapper = SignageApplication.getObjectMapper();
                    try {
                        jsonProduct = mapper.writeValueAsString(salesOrderMenu.getProduct());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    int qty = salesOrderMenu.getQtySalesOrder();
                    ((SalesOrderMenuActivity) context).orderUpdate(jsonProduct, qty);
                } else if (which == 2) {
                    confirmDialog(location, salesOrderMenuId);
                }
            }
        });
        builder.show();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        TextView sku,qty;
        TextView serialQty;
        ImageView preview;
        Button btnSerial;

        public ViewHolder(View itemView) {
            super(itemView);
            sku = (TextView) itemView.findViewById(R.id.eu_sku);
            qty = (TextView) itemView.findViewById(R.id.eu_qty);
            preview = (ImageView) itemView.findViewById(R.id.eu_sku_img);
            serialQty = (TextView) itemView.findViewById(R.id.eu_serial_qty);
            btnSerial = (Button) itemView.findViewById(R.id.eu_serial_button);
        }
    }
}

