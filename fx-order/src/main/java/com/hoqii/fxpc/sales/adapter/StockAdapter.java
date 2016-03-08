package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.activity.MainActivity;
import com.hoqii.fxpc.sales.content.database.adapter.OrderMenuDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.entity.Stock;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.hoqii.fxpc.sales.util.ImageUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 2/23/16.
 */
public class StockAdapter extends RecyclerView.Adapter<StockAdapter.ViewHolder>{

    private Context context;
    private List<Stock> stockList = new ArrayList<Stock>();
    private SharedPreferences preferences;

    public StockAdapter(Context context){
        this.context = context;
        preferences = context.getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_stock, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.productName.setText(stockList.get(position).getProduct().getName());
        holder.productQty.setText(Integer.toString(stockList.get(position).getQty()));
        if (stockList.get(position).getProduct().getParentCategory().getId() != null){
            holder.productCategory.setText(stockList.get(position).getProduct().getParentCategory().getName());
        }else {
            holder.productCategory.setText("Uncategorized");
        }
        if (stockList.get(position).getProduct().getDescription().toString().equalsIgnoreCase("null")){
            holder.productDescription.setText("No description");
        } else {
            holder.productDescription.setText(stockList.get(position).getProduct().getDescription());
        }

        String imageUrl = preferences.getString("server_url", "")+"/api/products/"+stockList.get(position).getProduct().getId() + "/image?access_token="+ AuthenticationUtils.getCurrentAuthentication().getAccessToken();
        Glide.with(context).load(imageUrl).error(R.drawable.no_image).into(holder.preview);

        if (position == 0){
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.setMargins(8,24,8,2);
            holder.layout.setLayoutParams(params);
        }else {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.setMargins(8,0,8,2);
            holder.layout.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView productName, productCategory, productQty, productDescription;
        ImageView preview;
        RelativeLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
            productName = (TextView) itemView.findViewById(R.id.text_name);
            productCategory = (TextView) itemView.findViewById(R.id.text_category);
            productQty = (TextView) itemView.findViewById(R.id.text_qty);
            productDescription = (TextView) itemView.findViewById(R.id.text_description);
            preview = (ImageView) itemView.findViewById(R.id.img_preview);
            layout = (RelativeLayout) itemView.findViewById(R.id.layout);
        }
    }

    public void addItems(List<Stock> stocks){
        this.stockList = stocks;
        notifyDataSetChanged();
    }
}
