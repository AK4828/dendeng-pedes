package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.entity.OrderMenuSerial;
import com.hoqii.fxpc.sales.fragment.ShipmentMenuListFragment;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.joanzapata.iconify.widget.IconTextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 12/6/15.
 */
public class ShipmentHistoryMenuAdapter extends RecyclerView.Adapter<ShipmentHistoryMenuAdapter.ViewHolder> {


    private Context context;
    private List<OrderMenuSerial> orderMenuSerialList = new ArrayList<OrderMenuSerial>();
    private SharedPreferences preferences;
    private ShipmentMenuListFragment f;

    public ShipmentHistoryMenuAdapter(Context context, ShipmentMenuListFragment f) {
        this.context = context;
        this.f = f;

        preferences = context.getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_shipment_menu, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.productName.setText(orderMenuSerialList.get(position).getOrderMenu().getProduct().getName());
        holder.description.setText(orderMenuSerialList.get(position).getOrderMenu().getProduct().getDescription());
        holder.serial.setText(context.getResources().getString(R.string.holder_serial)+ orderMenuSerialList.get(position).getSerialNumber());

        String imageUrl = preferences.getString("server_url", "")+"/api/products/"+ orderMenuSerialList.get(position).getOrderMenu().getProduct().getId() + "/image?access_token="+ AuthenticationUtils.getCurrentAuthentication().getAccessToken();

        Glide.with(context).load(imageUrl).error(R.drawable.no_image).into(holder.preview);

        if (position == 0){
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.setMargins(10,24,10,0);
            holder.layout.setLayoutParams(params);
        }else{
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.setMargins(10,0,10,0);
            holder.layout.setLayoutParams(params);
        }

        if (position == orderMenuSerialList.size() - 1){
            f.loadMoreContent();
        }
    }

    @Override
    public int getItemCount() {
        return orderMenuSerialList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView productName, description, serial;
        private IconTextView count;
        private ImageView preview;
        private LinearLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
            productName = (TextView) itemView.findViewById(R.id.om_name);
            description = (TextView) itemView.findViewById(R.id.om_description);
            serial = (TextView) itemView.findViewById(R.id.om_serial);
            preview = (ImageView) itemView.findViewById(R.id.om_preview);
            layout = (LinearLayout) itemView.findViewById(R.id.layout);
        }
    }

    public void addItems(List<OrderMenuSerial> orderMenuSerials){
        for (OrderMenuSerial s : orderMenuSerials){
            orderMenuSerialList.add(s);
            notifyDataSetChanged();
        }
    }


}
