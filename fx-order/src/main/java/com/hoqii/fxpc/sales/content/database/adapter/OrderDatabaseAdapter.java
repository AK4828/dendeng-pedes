package com.hoqii.fxpc.sales.content.database.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.util.Log;

import com.hoqii.fxpc.sales.content.MidasContentProvider;
import com.hoqii.fxpc.sales.content.database.model.DefaultPersistenceModel;
import com.hoqii.fxpc.sales.content.database.model.OrderDatabaseModel;
import com.hoqii.fxpc.sales.core.LogInformation;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by meruvian on 24/07/15.
 */
public class OrderDatabaseAdapter extends DefaultDatabaseAdapter {
    private Uri dbUriOrder = Uri.parse(MidasContentProvider.CONTENT_PATH
            + MidasContentProvider.TABLES[4]);

    private Context context;
    private ContactDatabaseAdapter contactDatabaseAdapter;


    public OrderDatabaseAdapter(Context context) {
        this.context = context;
        contactDatabaseAdapter = new ContactDatabaseAdapter(context);

    }

    public String saveOrder(Order order) {
        UUID uuid = UUID.randomUUID();
        String id = String.valueOf(uuid);

        ContentValues values = new ContentValues();
        values.put(DefaultPersistenceModel.ID, id);
        values.put(DefaultPersistenceModel.SITE_ID, order.getLogInformation().getSite());
        values.put(DefaultPersistenceModel.CREATE_BY, order.getLogInformation().getCreateBy());
        values.put(DefaultPersistenceModel.CREATE_DATE, order.getLogInformation().getCreateDate().getTime());
        values.put(DefaultPersistenceModel.UPDATE_BY, order.getLogInformation().getLastUpdateBy());
        values.put(DefaultPersistenceModel.UPDATE_DATE, order.getLogInformation().getLastUpdateDate().getTime());
        values.put(DefaultPersistenceModel.STATUS_FLAG, 0);
        values.put(DefaultPersistenceModel.SYNC_STATUS, 0);

        values.put(OrderDatabaseModel.SITE_ORDER_ID, order.getSite().getId());
        values.put(OrderDatabaseModel.RECIEPT_NUMBER, order.getReceiptNumber());
        values.put(OrderDatabaseModel.ORDER_TYPE, order.getOrderType());
        values.put(OrderDatabaseModel.REF_ID, order.getRefId());
        values.put(OrderDatabaseModel.STATUS, order.getStatus().name());

        context.getContentResolver().insert(dbUriOrder, values);
        return id;
    }

    public void updateRefOrder(Order order) {
        ContentValues values = new ContentValues();
        values.put(DefaultPersistenceModel.UPDATE_DATE, order.getLogInformation().getLastUpdateDate().getTime());
//        values.put(OrderDatabaseModel.SITE_ID, order.getSite().getId());
        values.put(OrderDatabaseModel.RECIEPT_NUMBER, order.getReceiptNumber());
        values.put(OrderDatabaseModel.REF_ID, order.getRefId());
//        values.put(OrderDatabaseModel.CONTACTID, order.getContact().getId());

        context.getContentResolver().update(dbUriOrder, values,
                OrderDatabaseModel.ID + " = ?", new String[]{order.getId()});
    }

    public void updateSiteOrder(Order order) {
        ContentValues values = new ContentValues();
        values.put(OrderDatabaseModel.SITE_ORDER_ID, order.getSite().getId());

        context.getContentResolver().update(dbUriOrder, values,
                OrderDatabaseModel.ID + " = ?", new String[]{order.getId()});
    }

    public void updateBizOrder(Order order) {
        ContentValues values = new ContentValues();
//        values.put(OrderDatabaseModel.CONTACTID, order.getContact().getId());

        context.getContentResolver().update(dbUriOrder, values,
                OrderDatabaseModel.ID + " = ?", new String[]{order.getId()});
    }

    public void updateOrder(String orderId) {
        ContentValues values = new ContentValues();
        Order order = new Order();
        values.put(DefaultPersistenceModel.UPDATE_DATE, order.getLogInformation().getLastUpdateDate().getTime());
        values.put(OrderDatabaseModel.STATUS_FLAG, 1);

        context.getContentResolver().update(dbUriOrder, values,
                OrderDatabaseModel.ID + " = ?", new String[]{orderId});
    }

    public String getOrderId() {
        String query = OrderDatabaseModel.STATUS_FLAG + " = ? AND " + OrderDatabaseModel.SITE_ID+ " = ?";
        String[] params = {"0",AuthenticationUtils.getCurrentAuthentication().getSite().getId().toString()};
//        String query = OrderDatabaseModel.STATUS_FLAG + " = ? ";
//        String[] params = {"0"};

        Cursor cursor = context.getContentResolver().query(dbUriOrder, null,
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

    public String getLastOrderId() {
        String query = OrderDatabaseModel.STATUS_FLAG + " = 1";
        Cursor cursor = context.getContentResolver().query(dbUriOrder, null,
                query, null, null);

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

    public String getLastOrderIdByFlag(String token,  int flag) {
        String query = OrderDatabaseModel.CREATE_BY + " = ? AND " + OrderDatabaseModel.STATUS_FLAG + " = ?";
        String[] parameter = { token , String.valueOf(flag)};
        Cursor cursor = context.getContentResolver().query(dbUriOrder, null,
                query, parameter, null);

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

    public String getLastOrderId(String token) {
        String query = OrderDatabaseModel.CREATE_BY + " = ? AND " + OrderDatabaseModel.STATUS_FLAG + " = 1";
        String[] parameter = { token };
        Cursor cursor = context.getContentResolver().query(dbUriOrder, null,
                query, null, null);

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

    public String getLastReceipt() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");

        String query = OrderDatabaseModel.STATUS_FLAG + " = 1";
        Cursor cursor = context.getContentResolver().query(dbUriOrder, null,
                query, null, null);

        String receipt = null;
        Order order = null;

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToLast();

                order = new Order();
                com.hoqii.fxpc.sales.core.LogInformation log = getLogInformationDefault(cursor);


                order.setId(cursor.getString(cursor
                        .getColumnIndex(OrderDatabaseModel.ID)));
                order.setReceiptNumber(cursor.getString(cursor
                        .getColumnIndex(OrderDatabaseModel.RECIEPT_NUMBER)));
                order.setOrderType(cursor.getString(cursor
                        .getColumnIndex(OrderDatabaseModel.ORDER_TYPE)));
                order.setLogInformation(log);
            }
        }

        cursor.close();

        if(order == null) {
            receipt = "00001";
        } else {
            Date lastOrderDate = new Date(order.getLogInformation().getCreateDate().getTime());
            Date currentDate = new Date();

            String formatLastOrderDate = dateFormat.format(lastOrderDate);
            String formatCurrentDate = dateFormat.format(currentDate);

            if (!formatCurrentDate.equals(formatLastOrderDate)) {
                receipt = "00001";
            } else {
                String lastOrderReceipt = order.getReceiptNumber().substring(order.getReceiptNumber().length() - 5);
                int newOrderReceipt = Integer.valueOf(lastOrderReceipt) + 1;
                String newReceipt = String.valueOf(newOrderReceipt);

                String zeros = "";
                for (int a = newReceipt.length(); a < 5; a++) {
                    zeros += "0";
                }

                receipt = zeros + newReceipt;
            }
        }

        return receipt;
    }

    public Order findOrderById(String id) {

        String criteria = OrderDatabaseModel.ID + " = ?";
        String[] parameter = { id };
        Cursor cursor = context.getContentResolver().query(dbUriOrder, null,
                criteria, parameter, null);

        Order order = new Order();

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                LogInformation log = getLogInformationDefault(cursor);

                order.setLogInformation(log);
                order.setId(cursor.getString(cursor.getColumnIndex(OrderDatabaseModel.ID)));
                order.setRefId(cursor.getString(cursor.getColumnIndex(OrderDatabaseModel.REF_ID)));
                order.setReceiptNumber(cursor.getString(cursor.getColumnIndex(OrderDatabaseModel.RECIEPT_NUMBER)));
                order.setOrderType(cursor.getString(cursor.getColumnIndex(OrderDatabaseModel.ORDER_TYPE)));
                order.getSite().setId(cursor.getString(cursor.getColumnIndex(OrderDatabaseModel.SITE_ORDER_ID)));
//                order.getContact().setId(cursor.getString(cursor.getColumnIndex(OrderDatabaseModel.CONTACTID)));
            }
        }
        cursor.close();

        return order;
    }

    public List<Order> getOrders() {
        Cursor cursor = context.getContentResolver().query(dbUriOrder, null,
                null, null, null);

        List<Order> orders = new ArrayList<>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Order order = new Order();
                com.hoqii.fxpc.sales.core.LogInformation log = getLogInformationDefault(cursor);

                order.setLogInformation(log);
                order.setId(cursor.getString(cursor
                        .getColumnIndex(OrderDatabaseModel.ID)));
                order.setReceiptNumber(cursor.getString(cursor
                        .getColumnIndex(OrderDatabaseModel.RECIEPT_NUMBER)));
                order.setOrderType(cursor.getString(cursor
                        .getColumnIndex(OrderDatabaseModel.ORDER_TYPE)));
                orders.add(order);
            }
        }
        cursor.close();

        return orders;
    }

    public List<Order> getActiveOrdersBySiteId(String siteId) {
        String query = DefaultPersistenceModel.STATUS_FLAG + " = ? AND " + DefaultPersistenceModel.SITE_ID + " = ? ";
        String params[] = {"1", siteId};
        Cursor cursor = context.getContentResolver().query(dbUriOrder, null,
                query, params, DefaultPersistenceModel.CREATE_DATE + " DESC");


        List<Order> orders = new ArrayList<>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Order order = new Order();
                com.hoqii.fxpc.sales.core.LogInformation log = getLogInformationDefault(cursor);

                order.setLogInformation(log);
                order.setId(cursor.getString(cursor
                        .getColumnIndex(OrderDatabaseModel.ID)));
                order.setReceiptNumber(cursor.getString(cursor
                        .getColumnIndex(OrderDatabaseModel.RECIEPT_NUMBER)));
                order.setOrderType(cursor.getString(cursor
                        .getColumnIndex(OrderDatabaseModel.ORDER_TYPE)));

                orders.add(order);
            }
        }
        cursor.close();

        return orders;
    }


    public List<String> findAllIdOrder() {
        String query = OrderDatabaseModel.SYNC_STATUS + " = ?";
        String[] parameter = { "0" };

        Cursor cursor = context.getContentResolver().query(dbUriOrder, null, query, parameter, null);

        List<String> orderIdes = new ArrayList<String>();

        if(cursor != null) {
            if(cursor.getCount() > 0) {
                try {
                    while (cursor.moveToNext()) {
                        orderIdes.add(cursor.getString(cursor.getColumnIndex(OrderDatabaseModel.ID)));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        cursor.close();

        return orderIdes;
    }

    public void updateRefIdOrder(long createDate1, long createDate2) {
        ContentValues values = new ContentValues();
        values.put(OrderDatabaseModel.REF_ID, "");

        String where = OrderDatabaseModel.CREATE_DATE + " > " + createDate1 + " and " + OrderDatabaseModel.CREATE_DATE + " < " + createDate2;
        context.getContentResolver().update(dbUriOrder, values, where, null);
    }

    public void updateOrderTypeById(String id) {
        ContentValues values = new ContentValues();
        values.put(OrderDatabaseModel.ORDER_TYPE, "3");

        context.getContentResolver().update(dbUriOrder, values, OrderDatabaseModel.ID + " = ? ", new String[]{id});
    }

    public void updateOrderStatusById(String id, String status) {
        ContentValues values = new ContentValues();
        values.put(OrderDatabaseModel.STATUS, status);

        context.getContentResolver().update(dbUriOrder, values, OrderDatabaseModel.ID + " = ? ", new String[]{id});
    }

    public void updateOrdersStatusById(List<Order> orders, String status) {
        for (Order od : orders) {
            Log.d(getClass().getSimpleName(), "od.id: " + od.getId());
            ContentValues values = new ContentValues();
            values.put(OrderDatabaseModel.STATUS, status);

            context.getContentResolver().update(dbUriOrder, values, OrderDatabaseModel.ID + " = ? ", new String[]{ od.getId() });
        }
    }

    public void updateSyncStatusById(String id) {
        ContentValues values = new ContentValues();
        values.put(OrderDatabaseModel.SYNC_STATUS, 1);
        values.put(OrderDatabaseModel.STATUS_FLAG, 1);

        context.getContentResolver().update(dbUriOrder, values, OrderDatabaseModel.ID + " = ? ", new String[]{id});
    }

    public String getReceiptNumberByOrderId(String orderId){
        String receiptNumber = null;
        String query = OrderDatabaseModel.ID + " = ? ";
        String[] parameter = { orderId };
        Cursor cursor = context.getContentResolver().query(dbUriOrder, null,
                query, parameter, null);

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToLast();
                receiptNumber = cursor.getString(cursor
                        .getColumnIndex(OrderDatabaseModel.RECIEPT_NUMBER));
            }
        }

        cursor.close();
        return receiptNumber;
    }

}
