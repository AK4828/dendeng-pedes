package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.activity.SellerOrderListActivity;
import com.hoqii.fxpc.sales.activity.SellerOrderMenuListActivity;
import com.hoqii.fxpc.sales.activity.SiteListActivity;
import com.hoqii.fxpc.sales.content.database.adapter.OrderDatabaseAdapter;
import com.hoqii.fxpc.sales.core.commons.Site;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.joanzapata.iconify.widget.IconTextView;

import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by miftakhul on 12/6/15.
 */
public class SiteAdapter extends RecyclerView.Adapter<SiteAdapter.ViewHolder> {


    private Context context;
    private List<Site> siteList = new ArrayList<Site>();
    private OrderDatabaseAdapter orderDatabaseAdapter;

    public SiteAdapter(Context context, List<Site> sites) {
        this.context = context;
        this.siteList = sites;

        orderDatabaseAdapter = new OrderDatabaseAdapter(context);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_site_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.name.setText(siteList.get(position).getDescription());
        holder.email.setText("{typcn-mail} "+siteList.get(position).getEmail());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String orderId = orderDatabaseAdapter.getOrderId();

                if (orderId == null){
                    //create when not exist
                    Order order = new Order();

                    order.setSiteId("");
                    order.setOrderType("1");
                    order.setReceiptNumber("");
                    order.getSite().setId(siteList.get(position).getId());
                    order.getLogInformation().setCreateBy(AuthenticationUtils.getCurrentAuthentication().getUser().getId());
                    order.getLogInformation().setCreateDate(new Date());
                    order.getLogInformation().setLastUpdateBy(AuthenticationUtils.getCurrentAuthentication().getUser().getId());
                    order.getLogInformation().setSite(AuthenticationUtils.getCurrentAuthentication().getSite().getId());

                    order.setStatus(Order.OrderStatus.PROCESSED);
                    orderDatabaseAdapter.saveOrder(order);
                }else {
                    //update when exist
                    Order order = new Order();
                    order.setId(orderId);
                    order.getSite().setId(siteList.get(position).getId());
                    orderDatabaseAdapter.updateSiteOrder(order);

                }

                ((SiteListActivity) context).finalChoise();
            }
        });
    }

    @Override
    public int getItemCount() {
        return siteList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView name;
        private IconTextView email;

        public ViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.s_name);
            email = (IconTextView) itemView.findViewById(R.id.s_email);
        }
    }


}
