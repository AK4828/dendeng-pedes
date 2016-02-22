package com.hoqii.fxpc.sales.content.database.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.util.Log;

import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.content.MidasContentProvider;
import com.hoqii.fxpc.sales.content.database.model.BusinessPartnerDatabaseModel;
import com.hoqii.fxpc.sales.entity.BusinessPartner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 12/6/15.
 */
public class BusinessPartnerDatabaseAdapter {

    private Uri dbUriBusinessPartner = Uri.parse(MidasContentProvider.CONTENT_PATH
            + MidasContentProvider.TABLES[18]);

    private Context context;

    public BusinessPartnerDatabaseAdapter(Context context){
        this.context = context;
    }

    public BusinessPartner findBusinessPartnerById(String id){
        Log.d("businespartner cursorid", id);
        String query = BusinessPartnerDatabaseModel.ID + " = ?";
        String[] parameter = {id};

        Cursor cursor = context.getContentResolver().query(dbUriBusinessPartner, null, query, parameter, null);

        BusinessPartner businessPartner = null;

        if (cursor != null){
            if (cursor.getCount() > 0){
                try {
                    cursor.moveToFirst();
                    businessPartner = new BusinessPartner();
                    businessPartner.setId(cursor.getString(cursor.getColumnIndex(BusinessPartnerDatabaseModel.ID)));
                    businessPartner.setName(cursor.getString(cursor.getColumnIndex(BusinessPartnerDatabaseModel.NAME)));
                }catch (SQLException e){
                    e.printStackTrace();

                    businessPartner = null;
                }
            }
        }

        cursor.close();
        return businessPartner;
    }

    public void saveBusinessPartner(List<BusinessPartner> businessPartners){

        for (BusinessPartner businessPartner : businessPartners){
            ContentValues values = new ContentValues();

            if (findBusinessPartnerById(businessPartner.getId()) == null){
                values.put(BusinessPartnerDatabaseModel.ID, businessPartner.getId());
                values.put(BusinessPartnerDatabaseModel.NAME, businessPartner.getName());
                values.put(BusinessPartnerDatabaseModel.OFFICEPHONE, businessPartner.getOfficePhone());
                values.put(BusinessPartnerDatabaseModel.FAX, businessPartner.getFax());
                values.put(BusinessPartnerDatabaseModel.EMAIL, businessPartner.getEmail());
                values.put(BusinessPartnerDatabaseModel.OTHEREMAIL, businessPartner.getOtherEmail());
                values.put(BusinessPartnerDatabaseModel.ADDRESS, businessPartner.getAddress());
                values.put(BusinessPartnerDatabaseModel.CITY, businessPartner.getCity());
                values.put(BusinessPartnerDatabaseModel.ZIPCODE, businessPartner.getZipCode());
                values.put(BusinessPartnerDatabaseModel.COUNTRY, businessPartner.getCountry());
                values.put(BusinessPartnerDatabaseModel.DESCRIPTION, businessPartner.getDescription());
                values.put(BusinessPartnerDatabaseModel.STATUS_FLAG, 1);

                context.getContentResolver().insert(dbUriBusinessPartner, values);
            }else {
                values.put(BusinessPartnerDatabaseModel.NAME, businessPartner.getName());
                values.put(BusinessPartnerDatabaseModel.OFFICEPHONE, businessPartner.getOfficePhone());
                values.put(BusinessPartnerDatabaseModel.FAX, businessPartner.getFax());
                values.put(BusinessPartnerDatabaseModel.EMAIL, businessPartner.getEmail());
                values.put(BusinessPartnerDatabaseModel.OTHEREMAIL, businessPartner.getOtherEmail());
                values.put(BusinessPartnerDatabaseModel.ADDRESS, businessPartner.getAddress());
                values.put(BusinessPartnerDatabaseModel.CITY, businessPartner.getCity());
                values.put(BusinessPartnerDatabaseModel.ZIPCODE, businessPartner.getZipCode());
                values.put(BusinessPartnerDatabaseModel.COUNTRY, businessPartner.getCountry());
                values.put(BusinessPartnerDatabaseModel.DESCRIPTION, businessPartner.getDescription());
                values.put(BusinessPartnerDatabaseModel.STATUS_FLAG, 1);

                context.getContentResolver().update(dbUriBusinessPartner, values, BusinessPartnerDatabaseModel.ID + " = ?", new String[]{businessPartner.getId()});
            }
        }

    }

    public List<BusinessPartner> getBusinessPartner(){

        String query = BusinessPartnerDatabaseModel.STATUS_FLAG + " = " + SignageVariables.ACTIVE;

        Cursor cursor = context.getContentResolver().query(dbUriBusinessPartner, null, query, null, BusinessPartnerDatabaseModel.NAME);

        List<BusinessPartner> businessPartners = new ArrayList<BusinessPartner>();

        if (cursor != null){
            while (cursor.moveToNext()){
                BusinessPartner businessPartner = new BusinessPartner();
                businessPartner.setId(cursor.getString(cursor.getColumnIndex(BusinessPartnerDatabaseModel.ID)));
                businessPartner.setName(cursor.getString(cursor.getColumnIndex(BusinessPartnerDatabaseModel.NAME)));
                businessPartner.setOfficePhone(cursor.getString(cursor.getColumnIndex(BusinessPartnerDatabaseModel.OFFICEPHONE)));
                businessPartner.setFax(cursor.getString(cursor.getColumnIndex(BusinessPartnerDatabaseModel.FAX)));
                businessPartner.setEmail(cursor.getString(cursor.getColumnIndex(BusinessPartnerDatabaseModel.EMAIL)));
                businessPartner.setOtherEmail(cursor.getString(cursor.getColumnIndex(BusinessPartnerDatabaseModel.OTHEREMAIL)));
                businessPartner.setAddress(cursor.getString(cursor.getColumnIndex(BusinessPartnerDatabaseModel.ADDRESS)));
                businessPartner.setCity(cursor.getString(cursor.getColumnIndex(BusinessPartnerDatabaseModel.CITY)));
                businessPartner.setZipCode(cursor.getString(cursor.getColumnIndex(BusinessPartnerDatabaseModel.ZIPCODE)));
                businessPartner.setCountry(cursor.getString(cursor.getColumnIndex(BusinessPartnerDatabaseModel.COUNTRY)));
                businessPartner.setDescription(cursor.getString(cursor.getColumnIndex(BusinessPartnerDatabaseModel.DESCRIPTION)));

                businessPartners.add(businessPartner);
            }
        }


        cursor.close();

        return businessPartners;

    }

}
