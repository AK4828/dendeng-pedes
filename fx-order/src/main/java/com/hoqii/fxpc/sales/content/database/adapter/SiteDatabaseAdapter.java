package com.hoqii.fxpc.sales.content.database.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.content.MidasContentProvider;
import com.hoqii.fxpc.sales.content.database.model.BusinessPartnerDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.SiteDatabaseModel;
import com.hoqii.fxpc.sales.core.commons.Site;
import com.hoqii.fxpc.sales.entity.BusinessPartner;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 12/6/15.
 */
public class SiteDatabaseAdapter {

    private Uri dbUriSite = Uri.parse(MidasContentProvider.CONTENT_PATH
            + MidasContentProvider.TABLES[20]);

    private Context context;

    public SiteDatabaseAdapter(Context context){
        this.context = context;
    }

    public Site findSiteById(String id){
        String query = BusinessPartnerDatabaseModel.ID + " = ?";
        String[] parameter = {id};

        Cursor cursor = context.getContentResolver().query(dbUriSite, null, query, parameter, null);

        Site site = null;

        if (cursor != null){
            if (cursor.getCount() > 0){
                try {
                    cursor.moveToFirst();
                    site = new Site();
                    site.setId(cursor.getString(cursor.getColumnIndex(BusinessPartnerDatabaseModel.ID)));
                    site.setName(cursor.getString(cursor.getColumnIndex(BusinessPartnerDatabaseModel.NAME)));
                }catch (SQLException e){
                    e.printStackTrace();

                    site = null;
                }
            }
        }

        cursor.close();
        return site;
    }

    public void saveSite(List<Site> sites){

        for (Site site : sites){
            ContentValues values = new ContentValues();

            if (findSiteById(site.getId()) == null){
                values.put(SiteDatabaseModel.ID, site.getId());
                values.put(SiteDatabaseModel.NAME, site.getName());
                values.put(SiteDatabaseModel.DESCRIPTION, site.getDescription());
                values.put(SiteDatabaseModel.TITLE, site.getTitle());
                values.put(SiteDatabaseModel.URL_BRANDING, site.getUrlBranding());
                values.put(SiteDatabaseModel.SITE_URL, site.getSiteUrl());
                values.put(SiteDatabaseModel.ADMIN_EMAIL, site.getAdminEmail());
                values.put(SiteDatabaseModel.NOTIFY_FLAG, site.getNotifyFlag());
                values.put(SiteDatabaseModel.NOTIFY_EMAIL, site.getNotifyEmail());
                values.put(SiteDatabaseModel.NOTIFY_FROM, site.getNotifyFrom());
                values.put(SiteDatabaseModel.NOTIFY_MESSAGE, site.getNotifyMessage());
                values.put(SiteDatabaseModel.WORKSPACE_TYPE, site.getWorkspaceType());
                values.put(SiteDatabaseModel.VIRTUALHOST, site.getVirtualhost());
                values.put(SiteDatabaseModel.PATH, site.getPath());
                values.put(SiteDatabaseModel.LEVEL, site.getLevel());
                values.put(SiteDatabaseModel.THEME, site.getTheme());
                values.put(SiteDatabaseModel.VERY_TRANS_ID, site.getVerytransId());
                values.put(SiteDatabaseModel.ADDRESS, site.getAddress());
                values.put(SiteDatabaseModel.PHONE, site.getPhone());
                values.put(SiteDatabaseModel.FAX, site.getFax());
                values.put(SiteDatabaseModel.EMAIL, site.getEmail());
                values.put(SiteDatabaseModel.NPWP, site.getNpwp());
                values.put(SiteDatabaseModel.POSTAL_CODE, site.getPostalCode());
                values.put(SiteDatabaseModel.CITY, site.getCity());
                values.put(SiteDatabaseModel.TYPE, site.getType());

                context.getContentResolver().insert(dbUriSite, values);
            }else {
                values.put(SiteDatabaseModel.NAME, site.getName());
                values.put(SiteDatabaseModel.DESCRIPTION, site.getDescription());
                values.put(SiteDatabaseModel.TITLE, site.getTitle());
                values.put(SiteDatabaseModel.URL_BRANDING, site.getUrlBranding());
                values.put(SiteDatabaseModel.SITE_URL, site.getSiteUrl());
                values.put(SiteDatabaseModel.ADMIN_EMAIL, site.getAdminEmail());
                values.put(SiteDatabaseModel.NOTIFY_FLAG, site.getNotifyFlag());
                values.put(SiteDatabaseModel.NOTIFY_EMAIL, site.getNotifyEmail());
                values.put(SiteDatabaseModel.NOTIFY_FROM, site.getNotifyFrom());
                values.put(SiteDatabaseModel.NOTIFY_MESSAGE, site.getNotifyMessage());
                values.put(SiteDatabaseModel.WORKSPACE_TYPE, site.getWorkspaceType());
                values.put(SiteDatabaseModel.VIRTUALHOST, site.getVirtualhost());
                values.put(SiteDatabaseModel.PATH, site.getPath());
                values.put(SiteDatabaseModel.LEVEL, site.getLevel());
                values.put(SiteDatabaseModel.THEME, site.getTheme());
                values.put(SiteDatabaseModel.VERY_TRANS_ID, site.getVerytransId());
                values.put(SiteDatabaseModel.ADDRESS, site.getAddress());
                values.put(SiteDatabaseModel.PHONE, site.getPhone());
                values.put(SiteDatabaseModel.FAX, site.getFax());
                values.put(SiteDatabaseModel.EMAIL, site.getEmail());
                values.put(SiteDatabaseModel.NPWP, site.getNpwp());
                values.put(SiteDatabaseModel.POSTAL_CODE, site.getPostalCode());
                values.put(SiteDatabaseModel.CITY, site.getCity());
                values.put(SiteDatabaseModel.TYPE, site.getType());

                context.getContentResolver().update(dbUriSite, values, SiteDatabaseModel.ID + " = ?", new String[]{site.getId()});
            }
        }

    }

    public List<Site> getSite(){
//        String query = BusinessPartnerDatabaseModel.STATUS_FLAG + " = " + SignageVariables.ACTIVE;

        Cursor cursor = context.getContentResolver().query(dbUriSite, null, null, null, SiteDatabaseModel.NAME);

        List<Site> sites = new ArrayList<Site>();

        if (cursor != null){
            while (cursor.moveToNext()){
                Site site = new Site();
                site.setId(cursor.getString(cursor.getColumnIndex(SiteDatabaseModel.ID)));
                site.setName(cursor.getString(cursor.getColumnIndex(SiteDatabaseModel.NAME)));
                site.setDescription(cursor.getString(cursor.getColumnIndex(SiteDatabaseModel.DESCRIPTION)));
                site.setEmail(cursor.getString(cursor.getColumnIndex(SiteDatabaseModel.EMAIL)));
                site.setType(cursor.getString(cursor.getColumnIndex(SiteDatabaseModel.TYPE)));

                sites.add(site);
            }
        }

        cursor.close();

        return sites;

    }

    public void deleteSite(){
        context.getContentResolver().delete(dbUriSite, null, null);
    }

}
