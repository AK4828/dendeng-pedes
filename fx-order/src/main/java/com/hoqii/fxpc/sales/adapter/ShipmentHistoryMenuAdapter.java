package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.entity.SerialNumber;
import com.hoqii.fxpc.sales.fragment.ShipmentMenuListFragment;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.joanzapata.iconify.widget.IconTextView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 12/6/15.
 */
public class ShipmentHistoryMenuAdapter extends RecyclerView.Adapter<ShipmentHistoryMenuAdapter.ViewHolder> {


    private Context context;
    private List<SerialNumber> serialNumberList = new ArrayList<SerialNumber>();
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
        holder.productName.setText(serialNumberList.get(position).getOrderMenu().getProduct().getName());
        holder.description.setText(serialNumberList.get(position).getOrderMenu().getProduct().getDescription());
        holder.serial.setText("Serial : "+serialNumberList.get(position).getSerialNumber());

        String imageUrl = preferences.getString("server_url", "")+"/api/products/"+serialNumberList.get(position).getOrderMenu().getProduct().getId() + "/image?access_token="+ AuthenticationUtils.getCurrentAuthentication().getAccessToken();

        Glide.with(context).load(imageUrl).error(R.drawable.no_image).into(holder.preview);


        if (position == serialNumberList.size() - 1){
            f.loadMoreContent();
        }
    }

    @Override
    public int getItemCount() {
        return serialNumberList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView productName, description, serial;
        private IconTextView count;
        private ImageView preview;

        public ViewHolder(View itemView) {
            super(itemView);
            productName = (TextView) itemView.findViewById(R.id.om_name);
            description = (TextView) itemView.findViewById(R.id.om_description);
            serial = (TextView) itemView.findViewById(R.id.om_serial);
            preview = (ImageView) itemView.findViewById(R.id.om_preview);
        }
    }

    public void addItems(List<SerialNumber> serialNumbers){
        for (SerialNumber s : serialNumbers){
            serialNumberList.add(s);
            notifyDataSetChanged();
        }
    }


}
