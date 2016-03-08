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

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.activity.SellerOrderMenuListActivity;
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
public class ShipmentAdapter extends RecyclerView.Adapter<ShipmentAdapter.ViewHolder> {

    private Context context;
    private List<Shipment> shipmentList = new ArrayList<Shipment>();
    private JobManager jobManager;
    private SharedPreferences preferences;
    private boolean isLoli = false;


    public ShipmentAdapter(Context context) {
        this.context = context;

        jobManager = SignageApplication.getInstance().getJobManager();
        preferences = context.getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        Log.d(getClass().getSimpleName(), "shipment list adapter size " + shipmentList.size());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            isLoli = true;
        }else {
            isLoli = false;
        }
    }


    public ShipmentAdapter(Context context, List<Shipment> shipments) {
        this.context = context;
        this.shipmentList = shipments;
        jobManager = SignageApplication.getInstance().getJobManager();
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_seller_shipment_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy / hh:mm:ss");
        Date date = new Date();
        date.setTime(shipmentList.get(position).getLogInformation().getCreateDate().getTime());

        holder.site.setText(shipmentList.get(position).getOrder().getSite().getName());
        holder.shipmentNumber.setText("{typcn-tag} Shipment number :" + shipmentList.get(position).getReceiptNumber());
        holder.shipmentDate.setText("{typcn-time} " + simpleDateFormat.format(date));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, SellerOrderMenuListActivity.class);
                intent.putExtra("shipmentId", shipmentList.get(position).getId());
                intent.putExtra("shipmentDate", shipmentList.get(position).getLogInformation().getCreateDate().getTime());
                intent.putExtra("shipmentReceipt", shipmentList.get(position).getReceiptNumber());

                confirmDelivery(position);
            }
        });

        if (position == shipmentList.size() - 1){
            ((ShipmentListActivity)context).loadMoreContent();
        }
    }

    @Override
    public int getItemCount() {
        return shipmentList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView site;
        private IconTextView shipmentNumber, shipmentDate;

        public ViewHolder(View itemView) {
            super(itemView);
            site = (TextView) itemView.findViewById(R.id.sl_site);
            shipmentNumber = (IconTextView) itemView.findViewById(R.id.sl_number);
            shipmentDate = (IconTextView) itemView.findViewById(R.id.sl_tgl);
        }
    }

    private void confirmDelivery(final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);


        final View view = View.inflate(context, R.layout.view_shipment_update, null);

        TextView shipmentNumber, shipmentDate;
        final ImageView shipmentStatus;
        final CheckBox shipmentDelivered;
        final LinearLayout layout;
        final boolean[] checked = {false};

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy / hh:mm:ss");
        Date date = new Date();
        date.setTime(shipmentList.get(position).getLogInformation().getCreateDate().getTime());

        layout = (LinearLayout) view.findViewById(R.id.dialog_update_layout);
        shipmentNumber = (TextView) view.findViewById(R.id.sl_number);
        shipmentDate = (TextView) view.findViewById(R.id.sl_tgl);
        shipmentStatus = (ImageView) view.findViewById(R.id.img_shipment_status);
        shipmentDelivered = (CheckBox) view.findViewById(R.id.shipment_delivered);

        shipmentNumber.setText("Shipment Number : " + shipmentList.get(position).getReceiptNumber());
        shipmentDate.setText("Tanggal : " + simpleDateFormat.format(date));

        String status = shipmentList.get(position).getStatus().name();

        if (status.equalsIgnoreCase(Shipment.ShipmentStatus.WAIT.name())) {
            shipmentStatus.setImageResource(R.drawable.ic_cached_black_48dp);
        } else if (status.equalsIgnoreCase(Shipment.ShipmentStatus.DELIVERED.name())) {
            shipmentStatus.setImageResource(R.drawable.ic_done_all_black_48dp);
        }

        if (!shipmentDelivered.isChecked()) {
            shipmentStatus.setImageResource(R.drawable.ic_cached_black_48dp);
            checked[0] = false;
        } else {
            shipmentStatus.setImageResource(R.drawable.ic_done_all_black_48dp);
            checked[0] = true;
        }

        shipmentDelivered.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    shipmentStatus.setImageResource(R.drawable.ic_cached_black_48dp);
                    checked[0] = isChecked;

                    if (isLoli){
                        int x = ( shipmentDelivered.getLeft() + shipmentDelivered.getRight()) / 2;
                        int y = ( shipmentDelivered.getTop() + shipmentDelivered.getBottom()) / 2;
                        backAnimateRevealColorFromCoordinate(view, x, y);
                    }
                } else {
                    shipmentStatus.setImageResource(R.drawable.ic_done_all_black_48dp);
                    checked[0] = isChecked;


                    if (isLoli){
                        int x = ( shipmentDelivered.getLeft() + shipmentDelivered.getRight()) / 2;
                        int y = ( shipmentDelivered.getTop() + shipmentDelivered.getBottom()) / 2;
                        startAnimateRevealColorFromCoordinate(view, x, y);
                    }
                }
            }
        });

        builder.setView(view);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (checked[0]) {
                    String shipmentId = shipmentList.get(position).getId();
                    jobManager.addJobInBackground(new ShipmentUpdateJob(preferences.getString("server_url", ""), shipmentId, checked));
                }else {
                    AlertMessage("Data tidak di rubah");
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();

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
