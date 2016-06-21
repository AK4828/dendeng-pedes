package com.hoqii.fxpc.sales.content.database.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.hoqii.fxpc.sales.content.MidasContentProvider;
import com.hoqii.fxpc.sales.content.database.model.DefaultPersistenceModel;
import com.hoqii.fxpc.sales.content.database.model.SerialNumberDatabaseModel;
import com.hoqii.fxpc.sales.entity.OrderMenuSerial;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 1/5/16.
 */
public class SerialNumberDatabaseAdapter {

    private Uri dbUriSerial = Uri.parse(MidasContentProvider.CONTENT_PATH
            + MidasContentProvider.TABLES[19]);

    private Context context;

    public SerialNumberDatabaseAdapter(Context context) {
        this.context = context;
    }

    public OrderMenuSerial findSerialNumber(String id) {
        String criteria = SerialNumberDatabaseModel.ID + " = ? ";
        String param[] = {id};

        Cursor cursor = context.getContentResolver().query(dbUriSerial, null, criteria, param, null);

        OrderMenuSerial orderMenuSerial = null;

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                orderMenuSerial = new OrderMenuSerial();
                orderMenuSerial.setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.ID)));
                orderMenuSerial.getOrderMenu().getOrder().setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.OrderId)));
                orderMenuSerial.getOrderMenu().setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.OrderMenuId)));
                orderMenuSerial.getShipment().setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.ShipmentId)));
                orderMenuSerial.setSerialNumber(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.SerialNumber)));
            }
        }

        cursor.close();
        return orderMenuSerial;
    }

    public void save(List<OrderMenuSerial> orderMenuSerials) {
        for (OrderMenuSerial orderMenuSerial : orderMenuSerials) {

            if (findSerialNumber(orderMenuSerial.getId()) == null) {
                ContentValues values = new ContentValues();
                values.put(DefaultPersistenceModel.ID, orderMenuSerial.getId());
                values.put(SerialNumberDatabaseModel.OrderId, orderMenuSerial.getOrderMenu().getOrder().getId());
                values.put(SerialNumberDatabaseModel.OrderMenuId, orderMenuSerial.getOrderMenu().getId());
                values.put(SerialNumberDatabaseModel.SerialNumber, orderMenuSerial.getSerialNumber());
                values.put(SerialNumberDatabaseModel.ShipmentId, orderMenuSerial.getShipment().getId());
                values.put(DefaultPersistenceModel.SYNC_STATUS, 0);
                values.put(DefaultPersistenceModel.STATUS_FLAG, 0);

                context.getContentResolver().insert(dbUriSerial, values);
            } else {
                ContentValues values = new ContentValues();

                values.put(SerialNumberDatabaseModel.OrderId, orderMenuSerial.getOrderMenu().getOrder().getId());
                values.put(SerialNumberDatabaseModel.OrderMenuId, orderMenuSerial.getOrderMenu().getId());
                values.put(SerialNumberDatabaseModel.SerialNumber, orderMenuSerial.getSerialNumber());
                values.put(SerialNumberDatabaseModel.ShipmentId, orderMenuSerial.getShipment().getId());
                values.put(DefaultPersistenceModel.SYNC_STATUS, 0);
                values.put(DefaultPersistenceModel.STATUS_FLAG, 0);

                context.getContentResolver().update(dbUriSerial, values, SerialNumberDatabaseModel.ID + " = ? ", new String[]{orderMenuSerial.getId()});
            }
        }
    }

    public List<OrderMenuSerial> getSerialNumberListByOrderId(String orderId) {
        String query = SerialNumberDatabaseModel.OrderId + " = ? AND " + DefaultPersistenceModel.SYNC_STATUS + " = 0";
        String param[] = {orderId};

        List<OrderMenuSerial> orderMenuSerials = new ArrayList<OrderMenuSerial>();

        Cursor cursor = context.getContentResolver().query(dbUriSerial, null, query, param, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                OrderMenuSerial orderMenuSerial = new OrderMenuSerial();
                orderMenuSerial.setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.ID)));
                orderMenuSerial.getOrderMenu().getOrder().setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.OrderId)));
                orderMenuSerial.getOrderMenu().setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.OrderMenuId)));
                orderMenuSerial.setSerialNumber(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.SerialNumber)));

                orderMenuSerials.add(orderMenuSerial);
            }
        }

        cursor.close();
        return orderMenuSerials;
    }

    public List<OrderMenuSerial> getSerialNumberListByOrderIdAndhasSync(String orderId) {
        String query = SerialNumberDatabaseModel.OrderId + " = ? AND " + DefaultPersistenceModel.SYNC_STATUS + " = ? AND " + DefaultPersistenceModel.STATUS_FLAG + " = ? ";
        String param[] = {orderId, "1", "0"};

        List<OrderMenuSerial> orderMenuSerials = new ArrayList<OrderMenuSerial>();

        Cursor cursor = context.getContentResolver().query(dbUriSerial, null, query, param, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                OrderMenuSerial orderMenuSerial = new OrderMenuSerial();
                orderMenuSerial.setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.ID)));
                orderMenuSerial.getOrderMenu().getOrder().setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.OrderId)));
                orderMenuSerial.getOrderMenu().setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.OrderMenuId)));
                orderMenuSerial.setSerialNumber(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.SerialNumber)));

                orderMenuSerials.add(orderMenuSerial);
            }
        }

        cursor.close();
        return orderMenuSerials;
    }

    public List<OrderMenuSerial> getSerialNumberListByOrderIdAndOrderMenuIdAndHasSync(String orderId, String orderMenuId) {
        String query = SerialNumberDatabaseModel.OrderId + " = ? AND " + SerialNumberDatabaseModel.OrderMenuId + " = ? AND " + DefaultPersistenceModel.SYNC_STATUS + " = 1 AND "+DefaultPersistenceModel.STATUS_FLAG + " = 0 ";
        String param[] = {orderId, orderMenuId};

        List<OrderMenuSerial> orderMenuSerials = new ArrayList<OrderMenuSerial>();

        Cursor cursor = context.getContentResolver().query(dbUriSerial, null, query, param, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                OrderMenuSerial orderMenuSerial = new OrderMenuSerial();
                orderMenuSerial.setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.ID)));
                orderMenuSerial.getOrderMenu().getOrder().setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.OrderId)));
                orderMenuSerial.getOrderMenu().setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.OrderMenuId)));
                orderMenuSerial.setSerialNumber(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.SerialNumber)));

                orderMenuSerials.add(orderMenuSerial);
            }
        }

        cursor.close();
        return orderMenuSerials;
    }

    public List<OrderMenuSerial> getSerialNumberListByOrderIdAndOrderMenuId(String orderId, String orderMenuId) {
        String query = SerialNumberDatabaseModel.OrderId + " = ? AND " + SerialNumberDatabaseModel.OrderMenuId + " = ? AND " + DefaultPersistenceModel.SYNC_STATUS + " = 0";
        String param[] = {orderId, orderMenuId};

        List<OrderMenuSerial> orderMenuSerials = new ArrayList<OrderMenuSerial>();

        Cursor cursor = context.getContentResolver().query(dbUriSerial, null, query, param, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                OrderMenuSerial orderMenuSerial = new OrderMenuSerial();
                orderMenuSerial.setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.ID)));
                orderMenuSerial.getOrderMenu().getOrder().setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.OrderId)));
                orderMenuSerial.getOrderMenu().setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.OrderMenuId)));
                orderMenuSerial.setSerialNumber(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.SerialNumber)));

                orderMenuSerials.add(orderMenuSerial);
            }
        }

        cursor.close();
        return orderMenuSerials;
    }

    public OrderMenuSerial findSerialNumberByOrderId(String orderId) {
        String criteria = SerialNumberDatabaseModel.OrderId + " = ? ";
        String param[] = {orderId};

        Cursor cursor = context.getContentResolver().query(dbUriSerial, null, criteria, param, null);

        OrderMenuSerial orderMenuSerial = null;

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                orderMenuSerial = new OrderMenuSerial();
                orderMenuSerial.setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.ID)));
                orderMenuSerial.getOrderMenu().getOrder().setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.OrderId)));
                orderMenuSerial.getOrderMenu().setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.OrderMenuId)));
                orderMenuSerial.setSerialNumber(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.SerialNumber)));
            }
        }

        cursor.close();
        return orderMenuSerial;
    }

    public void deleteBySerialNumber(String serialNumber) {
        String query = SerialNumberDatabaseModel.SerialNumber + " = ? ";
        String param[] = {serialNumber};

        context.getContentResolver().delete(dbUriSerial, query, param);
    }

    public void updateStatus(String id, String refId, String ShipmentId) {

        ContentValues values = new ContentValues();

        values.put(DefaultPersistenceModel.SYNC_STATUS, 1);
        values.put(DefaultPersistenceModel.REF_ID, refId);
        values.put(SerialNumberDatabaseModel.ShipmentId, ShipmentId);

        context.getContentResolver().update(dbUriSerial, values, SerialNumberDatabaseModel.ID + " = ? ", new String[]{id});

    }

    public void updateStatusFlag(String orderMenuId) {

        ContentValues values = new ContentValues();
        values.put(DefaultPersistenceModel.STATUS_FLAG, 1);

        context.getContentResolver().update(dbUriSerial, values, SerialNumberDatabaseModel.OrderMenuId + " = ? ", new String[]{orderMenuId});

    }

    public void updateStatusByShipmentId(String shipmentId) {

        ContentValues values = new ContentValues();
        values.put(DefaultPersistenceModel.SYNC_STATUS, 1);
        values.put(DefaultPersistenceModel.STATUS_FLAG, 1);

        context.getContentResolver().update(dbUriSerial, values, SerialNumberDatabaseModel.ShipmentId + " = ? ", new String[]{shipmentId});

    }

}
