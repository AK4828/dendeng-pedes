package com.hoqii.fxpc.sales.content.database.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.hoqii.fxpc.sales.content.MidasContentProvider;
import com.hoqii.fxpc.sales.content.database.model.DefaultPersistenceModel;
import com.hoqii.fxpc.sales.content.database.model.SalesOrderMenuSerialDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.SerialNumberDatabaseModel;
import com.hoqii.fxpc.sales.entity.SalesOrderMenuSerial;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 1/5/16.
 */
public class SalesOrderSerialDatabaseAdapter extends DefaultDatabaseAdapter{

    private Uri dbUriSerial = Uri.parse(MidasContentProvider.CONTENT_PATH
            + MidasContentProvider.TABLES[23]);

    private Context context;

    public SalesOrderSerialDatabaseAdapter(Context context) {
        this.context = context;
    }

    public SalesOrderMenuSerial findSerialNumberById(String id) {
        String criteria = SalesOrderMenuSerialDatabaseModel.ID + " = ? ";
        String param[] = {id};

        Cursor cursor = context.getContentResolver().query(dbUriSerial, null, criteria, param, null);

        SalesOrderMenuSerial soSerial = null;

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                soSerial = new SalesOrderMenuSerial();
                soSerial.setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.ID)));
                soSerial.getSalesOrderMenu().setId(cursor.getString(cursor.getColumnIndex(SalesOrderMenuSerialDatabaseModel.SALES_ORDER_MENU_ID)));
                soSerial.setSerialNumber(cursor.getString(cursor.getColumnIndex(SalesOrderMenuSerialDatabaseModel.SERIAL)));
            }
        }

        cursor.close();
        return soSerial;
    }

    public void save(SalesOrderMenuSerial salesOrderMenuSerial){
        if (findSerialNumberById(salesOrderMenuSerial.getId()) == null) {
            ContentValues values = new ContentValues();
            values.put(DefaultPersistenceModel.ID, salesOrderMenuSerial.getId());
            values.put(SalesOrderMenuSerialDatabaseModel.SALES_ORDER_ID, salesOrderMenuSerial.getSalesOrderMenu().getSalesOrder().getId());
            values.put(SalesOrderMenuSerialDatabaseModel.SALES_ORDER_MENU_ID, salesOrderMenuSerial.getSalesOrderMenu().getId());
            values.put(SalesOrderMenuSerialDatabaseModel.SERIAL, salesOrderMenuSerial.getSerialNumber());
            values.put(DefaultPersistenceModel.SYNC_STATUS, 0);
            values.put(DefaultPersistenceModel.STATUS_FLAG, 0);

            context.getContentResolver().insert(dbUriSerial, values);
        } else {
            ContentValues values = new ContentValues();

            values.put(SalesOrderMenuSerialDatabaseModel.SALES_ORDER_ID, salesOrderMenuSerial.getSalesOrderMenu().getSalesOrder().getId());
            values.put(SalesOrderMenuSerialDatabaseModel.SALES_ORDER_MENU_ID, salesOrderMenuSerial.getSalesOrderMenu().getId());
            values.put(SalesOrderMenuSerialDatabaseModel.SERIAL, salesOrderMenuSerial.getSerialNumber());
            values.put(DefaultPersistenceModel.SYNC_STATUS, 0);
            values.put(DefaultPersistenceModel.STATUS_FLAG, 0);

            context.getContentResolver().update(dbUriSerial, values, SalesOrderMenuSerialDatabaseModel.ID + " = ? ", new String[]{salesOrderMenuSerial.getId()});
        }
    }

    public void save(List<SalesOrderMenuSerial> salesOrderMenuSerials){
        for (SalesOrderMenuSerial x : salesOrderMenuSerials){
            if (findSerialNumberById(x.getId()) == null) {
                ContentValues values = new ContentValues();
                values.put(DefaultPersistenceModel.ID, x.getId());
                values.put(SalesOrderMenuSerialDatabaseModel.SALES_ORDER_ID, x.getSalesOrderMenu().getSalesOrder().getId());
                values.put(SalesOrderMenuSerialDatabaseModel.SALES_ORDER_MENU_ID, x.getSalesOrderMenu().getId());
                values.put(SalesOrderMenuSerialDatabaseModel.SERIAL, x.getSerialNumber());
                values.put(DefaultPersistenceModel.SYNC_STATUS, 0);
                values.put(DefaultPersistenceModel.STATUS_FLAG, 0);

                context.getContentResolver().insert(dbUriSerial, values);
            } else {
                ContentValues values = new ContentValues();

                values.put(SalesOrderMenuSerialDatabaseModel.SALES_ORDER_ID, x.getSalesOrderMenu().getSalesOrder().getId());
                values.put(SalesOrderMenuSerialDatabaseModel.SALES_ORDER_MENU_ID, x.getSalesOrderMenu().getId());
                values.put(SalesOrderMenuSerialDatabaseModel.SERIAL, x.getSerialNumber());
                values.put(DefaultPersistenceModel.SYNC_STATUS, 0);
                values.put(DefaultPersistenceModel.STATUS_FLAG, 0);

                context.getContentResolver().update(dbUriSerial, values, SalesOrderMenuSerialDatabaseModel.ID + " = ? ", new String[]{x.getId()});
            }
        }
    }

    public List<SalesOrderMenuSerial> getActiveSerialNumberList() {
        String query = DefaultPersistenceModel.SYNC_STATUS + " = 0";

        List<SalesOrderMenuSerial> serials = new ArrayList<SalesOrderMenuSerial>();

        Cursor cursor = context.getContentResolver().query(dbUriSerial, null, query, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                SalesOrderMenuSerial serial = new SalesOrderMenuSerial();
                serial.setId(cursor.getString(cursor.getColumnIndex(SalesOrderMenuSerialDatabaseModel.ID)));
                serial.getSalesOrderMenu().setId(cursor.getString(cursor.getColumnIndex(SalesOrderMenuSerialDatabaseModel.SALES_ORDER_MENU_ID)));
                serial.setSerialNumber(cursor.getString(cursor.getColumnIndex(SalesOrderMenuSerialDatabaseModel.SERIAL)));
                serials.add(serial);
            }
        }

        cursor.close();
        return serials;
    }

    public List<SalesOrderMenuSerial> getSerialNumberListBySalesOrderMenuId(String SalesOrderMenuId) {
        String query = SalesOrderMenuSerialDatabaseModel.SALES_ORDER_MENU_ID + " = ? AND " + DefaultPersistenceModel.SYNC_STATUS + " = 0";
        String param[] = {SalesOrderMenuId};

        List<SalesOrderMenuSerial> serials = new ArrayList<SalesOrderMenuSerial>();

        Cursor cursor = context.getContentResolver().query(dbUriSerial, null, query, param, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                SalesOrderMenuSerial serial = new SalesOrderMenuSerial();
                serial.setId(cursor.getString(cursor.getColumnIndex(SalesOrderMenuSerialDatabaseModel.ID)));
                serial.getSalesOrderMenu().setId(cursor.getString(cursor.getColumnIndex(SalesOrderMenuSerialDatabaseModel.SALES_ORDER_MENU_ID)));
                serial.setSerialNumber(cursor.getString(cursor.getColumnIndex(SalesOrderMenuSerialDatabaseModel.SERIAL)));
                serials.add(serial);
            }
        }

        cursor.close();
        return serials;
    }

    public List<SalesOrderMenuSerial> getSerialNumberListBySalesOrderId(String salesOrderId) {
        String query = SalesOrderMenuSerialDatabaseModel.SALES_ORDER_ID + " = ? AND " + DefaultPersistenceModel.SYNC_STATUS + " = 0";
        String param[] = {salesOrderId};

        List<SalesOrderMenuSerial> serials = new ArrayList<SalesOrderMenuSerial>();

        Cursor cursor = context.getContentResolver().query(dbUriSerial, null, query, param, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                SalesOrderMenuSerial serial = new SalesOrderMenuSerial();
                serial.setId(cursor.getString(cursor.getColumnIndex(SalesOrderMenuSerialDatabaseModel.ID)));
                serial.getSalesOrderMenu().setId(cursor.getString(cursor.getColumnIndex(SalesOrderMenuSerialDatabaseModel.SALES_ORDER_MENU_ID)));
                serial.setSerialNumber(cursor.getString(cursor.getColumnIndex(SalesOrderMenuSerialDatabaseModel.SERIAL)));
                serials.add(serial);
            }
        }

        cursor.close();
        return serials;
    }

    public void deleteBySerialNumber(String serialNumber) {
        String query = SalesOrderMenuSerialDatabaseModel.SERIAL + " = ? ";
        String param[] = {serialNumber};

        context.getContentResolver().delete(dbUriSerial, query, param);
    }

    public void updateStatusFlag(String salesOrderMenuSerialId) {

        ContentValues values = new ContentValues();
        values.put(DefaultPersistenceModel.STATUS_FLAG, 1);

        context.getContentResolver().update(dbUriSerial, values, SalesOrderMenuSerialDatabaseModel.ID + " = ? ", new String[]{salesOrderMenuSerialId});

    }

}
