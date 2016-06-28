package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.activity.MainActivityMaterial;
import com.hoqii.fxpc.sales.activity.OrderActivity;
import com.hoqii.fxpc.sales.activity.SalesOrderDetailActivity;
import com.hoqii.fxpc.sales.activity.SalesSkuActivity;
import com.hoqii.fxpc.sales.entity.Stock;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.joanzapata.iconify.widget.IconTextView;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by miftakhul on 8/19/15.
 */
public class SalesOrderProductAdapter extends BaseAdapter {
    private Context mcontext;
    private List<Stock> stocks;
    private DecimalFormat decimalFormat = new DecimalFormat("#,###");
    private int mutedColor;
    private boolean isMainActivity = false;
    private SharedPreferences preferences;


    private static LayoutInflater infalter = null;

    public SalesOrderProductAdapter(Context c, List<Stock> stocks) {
        mcontext = c;
        infalter = (LayoutInflater) mcontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.stocks = stocks;
        preferences = c.getSharedPreferences(SignageVariables.PREFS_SERVER, 0);

    }

    @Override
    public int getCount() {
        return stocks.size();
    }

    @Override
    public Object getItem(int position) {
        return stocks.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        final Holder holder = new Holder();
        View itemView = infalter.inflate(R.layout.adapter_product_grid, null);

        holder.title = (TextView) itemView.findViewById(R.id.text_name);
        holder.price = (TextView) itemView.findViewById(R.id.text_price);
        holder.imageView = (ImageView) itemView.findViewById(R.id.image);
        holder.detailLayout = (RelativeLayout) itemView.findViewById(R.id.detail_layout);
        holder.point = (IconTextView) itemView.findViewById(R.id.text_point);

        holder.title.setText(stocks.get(position).getProduct().getName());


        holder.price.setText(mcontext.getResources().getString(R.string.text_currency)+ decimalFormat.format(stocks.get(position).getProduct().getSellPrice()));
        holder.point.setText(stocks.get(position).getProduct().getReward()+" "+mcontext.getResources().getString(R.string.text_point_end)+" {typcn-star-outline}");

        String imageUrl = preferences.getString("server_url", "")+"/api/products/"+stocks.get(position).getProduct().getId() + "/image?access_token="+ AuthenticationUtils.getCurrentAuthentication().getAccessToken();
        Glide.with(mcontext).load(imageUrl).error(R.drawable.ic_description_24dp).into(holder.imageView);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ObjectMapper mapper = SignageApplication.getObjectMapper();
                String jsonProduct = null;
                try {
                    jsonProduct = mapper.writeValueAsString(stocks.get(position).getProduct());
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                Intent intentOrder = new Intent(mcontext, SalesOrderDetailActivity.class);
                intentOrder.putExtra("jsonProduct", jsonProduct);
                intentOrder.putExtra("stockProduct", stocks.get(position).getQty());

                View image = v.findViewById(R.id.image);
                View title = v.findViewById(R.id.text_name);
                View price = v.findViewById(R.id.text_price);

                ((SalesSkuActivity) mcontext).order(intentOrder, image, title, price);

            }
        });

        return itemView;
    }

    public class Holder {

        TextView title, price;
        ImageView imageView;
        RelativeLayout detailLayout;
        IconTextView point;
    }


}
