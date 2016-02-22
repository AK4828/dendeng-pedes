package com.hoqii.fxpc.sales.adapter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageAppication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.activity.SellerOrderMenuListActivity;
import com.hoqii.fxpc.sales.activity.ShipmentHistoryDetailActivity;
import com.hoqii.fxpc.sales.activity.ShipmentHistoryListActivity;
import com.hoqii.fxpc.sales.activity.ShipmentListActivity;
import com.hoqii.fxpc.sales.entity.Shipment;
import com.hoqii.fxpc.sales.job.ShipmentUpdateJob;
import com.joanzapata.iconify.widget.IconTextView;
import com.path.android.jobqueue.JobManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by miftakhul on 12/6/15.
 */
public class ShipmentHistoryAdapter extends RecyclerView.Adapter<ShipmentHistoryAdapter.ViewHolder> {

    private Context context;
    private List<Shipment> shipmentList = new ArrayList<Shipment>();
    private JobManager jobManager;
    private SharedPreferences preferences;
    private boolean isLoli = false;


    public ShipmentHistoryAdapter(Context context) {
        this.context = context;

        jobManager = SignageAppication.getInstance().getJobManager();
        preferences = context.getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        Log.d(getClass().getSimpleName(), "shipment list adapter size " + shipmentList.size());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            isLoli = true;
        }else {
            isLoli = false;
        }
    }


    public ShipmentHistoryAdapter(Context context, List<Shipment> shipments) {
        this.context = context;
        this.shipmentList = shipments;
        jobManager = SignageAppication.getInstance().getJobManager();
        preferences = context.getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        Log.d(getClass().getSimpleName(), "shipment list adapter size " + shipmentList.size());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            isLoli = true;
        }else {
            isLoli = false;
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_seller_shipment_history_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy / hh:mm:ss");
        Date date = new Date();
        date.setTime(shipmentList.get(position).getLogInformation().getCreateDate().getTime());

        holder.site.setText(shipmentList.get(position).getOrder().getSiteFrom().getName());
        holder.shipmentNumber.setText(shipmentList.get(position).getReceiptNumber());
        holder.orderNumber.setText(shipmentList.get(position).getOrder().getReceiptNumber());
        holder.shipmentDate.setText("{typcn-time} " + simpleDateFormat.format(date));

        switch (shipmentList.get(position).getStatus()){
            case WAIT:
                holder.statusWait.setTextColor(context.getResources().getColor(R.color.colorAccent));
                holder.statusDelivered.setTextColor(context.getResources().getColor(R.color.grey));
                break;
            case DELIVERED:
                holder.statusWait.setTextColor(context.getResources().getColor(R.color.colorAccent));
                holder.statusDelivered.setTextColor(context.getResources().getColor(R.color.colorAccent));
                break;
            default:
                holder.statusWait.setTextColor(context.getResources().getColor(R.color.grey));
                holder.statusDelivered.setTextColor(context.getResources().getColor(R.color.grey));
                break;
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ShipmentHistoryDetailActivity.class);
                intent.putExtra("shipmentId", shipmentList.get(position).getId());
                intent.putExtra("shipmentDate", shipmentList.get(position).getLogInformation().getCreateDate().getTime());
                intent.putExtra("shipmentReceipt", shipmentList.get(position).getReceiptNumber());


                ObjectMapper mapper = SignageAppication.getObjectMapper();
                try {
                    String shipmentJson = mapper.writeValueAsString(shipmentList.get(position));
                    intent.putExtra("shipmentJson", shipmentJson);

                    Log.d(getClass().getSimpleName(), "data :" + shipmentJson);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                context.startActivity(intent);

            }
        });

        if (position == shipmentList.size() - 1){
            ((ShipmentHistoryListActivity)context).loadMoreContent();
        }
    }

    @Override
    public int getItemCount() {
        return shipmentList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView site;
        private IconTextView shipmentNumber, orderNumber, shipmentDate, statusWait, statusDelivered;

        public ViewHolder(View itemView) {
            super(itemView);
            site = (TextView) itemView.findViewById(R.id.sl_site);
            shipmentNumber = (IconTextView) itemView.findViewById(R.id.sl_number);
            orderNumber = (IconTextView) itemView.findViewById(R.id.sl_orderNumber);
            shipmentDate = (IconTextView) itemView.findViewById(R.id.sl_tgl);
            statusWait = (IconTextView) itemView.findViewById(R.id.ol_wait);
            statusDelivered = (IconTextView) itemView.findViewById(R.id.ol_delivered);
        }
    }


    private void AlertMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Shipment");
        builder.setMessage(message);
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    public void removeItem(String shipmentId){
        Log.d(getClass().getSimpleName(), "Searching for " + shipmentId);
        for (int x = 0; x < shipmentList.size(); x++){
            if (shipmentList.get(x).getId().equalsIgnoreCase(shipmentId)){
                Log.d(getClass().getSimpleName(), "data contains " + shipmentId);
                shipmentList.remove(x);
                notifyItemRemoved(x);
                notifyDataSetChanged();
            }else {
                Log.d(getClass().getSimpleName(), "data not contains " + shipmentId);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startAnimateRevealColorFromCoordinate(View view, int x, int y){
        float finalRadius = (float) Math.hypot(view.getWidth(), view.getHeight());

        Animator anim = ViewAnimationUtils.createCircularReveal(view, x, y, 0, finalRadius);
        view.setBackgroundColor(context.getResources().getColor(R.color.green));
        anim.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void backAnimateRevealColorFromCoordinate(final View view, int x, int y){

        int initialRadius = view.getWidth();

        Animator anim = ViewAnimationUtils.createCircularReveal(view, x, y, initialRadius, 0);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setBackgroundColor(context.getResources().getColor(R.color.light_grey));
            }
        });

        anim.start();
    }

    public void addItems(List<Shipment> shipments){
        for (Shipment s : shipments){
            if (!shipmentList.contains(s)){
                shipmentList.add(s);
                notifyDataSetChanged();
            }
        }
    }

}
