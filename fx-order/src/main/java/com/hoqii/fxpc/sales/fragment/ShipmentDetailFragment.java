package com.hoqii.fxpc.sales.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageAppication;
import com.hoqii.fxpc.sales.entity.Shipment;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by miftakhul on 2/10/16.
 */
public class ShipmentDetailFragment extends Fragment {

    private Shipment shipment;
    private TextView site, address, email, phone, fax, postalCode, city, shipDate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shipment_detail, container, false);

        site = (TextView) view.findViewById(R.id.s_site);
        address = (TextView) view.findViewById(R.id.s_address);
        email = (TextView) view.findViewById(R.id.s_email);
        phone = (TextView) view.findViewById(R.id.s_phone);
        fax = (TextView) view.findViewById(R.id.s_fax);
        postalCode = (TextView) view.findViewById(R.id.s_postal_code);
        city = (TextView) view.findViewById(R.id.s_city);
        shipDate = (TextView) view.findViewById(R.id.s_ship_date);

        Bundle b = getArguments();
        String shipmentJson = b.getString("shipmentJson");

        ObjectMapper mapper = SignageAppication.getObjectMapper();
        try {
            shipment = mapper.readValue(shipmentJson, Shipment.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy / hh:mm:ss");
        Date dateSend = new Date();
        dateSend.setTime(shipment.getLogInformation().getCreateDate().getTime());

        site.setText(shipment.getOrder().getSiteFrom().getName());
        address.setText(shipment.getOrder().getSiteFrom().getAddress());
        email.setText(shipment.getOrder().getSiteFrom().getEmail());
        phone.setText(shipment.getOrder().getSiteFrom().getPhone());
        fax.setText(shipment.getOrder().getSiteFrom().getFax());
        postalCode.setText(shipment.getOrder().getSiteFrom().getPostalCode());
        city.setText(shipment.getOrder().getSiteFrom().getCity());
        shipDate.setText(simpleDateFormat.format(dateSend));

        return view;
    }


}

