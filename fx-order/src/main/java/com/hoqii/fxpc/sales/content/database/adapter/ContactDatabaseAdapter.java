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
import com.hoqii.fxpc.sales.content.database.model.ContactDatabaseModel;
import com.hoqii.fxpc.sales.entity.BusinessPartner;
import com.hoqii.fxpc.sales.entity.Contact;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 12/6/15.
 */
public class ContactDatabaseAdapter {

    private Uri dbUriContact = Uri.parse(MidasContentProvider.CONTENT_PATH + MidasContentProvider.TABLES[7]);
    private Uri dbUriBusinessPartner = Uri.parse(MidasContentProvider.CONTENT_PATH
            + MidasContentProvider.TABLES[18]);

    private Context context;

    public ContactDatabaseAdapter(Context context) {
        this.context = context;
    }

    public Contact findContactById(String id) {
        String query = ContactDatabaseModel.ID + " = ?";
        String[] parameter = {id};

        Cursor cursor = context.getContentResolver().query(dbUriContact, null, query, parameter, null);

        Contact contact = null;

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                try {
                    cursor.moveToFirst();
                    Log.d("businespartner cursor", String.valueOf(cursor));

                    contact = new Contact();
                    contact.setId(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.ID)));
                } catch (SQLException e) {
                    e.printStackTrace();

                    contact = null;
                }
            }
        }

        cursor.close();
        return contact;
    }

    public void saveContact(List<Contact> contacts) {
        for (Contact contact : contacts) {

            ContentValues values = new ContentValues();
            if (findContactById(contact.getId()) == null) {
                values.put(ContactDatabaseModel.ID, contact.getId());
                values.put(ContactDatabaseModel.FIRSTNAME, contact.getFirstName());
                values.put(ContactDatabaseModel.LASTNAME, contact.getLastName());
                values.put(ContactDatabaseModel.OFFICE_PHONE, contact.getOfficePhone());
                values.put(ContactDatabaseModel.MOBILE, contact.getMobile());
                values.put(ContactDatabaseModel.HOME_PHONE, contact.getHomePhone());
                values.put(ContactDatabaseModel.OTHER_PHONE, contact.getOtherPhone());
                values.put(ContactDatabaseModel.FAX, contact.getFax());
                values.put(ContactDatabaseModel.EMAIL, contact.getEmail());
                values.put(ContactDatabaseModel.OTHER_EMAIL, contact.getOtherEmail());
                values.put(ContactDatabaseModel.ASSISTANT, contact.getAssistant());
                values.put(ContactDatabaseModel.ASSISTANT_PHONE, contact.getAssistantPhone());
                values.put(ContactDatabaseModel.ADDRESS, contact.getAddress());
                values.put(ContactDatabaseModel.CITY, contact.getCity());
                values.put(ContactDatabaseModel.ZIPCODE, contact.getZipCode());
                values.put(ContactDatabaseModel.COUNTRY, contact.getCountry());
                values.put(ContactDatabaseModel.DESCRIPTION, contact.getDescription());
                values.put(ContactDatabaseModel.BUSINESS_PARTNER_ID, contact.getBusinessPartner().getId().toString());
                values.put(ContactDatabaseModel.STATUS_FLAG, 1);

                context.getContentResolver().insert(dbUriContact, values);
            } else {
                values.put(ContactDatabaseModel.FIRSTNAME, contact.getFirstName());
                values.put(ContactDatabaseModel.LASTNAME, contact.getLastName());
                values.put(ContactDatabaseModel.OFFICE_PHONE, contact.getOfficePhone());
                values.put(ContactDatabaseModel.MOBILE, contact.getMobile());
                values.put(ContactDatabaseModel.HOME_PHONE, contact.getHomePhone());
                values.put(ContactDatabaseModel.OTHER_PHONE, contact.getOtherPhone());
                values.put(ContactDatabaseModel.FAX, contact.getFax());
                values.put(ContactDatabaseModel.EMAIL, contact.getEmail());
                values.put(ContactDatabaseModel.OTHER_EMAIL, contact.getOtherEmail());
                values.put(ContactDatabaseModel.ASSISTANT, contact.getAssistant());
                values.put(ContactDatabaseModel.ASSISTANT_PHONE, contact.getAssistantPhone());
                values.put(ContactDatabaseModel.ADDRESS, contact.getAddress());
                values.put(ContactDatabaseModel.CITY, contact.getCity());
                values.put(ContactDatabaseModel.ZIPCODE, contact.getZipCode());
                values.put(ContactDatabaseModel.COUNTRY, contact.getCountry());
                values.put(ContactDatabaseModel.DESCRIPTION, contact.getDescription());
                values.put(ContactDatabaseModel.BUSINESS_PARTNER_ID, contact.getBusinessPartner().getId().toString());
                values.put(ContactDatabaseModel.STATUS_FLAG, 1);

                context.getContentResolver().update(dbUriContact, values, ContactDatabaseModel.ID + " = ? ", new String[]{contact.getId()});
            }
        }
    }

    public List<Contact> getContact() {
        String query = ContactDatabaseModel.STATUS_FLAG + " = ?";
        String[] params = {SignageVariables.ACTIVE};

        Cursor cursor = context.getContentResolver().query(dbUriContact, null, query, params, ContactDatabaseModel.FIRSTNAME);

        List<Contact> contacts = new ArrayList<Contact>();

        if (cursor != null) {
            while (cursor.moveToNext()) {

                Contact contact = new Contact();

                contact.setId(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.ID)));
                contact.setFirstName(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.FIRSTNAME)));
                contact.setLastName(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.LASTNAME)));
                contact.setOfficePhone(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.OFFICE_PHONE)));
                contact.setMobile(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.MOBILE)));
                contact.setHomePhone(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.HOME_PHONE)));
                contact.setOtherPhone(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.OTHER_PHONE)));
                contact.setFax(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.FAX)));
                contact.setEmail(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.EMAIL)));
                contact.setOtherEmail(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.OTHER_EMAIL)));
                contact.setAddress(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.ADDRESS)));
                contact.setCity(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.CITY)));
                contact.setZipCode(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.ZIPCODE)));
                contact.setCountry(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.COUNTRY)));
                contact.setDescription(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.DESCRIPTION)));

                String businessPartnerId = cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.BUSINESS_PARTNER_ID));
                BusinessPartner businessPartner = new BusinessPartner();

                businessPartner = findBusinessPartners(businessPartnerId);
                if (businessPartner != null) {
                    contact.setBusinessPartner(businessPartner);
                }

                contacts.add(contact);
            }
        }

        cursor.close();
        return contacts;
    }

    public Contact getContactByContactId(String contactId) {
        String query = ContactDatabaseModel.ID + " = ? AND " + ContactDatabaseModel.STATUS_FLAG + " = ?";
        String[] params = {contactId, SignageVariables.ACTIVE};

        Cursor cursor = context.getContentResolver().query(dbUriContact, null, query, params, ContactDatabaseModel.FIRSTNAME);

        Contact contact = null;

        if (cursor != null) {
            if (cursor.getCount() > 0){
                cursor.moveToFirst();

                contact = new Contact();

                contact.setId(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.ID)));
                contact.setFirstName(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.FIRSTNAME)));
                contact.setLastName(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.LASTNAME)));
                contact.setOfficePhone(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.OFFICE_PHONE)));
                contact.setMobile(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.MOBILE)));
                contact.setHomePhone(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.HOME_PHONE)));
                contact.setOtherPhone(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.OTHER_PHONE)));
                contact.setFax(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.FAX)));
                contact.setEmail(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.EMAIL)));
                contact.setOtherEmail(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.OTHER_EMAIL)));
                contact.setAddress(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.ADDRESS)));
                contact.setCity(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.CITY)));
                contact.setZipCode(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.ZIPCODE)));
                contact.setCountry(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.COUNTRY)));
                contact.setDescription(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.DESCRIPTION)));

                String businessPartnerId = cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.BUSINESS_PARTNER_ID));
                BusinessPartner businessPartner = new BusinessPartner();

                businessPartner = findBusinessPartners(businessPartnerId);
                if (businessPartner != null) {
                    contact.setBusinessPartner(businessPartner);
                }
            }
        }

        cursor.close();
        return contact;
    }

    public List<Contact> getContactByBusinessPartner(String businessPartnerId) {
        String query = ContactDatabaseModel.BUSINESS_PARTNER_ID + " = ? AND " + ContactDatabaseModel.STATUS_FLAG + " = ?";
        String[] params = {businessPartnerId, SignageVariables.ACTIVE};

        Cursor cursor = context.getContentResolver().query(dbUriContact, null, query, params, ContactDatabaseModel.FIRSTNAME);

        List<Contact> contacts = new ArrayList<Contact>();

        if (cursor != null) {
            while (cursor.moveToNext()) {

                Contact contact = new Contact();

                contact.setId(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.ID)));
                contact.setFirstName(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.FIRSTNAME)));
                contact.setLastName(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.LASTNAME)));
                contact.setOfficePhone(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.OFFICE_PHONE)));
                contact.setMobile(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.MOBILE)));
                contact.setHomePhone(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.HOME_PHONE)));
                contact.setOtherPhone(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.OTHER_PHONE)));
                contact.setFax(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.FAX)));
                contact.setEmail(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.EMAIL)));
                contact.setOtherEmail(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.OTHER_EMAIL)));
                contact.setAddress(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.ADDRESS)));
                contact.setCity(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.CITY)));
                contact.setZipCode(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.ZIPCODE)));
                contact.setCountry(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.COUNTRY)));
                contact.setDescription(cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.DESCRIPTION)));

                String bpId = cursor.getString(cursor.getColumnIndex(ContactDatabaseModel.BUSINESS_PARTNER_ID));
                BusinessPartner businessPartner = new BusinessPartner();

                businessPartner = findBusinessPartners(bpId);
                if (businessPartner != null) {
                    contact.setBusinessPartner(businessPartner);
                }

                contacts.add(contact);
            }
        }

        cursor.close();
        return contacts;
    }

    public BusinessPartner findBusinessPartners(String businessPartnerId) {

        String query = BusinessPartnerDatabaseModel.ID + " = ? AND " + BusinessPartnerDatabaseModel.STATUS_FLAG + " = " + SignageVariables.ACTIVE;
        String[] params = {businessPartnerId};

        Cursor cursor = context.getContentResolver().query(dbUriBusinessPartner, null, query, params, BusinessPartnerDatabaseModel.NAME);

        BusinessPartner businessPartner = null;

        if (cursor != null) {
            cursor.moveToFirst();

            businessPartner = new BusinessPartner();
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
        }

        cursor.close();
        return businessPartner;
    }
}
