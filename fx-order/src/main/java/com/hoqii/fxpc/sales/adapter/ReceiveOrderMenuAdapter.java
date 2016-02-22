package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.activity.ReceiveDetailActivity;
import com.hoqii.fxpc.sales.content.database.adapter.SerialNumberDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.entity.SerialNumber;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 12/6/15.
 */
public class ReceiveOrderMenuAdapter extends RecyclerView.Adapter<ReceiveOrderMenuAdapter.ViewHolder> {


    private Context context;
    private List<SerialNumber> serialNumberList = new ArrayList<SerialNumber>();
    private List<String> tempSerialNumberList = new ArrayList<String>();
    private SerialNumberDatabaseAdapter serialNumberDatabaseAdapter;
    private boolean verify = false;

    public ReceiveOrderMenuAdapter(Context context, String orderId) {
        this.context = context;

        serialNumberDatabaseAdapter = new SerialNumberDatabaseAdapter(context);
        List<SerialNumber> sn = serialNumberDatabaseAdapter.getSerialNumberListByOrderId(orderId);
        for (SerialNumber s : sn){
            tempSerialNumberList.add(s.getSerialNumber());
        }

    }

    public ReceiveOrderMenuAdapter(Context context, String orderId, boolean verify) {
        this.context = context;
        this.verify = verify;

        serialNumberDatabaseAdapter = new SerialNumberDatabaseAdapter(context);
        List<SerialNumber> sn = serialNumberDatabaseAdapter.getSerialNumberListByOrderId(orderId);
        for (SerialNumber s : sn){
            tempSerialNumberList.add(s.getSerialNumber());
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_receive_order_menu_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.productName.setText("Product : " + serialNumberList.get(position).getOrderMenu().getProduct().getName());
        holder.productSerial.setText("Serial Number : " + serialNumberList.get(position).getSerialNumber());


        if (tempSerialNumberList.contains(serialNumberList.get(position).getSerialNumber())){
            holder.imageStatus.setVisibility(View.VISIBLE);
        }else {
            holder.imageStatus.setVisibility(View.GONE);
        }

        if (verify == true){
            holder.imageStatus.setVisibility(View.VISIBLE);
        }

        //disable load more
//        if (position == serialNumberList.size() - 1){
//            ((ReceiveDetailActivity)context).loadMoreContent();
//        }
    }

    @Override
    public int getItemCount() {
        return serialNumberList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView productName, productSerial;
        private ImageView imageStatus;

        public ViewHolder(View itemView) {
            super(itemView);
            productName = (TextView) itemView.findViewById(R.id.om_name);
            productSerial = (TextView) itemView.findViewById(R.id.om_serial);
            imageStatus = (ImageView) itemView.findViewById(R.id.ol_img);
        }
    }

    public void addItems(List<SerialNumber> serialNumbers){
        for (SerialNumber s : serialNumbers){
            if (!serialNumberList.contains(s)){
                serialNumberList.add(s);
            }
        }
        if (tempSerialNumberList.size() == serialNumberList.size()){
            this.verify = true;
            Log.d(getClass().getSimpleName(), "verify true");
        }
        notifyDataSetChanged();
        Log.d(getClass().getSimpleName(), "adapter updated");
    }

    public boolean isVerify(){
        return verify;
    }

    public void setVerify(boolean verify){
        this.verify = verify;
    }

}
