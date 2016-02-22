package com.hoqii.fxpc.sales.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.activity.ContactsActivity;
import com.hoqii.fxpc.sales.content.database.adapter.ContactDatabaseAdapter;
import com.hoqii.fxpc.sales.content.database.adapter.OrderDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.Contact;
import com.hoqii.fxpc.sales.entity.Order;

import java.util.List;

/**
 * Created by miftakhul on 12/6/15.
 */
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {

    private ContactDatabaseAdapter contactDatabaseAdapter;
    private OrderDatabaseAdapter orderDatabaseAdapter;

    private Context context;
    private List<Contact> contacts;


    public ContactsAdapter(Context context) {
        this.context = context;

        contactDatabaseAdapter = new ContactDatabaseAdapter(context);
        orderDatabaseAdapter = new OrderDatabaseAdapter(context);
        contacts = contactDatabaseAdapter.getContact();
    }

    public ContactsAdapter(Context context, String businessPartnerId) {
        this.context = context;

        contactDatabaseAdapter = new ContactDatabaseAdapter(context);
        orderDatabaseAdapter = new OrderDatabaseAdapter(context);
        contacts = contactDatabaseAdapter.getContactByBusinessPartner(businessPartnerId);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_contact_partner, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);


        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        String name = contacts.get(position).getFirstName() + " " + contacts.get(position).getLastName();

        holder.ctName.setText(name);
        holder.ctMobile.setText(contacts.get(position).getMobile());
        holder.ctEmail.setText(contacts.get(position).getEmail());
        holder.ctAddress.setText(contacts.get(position).getAddress());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String orderId = orderDatabaseAdapter.getOrderId();

                Order order = new Order();
                order.setId(orderId);
//                order.getContact().setId(contacts.get(position).getId());
                orderDatabaseAdapter.updateBizOrder(order);

                ((ContactsActivity) context).sendContactId();
            }
        });

    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView ctName, ctMobile, ctEmail, ctAddress;

        public ViewHolder(View itemView) {
            super(itemView);

            ctName = (TextView) itemView.findViewById(R.id.ct_name);
            ctMobile = (TextView) itemView.findViewById(R.id.ct_mobile);
            ctEmail = (TextView) itemView.findViewById(R.id.ct_email);
            ctAddress = (TextView) itemView.findViewById(R.id.ct_address);
        }
    }
}
