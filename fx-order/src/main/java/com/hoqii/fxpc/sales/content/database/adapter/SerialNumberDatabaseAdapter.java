package com.hoqii.fxpc.sales.content.database.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.hoqii.fxpc.sales.content.MidasContentProvider;
import com.hoqii.fxpc.sales.content.database.model.DefaultPersistenceModel;
import com.hoqii.fxpc.sales.content.database.model.SerialNumberDatabaseModel;
import com.hoqii.fxpc.sales.entity.SerialNumber;

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

    public SerialNumber findSerialNumber(String id) {
        String criteria = SerialNumberDatabaseModel.ID + " = ? ";
        String param[] = {id};

        Cursor cursor = context.getContentResolver().query(dbUriSerial, null, criteria, param, null);

        SerialNumber serialNumber = null;

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                serialNumber = new SerialNumber();
                serialNumber.setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.ID)));
                serialNumber.getOrderMenu().getOrder().setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.OrderId)));
                serialNumber.getOrderMenu().setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.OrderMenuId)));
                serialNumber.getShipment().setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.ShipmentId)));
                serialNumber.setSerialNumber(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.SerialNumber)));
            }
        }

        cursor.close();
        return serialNumber;
    }

    public void save(List<SerialNumber> serialNumbers) {
        for (SerialNumber serialNumber : serialNumbers) {

            if (findSerialNumber(serialNumber.getId()) == null) {
                ContentValues values = new ContentValues();
                values.put(DefaultPersistenceModel.ID, serialNumber.getId());
                values.put(SerialNumberDatabaseModel.OrderId, serialNumber.getOrderMenu().getOrder().getId());
                values.put(SerialNumberDatabaseModel.OrderMenuId, serialNumber.getOrderMenu().getId());
                values.put(SerialNumberDatabaseModel.SerialNumber, serialNumber.getSerialNumber());
                values.put(SerialNumberDatabaseModel.ShipmentId, serialNumber.getShipment().getId());
                values.put(DefaultPersistenceModel.SYNC_STATUS, 0);

                context.getContentResolver().insert(dbUriSerial, values);
            } else {
                ContentValues values = new ContentValues();

                values.put(SerialNumberDatabaseModel.OrderId, serialNumber.getOrderMenu().getOrder().getId());
                values.put(SerialNumberDatabaseModel.OrderMenuId, serialNumber.getOrderMenu().getId());
                values.put(SerialNumberDatabaseModel.SerialNumber, serialNumber.getSerialNumber());
                values.put(SerialNumberDatabaseModel.ShipmentId, serialNumber.getShipment().getId());
                values.put(DefaultPersistenceModel.SYNC_STATUS, 0);

                context.getContentResolver().update(dbUriSerial, values, SerialNumberDatabaseModel.ID + " = ? ", new String[]{serialNumber.getId()});
            }
        }
    }

    public List<SerialNumber> getSerialNumberListByOrderId(String orderId) {
        String query = SerialNumberDatabaseModel.OrderId + " = ? AND " + DefaultPersistenceModel.SYNC_STATUS + " = 0";
        String param[] = {orderId};

        List<SerialNumber> serialNumbers = new ArrayList<SerialNumber>();

        Cursor cursor = context.getContentResolver().query(dbUriSerial, null, query, param, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                SerialNumber serialNumber = new SerialNumber();
                serialNumber.setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.ID)));
                serialNumber.getOrderMenu().getOrder().setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.OrderId)));
                serialNumber.getOrderMenu().setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.OrderMenuId)));
                serialNumber.setSerialNumber(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.SerialNumber)));

                serialNumbers.add(serialNumber);
            }
        }

        cursor.close();
        return serialNumbers;
    }

    public List<SerialNumber> getSerialNumberListByOrderIdAndOrderMenuId(String orderId, String orderMenuId) {
        String query = SerialNumberDatabaseModel.OrderId + " = ? AND " + SerialNumberDatabaseModel.OrderMenuId + " = ? AND " + DefaultPersistenceModel.SYNC_STATUS + " = 0";
        String param[] = {orderId, orderMenuId};

        List<SerialNumber> serialNumbers = new ArrayList<SerialNumber>();

        Cursor cursor = context.getContentResolver().query(dbUriSerial, null, query, param, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                SerialNumber serialNumber = new SerialNumber();
                serialNumber.setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.ID)));
                serialNumber.getOrderMenu().getOrder().setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.OrderId)));
                serialNumber.getOrderMenu().setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.OrderMenuId)));
                serialNumber.setSerialNumber(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.SerialNumber)));

                serialNumbers.add(serialNumber);
            }
        }

        cursor.close();
        return serialNumbers;
    }

    public SerialNumber findSerialNumberByOrderId(String orderId) {
        String criteria = SerialNumberDatabaseModel.OrderId + " = ? ";
        String param[] = {orderId};

        Cursor cursor = context.getContentResolver().query(dbUriSerial, null, criteria, param, null);

        SerialNumber serialNumber = null;

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                serialNumber = new SerialNumber();
                serialNumber.setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.ID)));
                serialNumber.getOrderMenu().getOrder().setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.OrderId)));
                serialNumber.getOrderMenu().setId(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.OrderMenuId)));
                serialNumber.setSerialNumber(cursor.getString(cursor.getColumnIndex(SerialNumberDatabaseModel.SerialNumber)));
            }
        }

        cursor.close();
        return serialNumber;
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

    public void updateStatusByShipmentId(String shipmentId) {

        ContentValues values = new ContentValues();
        values.put(DefaultPersistenceModel.SYNC_STATUS, 1);

        context.getContentResolver().update(dbUriSerial, values, SerialNumberDatabaseModel.ShipmentId + " = ? ", new String[]{shipmentId});

    }

}
