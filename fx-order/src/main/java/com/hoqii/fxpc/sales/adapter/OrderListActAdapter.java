package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageButton;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.activity.MainActivity;
import com.hoqii.fxpc.sales.content.database.adapter.OrderMenuDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.fragment.OrderListFragment;
import com.hoqii.fxpc.sales.holder.OrderHolder;
import com.hoqii.fxpc.sales.util.ImageUtil;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.TypiconsIcons;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.meruvian.midas.core.defaults.DefaultAdapter;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by meruvian on 14/07/15.
 */
public class OrderListActAdapter extends DefaultAdapter<OrderMenu, OrderHolder> {
    private DecimalFormat decimalFormat = new DecimalFormat("#,###");
    private double totalPrice;
    private OrderMenuDatabaseAdapter orderMenuDbAdapter;
    private Fragment fragment;
    private ImageLoader imageLoader = ImageLoader.getInstance();

    public OrderListActAdapter(Context context, int layout, List contents, Fragment fragment) {
        super(context, layout, contents);
        this.fragment = fragment;

        orderMenuDbAdapter = new OrderMenuDatabaseAdapter(context);
        if (!imageLoader.isInited()) {
            imageLoader.init(ImageLoaderConfiguration.createDefault(getContext()));
        }
    }

    @Override
    public OrderHolder ViewHolder(View view) {
        return new OrderHolder(view);
    }

    @Override
    public View createdView(View view, final OrderHolder holder, final OrderMenu orderMenu) {
        totalPrice = orderMenu.getProduct().getSellPrice() * (orderMenu.getQty());
        String q = String.valueOf(orderMenu.getQty());

        holder.menuName.setText(orderMenu.getProduct().getName());
        holder.menuQuantity.setText(q);
        holder.totalPrice.setText("Rp " + decimalFormat.format(totalPrice));

//        holder.buttonEdit.setImageDrawable(new IconDrawable(getContext(), TypiconsIcons.typcn_pencil).colorRes(R.color.grey).actionBarSize());
//        holder.buttonEdit.setOnClickListener(new OnClickDataEdit(getContext(), getPosition(), orderMenu.getId()));
//        holder.buttonDelete.setImageDrawable(new IconDrawable(getContext(), TypiconsIcons.typcn_times).colorRes(R.color.grey).actionBarSize());
//        holder.buttonDelete.setOnClickListener(new OnClickData(getContext(), getPosition(), orderMenu.getId()));
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                optionDialog(getPosition(), orderMenu.getId());
                return true;
            }
        });


        imageLoader.displayImage("file://" + ImageUtil.getImagePath(getContext(), orderMenu.getProduct().getId()), holder.preview, new SimpleImageLoadingListener() {
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

        return view;
    }

    private class OnClickData extends ImageButton implements ImageButton.OnClickListener {
        private int location;
        private String orderMenuId;

        public OnClickData(Context context, int location, String orderMenuId) {
            super(context);
            this.location = location;
            this.orderMenuId = orderMenuId;
        }

        @Override
        public void onClick(View view) {
            confirmDialog(location, orderMenuId);
        }
    }

    private class OnClickDataEdit extends ImageButton implements ImageButton.OnClickListener {
        private int location;
        private String orderMenuId;

        public OnClickDataEdit(Context context, int location, String orderMenuId) {
            super(context);
            this.location = location;
            this.orderMenuId = orderMenuId;
        }

        @Override
        public void onClick(View view) {
            confirmEditDialog(location, orderMenuId);
        }
    }


    private void confirmDialog(final int location, final String orderMenuId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Konfirmasi");
        builder.setMessage("Hapus order ?");
        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                OrderListFragment orderListActivity = (OrderListFragment) fragment;

                getList().remove(location);
                notifyDataSetChanged();
                orderMenuDbAdapter.deleteOrderMenu(orderMenuId);
                orderListActivity.setTotalOrderTab();
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

    private void confirmEditDialog(final int location, final String orderMenuId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Konfirmasi");
        builder.setMessage("Edit order ?");
        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                OrderMenu orderMenu = orderMenuDbAdapter.getOrderMenuById(orderMenuId);

                String productId = orderMenu.getProduct().getId();
                int qty = orderMenu.getQtyOrder();
                ((MainActivity)getContext()).orderUpdate(productId, qty);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Konfirmasi");

        builder.setItems(new String[]{"Edit", "Delete"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    OrderMenu orderMenu = orderMenuDbAdapter.getOrderMenuById(orderMenuId);

                    String productId = orderMenu.getProduct().getId();
                    int qty = orderMenu.getQtyOrder();
                    ((MainActivity) getContext()).orderUpdate(productId, qty);
                } else if (which == 1) {
                    confirmDialog(location, orderMenuId);
                }
            }
        });
        builder.show();
    }
}
