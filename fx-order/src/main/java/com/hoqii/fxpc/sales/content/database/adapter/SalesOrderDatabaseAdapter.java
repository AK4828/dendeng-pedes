package com.hoqii.fxpc.sales.content.database.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.hoqii.fxpc.sales.content.MidasContentProvider;
import com.hoqii.fxpc.sales.content.database.model.DefaultPersistenceModel;
import com.hoqii.fxpc.sales.content.database.model.OrderDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.SalesOrderDatabaseModel;
import com.hoqii.fxpc.sales.core.LogInformation;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.entity.SalesOrder;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;

import java.util.UUID;

/**
 * Created by meruvian on 24/07/15.
 */
public class SalesOrderDatabaseAdapter extends DefaultDatabaseAdapter {
    private Uri dbUriSalesOrder = Uri.parse(MidasContentProvider.CONTENT_PATH
            + MidasContentProvider.TABLES[21]);

    private Context context;
    private ContactDatabaseAdapter contactDatabaseAdapter;


    public SalesOrderDatabaseAdapter(Context context) {
        this.context = context;
        contactDatabaseAdapter = new ContactDatabaseAdapter(context);

    }

    public String saveSalesOrder(SalesOrder salesOrder) {
        UUID uuid = UUID.randomUUID();
        String id = String.valueOf(uuid);

        ContentValues values = new ContentValues();
        values.put(DefaultPersistenceModel.ID, id);
        values.put(DefaultPersistenceModel.SITE_ID, salesOrder.getLogInformation().getSite());
        values.put(DefaultPersistenceModel.CREATE_BY, salesOrder.getLogInformation().getCreateBy());
        values.put(DefaultPersistenceModel.CREATE_DATE, salesOrder.getLogInformation().getCreateDate().getTime());
        values.put(DefaultPersistenceModel.UPDATE_BY, salesOrder.getLogInformation().getLastUpdateBy());
        values.put(DefaultPersistenceModel.UPDATE_DATE, salesOrder.getLogInformation().getLastUpdateDate().getTime());
        values.put(DefaultPersistenceModel.STATUS_FLAG, 0);
        values.put(DefaultPersistenceModel.SYNC_STATUS, 0);

        values.put(SalesOrderDatabaseModel.SITE_FROM_ORDER_ID, salesOrder.getSiteFrom().getId());
        values.put(SalesOrderDatabaseModel.RECIEPT_NUMBER, salesOrder.getReceiptNumber());
        values.put(SalesOrderDatabaseModel.REF_ID, salesOrder.getRefId());
        values.put(SalesOrderDatabaseModel.STATUS, salesOrder.getStatus().name());

        context.getContentResolver().insert(dbUriSalesOrder, values);
        return id;
    }

    public String getSalesOrderId() {
        String query = OrderDatabaseModel.STATUS_FLAG + " = ? AND " + OrderDatabaseModel.SITE_ID+ " = ?";
        String[] params = {"0",AuthenticationUtils.getCurrentAuthentication().getSite().getId().toString()};
//        String query = OrderDatabaseModel.STATUS_FLAG + " = ? ";
//        String[] params = {"0"};

        Cursor cursor = context.getContentResolver().query(dbUriSalesOrder, null,
                query, params, null);

        String id = null;
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToLast();
                id = cursor.getString(cursor
                        .getColumnIndex(OrderDatabaseModel.ID));
            }
        }

        cursor.close();
        return id;
    }

    public SalesOrder findOrderById(String id) {

        String criteria = SalesOrderDatabaseModel.ID + " = ?";
        String[] parameter = { id };
        Cursor cursor = context.getContentResolver().query(dbUriSalesOrder, null,
                criteria, parameter, null);

        SalesOrder salesOrder = new SalesOrder();

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                LogInformation log = getLogInformationDefault(cursor);

                salesOrder.setLogInformation(log);
                salesOrder.setId(cursor.getString(cursor.getColumnIndex(SalesOrderDatabaseModel.ID)));
                salesOrder.setReceiptNumber(cursor.getString(cursor.getColumnIndex(SalesOrderDatabaseModel.RECIEPT_NUMBER)));
                salesOrder.getSiteFrom().setId(cursor.getString(cursor.getColumnIndex(SalesOrderDatabaseModel.SITE_FROM_ORDER_ID)));
                SalesOrder.SalesOrderStatus status = SalesOrder.SalesOrderStatus.valueOf(cursor.getString(cursor.getColumnIndex(SalesOrderDatabaseModel.STATUS)));
                salesOrder.setStatus(status);
                salesOrder.setName(cursor.getString(cursor.getColumnIndex(SalesOrderDatabaseModel.NAME)));
                salesOrder.setName(cursor.getString(cursor.getColumnIndex(SalesOrderDatabaseModel.EMAIL)));
                salesOrder.setName(cursor.getString(cursor.getColumnIndex(SalesOrderDatabaseModel.ADDRESS)));
                salesOrder.setName(cursor.getString(cursor.getColumnIndex(SalesOrderDatabaseModel.TELEPHONE)));
                salesOrder.getSiteFrom().setId(cursor.getString(cursor.getColumnIndex(OrderDatabaseModel.SITE_ORDER_ID)));
            }
        }
        cursor.close();

        return salesOrder;
    }




    public void updateSyncStatusById(String id) {
        ContentValues values = new ContentValues();
        values.put(OrderDatabaseModel.SYNC_STATUS, 1);
        values.put(OrderDatabaseModel.STATUS_FLAG, 1);

        context.getContentResolver().update(dbUriSalesOrder, values, OrderDatabaseModel.ID + " = ? ", new String[]{id});
    }



}
