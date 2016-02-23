package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.activity.MainActivity;
import com.hoqii.fxpc.sales.content.database.adapter.OrderMenuDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.util.ImageUtil;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 2/23/16.
 */
public class OrderMenuAdapter extends RecyclerView.Adapter<OrderMenuAdapter.ViewHolder>{

    private Context context;
    private List<OrderMenu> orderMenuList = new ArrayList<OrderMenu>();
    private DecimalFormat decimalFormat = new DecimalFormat("#,###");
    private OrderMenuDatabaseAdapter orderMenuDbAdapter;
    private ImageLoader imageLoader = ImageLoader.getInstance();

    public OrderMenuAdapter(Context context){
        this.context = context;
        orderMenuDbAdapter = new OrderMenuDatabaseAdapter(context);
        if (!imageLoader.isInited()) {
            imageLoader.init(ImageLoaderConfiguration.createDefault(context));
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_order_list, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        long totalPrice = orderMenuList.get(position).getProduct().getSellPrice() * orderMenuList.get(position).getQty();
        String q = String.valueOf(orderMenuList.get(position).getQty());

        holder.menuName.setText(orderMenuList.get(position).getProduct().getName());
        holder.menuQty.setText(q);
        holder.totalPrice.setText("Rp " + decimalFormat.format(totalPrice));
        imageLoader.displayImage("file://" + ImageUtil.getImagePath(context, orderMenuList.get(position).getProduct().getId()), holder.preview, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                holder.preview.setImageResource(R.drawable.no_image);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                optionDialog(position, orderMenuList.get(position).getId());
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderMenuList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView menuName, menuQty, totalPrice;
        ImageView preview;

        public ViewHolder(View itemView) {
            super(itemView);
            menuName = (TextView) itemView.findViewById(R.id.text_menu_name);
            menuQty = (TextView) itemView.findViewById(R.id.text_menu_quantity);
            totalPrice = (TextView) itemView.findViewById(R.id.text_total_price);
            preview = (ImageView) itemView.findViewById(R.id.img_preview);
        }
    }

    private void confirmDialog(final int location, final String orderMenuId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Konfirmasi");
        builder.setMessage("Hapus order ?");
        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                orderMenuList.remove(location);
                notifyItemRemoved(location);
                orderMenuDbAdapter.deleteOrderMenu(orderMenuId);
                ((MainActivity)context).updateInfo();
            }
        });
        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }


    private void optionDialog(final int location, final String orderMenuId){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Konfirmasi");

        builder.setItems(new String[]{"Edit", "Delete"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    OrderMenu orderMenu = orderMenuDbAdapter.getOrderMenuById(orderMenuId);

                    String productId = orderMenu.getProduct().getId();
                    int qty = orderMenu.getQtyOrder();
                    ((MainActivity) context).orderUpdate(productId, qty);
                } else if (which == 1) {
                    confirmDialog(location, orderMenuId);
                }
            }
        });
        builder.show();
    }

    public void addItems(List<OrderMenu> orderMenus){
        this.orderMenuList = orderMenus;
        notifyDataSetChanged();
    }
}
