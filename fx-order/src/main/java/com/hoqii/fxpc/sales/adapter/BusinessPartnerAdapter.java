package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.activity.BusinessPartnerActivity;
import com.hoqii.fxpc.sales.content.database.adapter.BusinessPartnerDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.BusinessPartner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 12/6/15.
 */
public class BusinessPartnerAdapter extends RecyclerView.Adapter<BusinessPartnerAdapter.ViewHolder> {

    private BusinessPartnerDatabaseAdapter businessPartnerDatabaseAdapter;

    private Context context;
    private List<BusinessPartner> businessPartners = new ArrayList<BusinessPartner>();

    public BusinessPartnerAdapter(Context context) {
        this.context = context;

        businessPartnerDatabaseAdapter = new BusinessPartnerDatabaseAdapter(context);
        businessPartners = businessPartnerDatabaseAdapter.getBusinessPartner();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_business_partner, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.bpName.setText(businessPartners.get(position).getName());
        holder.bpEmail.setText(businessPartners.get(position).getEmail());
        holder.bpAddress.setText(businessPartners.get(position).getAddress());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(context, ContactsActivity.class);
//                intent.putExtra("businessPartnerId", businessPartners.get(position).getId());
//
//                Log.d(getClass().getSimpleName(), "busines partner id " + businessPartners.get(position).getId());
//                context.startActivity(intent);

                ((BusinessPartnerActivity)context).openContact(businessPartners.get(position).getId());
            }
        });

    }

    @Override
    public int getItemCount() {
        return businessPartners.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView bpName, bpEmail, bpAddress;

        public ViewHolder(View itemView) {
            super(itemView);
            bpName = (TextView) itemView.findViewById(R.id.bp_name);
            bpEmail = (TextView) itemView.findViewById(R.id.bp_email);
            bpAddress = (TextView) itemView.findViewById(R.id.bp_address);
        }
    }

}
