package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.activity.SelfHistoryOrderMenuListActivity;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.entity.SerialNumber;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 12/6/15.
 */
public class SelfHistoryOrderMenuAdapter extends RecyclerView.Adapter<SelfHistoryOrderMenuAdapter.ViewHolder> {


    private Context context;
    private String orderId;
    private List<OrderMenu> orderMenuList = new ArrayList<OrderMenu>();
    private ImageLoader imageLoader = ImageLoader.getInstance();
    private SharedPreferences preferences;

    public SelfHistoryOrderMenuAdapter(Context context, String orderId) {
        this.context = context;
        this.orderId = orderId;


        if (!imageLoader.isInited()) {
            imageLoader.init(ImageLoaderConfiguration.createDefault(context));
        }

        preferences = context.getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
    }

    public SelfHistoryOrderMenuAdapter(Context context, List<OrderMenu> orderMenus) {
        this.context = context;
        this.orderMenuList = orderMenus;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_self_history_order_menu_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.productName.setText("Product : " + orderMenuList.get(position).getProduct().getName());
        holder.productCount.setText("Jumlah order : " + Integer.toString(orderMenuList.get(position).getQtyOrder()));

        String imageUrl = preferences.getString("server_url", "")+"/api/products/"+orderMenuList.get(position).getProduct().getId() + "/image?access_token="+ AuthenticationUtils.getCurrentAuthentication().getAccessToken();

        imageLoader.displayImage(imageUrl, holder.preview, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                holder.preview.setImageResource(R.drawable.no_image);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                holder.preview.setImageResource(R.drawable.no_image);
            }
        });

        if (position == orderMenuList.size() - 1){
            ((SelfHistoryOrderMenuListActivity)context).loadMoreContent();
        }
    }

    @Override
    public int getItemCount() {
        return orderMenuList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView productName, productCount;
        private ImageView imageStatus, preview;
        private Spinner newCount;

        public ViewHolder(View itemView) {
            super(itemView);
            productName = (TextView) itemView.findViewById(R.id.om_name);
            productCount = (TextView) itemView.findViewById(R.id.om_count);
            imageStatus = (ImageView) itemView.findViewById(R.id.ol_img);
            preview = (ImageView) itemView.findViewById(R.id.ol_preview);
//            newCount = (Spinner) itemView.findViewById(R.id.om_spin_new_count);
        }
    }

    public void addItems(List<OrderMenu> orderMenus){
        for (OrderMenu o : orderMenus){
            if (!orderMenuList.contains(o)) {
                orderMenuList.add(o);
                notifyDataSetChanged();
            }
        }
    }

}
