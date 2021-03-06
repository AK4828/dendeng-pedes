package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
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
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.hoqii.fxpc.sales.util.ImageUtil;

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
    private SharedPreferences preferences;

    public OrderMenuAdapter(Context context){
        this.context = context;
        orderMenuDbAdapter = new OrderMenuDatabaseAdapter(context);
        preferences = context.getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
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
        holder.totalPrice.setText(context.getResources().getString(R.string.text_currency) + decimalFormat.format(totalPrice));

//        Glide.with(context).load("file://" + ImageUtil.getImagePath(context, orderMenuList.get(position).getProduct().getId())).error(R.drawable.no_image).into(holder.preview);
        String imageUrl = preferences.getString("server_url", "")+"/api/products/"+orderMenuList.get(position).getProduct().getId() + "/image?access_token="+ AuthenticationUtils.getCurrentAuthentication().getAccessToken();
        Glide.with(context).load(imageUrl).error(R.drawable.ic_description_24dp).into(holder.preview);

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                optionDialog(position, orderMenuList.get(position).getId());
                return false;
            }
        });

        if (position == 0){
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.setMargins(0,24,0,0);
            holder.layout.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() {
        return orderMenuList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView menuName, menuQty, totalPrice;
        ImageView preview;
        RelativeLayout layout;

        public ViewHolder(View itemView) {
            super(itemView);
            menuName = (TextView) itemView.findViewById(R.id.text_menu_name);
            menuQty = (TextView) itemView.findViewById(R.id.text_menu_quantity);
            totalPrice = (TextView) itemView.findViewById(R.id.text_total_price);
            preview = (ImageView) itemView.findViewById(R.id.img_preview);
            layout = (RelativeLayout) itemView.findViewById(R.id.layout);
        }
    }

    private void confirmDialog(final int location, final String orderMenuId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.message_title_confirmation));
        builder.setMessage(context.getString(R.string.message_delete_order));
        builder.setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                orderMenuList.remove(location);
                notifyItemRemoved(location);
                orderMenuDbAdapter.deleteOrderMenu(orderMenuId);
                ((MainActivity)context).updateInfo();
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


    private void optionDialog(final int location, final String orderMenuId){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.message_title_confirmation));
        builder.setItems(new String[]{context.getString(R.string.text_edit), context.getString(R.string.text_delete)}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    OrderMenu orderMenu = orderMenuDbAdapter.getOrderMenuById(orderMenuId);
                    String jsonProduct = null;
                    ObjectMapper mapper = SignageApplication.getObjectMapper();
                    try {
                        jsonProduct = mapper.writeValueAsString(orderMenu.getProduct());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }

                    int qty = orderMenu.getQtyOrder();
                    ((MainActivity) context).orderUpdate(jsonProduct, qty);
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
