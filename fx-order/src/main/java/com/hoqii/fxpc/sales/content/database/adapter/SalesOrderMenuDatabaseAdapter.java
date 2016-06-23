package com.hoqii.fxpc.sales.content.database.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

import com.hoqii.fxpc.sales.content.MidasContentProvider;
import com.hoqii.fxpc.sales.content.database.model.DefaultPersistenceModel;
import com.hoqii.fxpc.sales.content.database.model.OrderDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.SalesOrderMenuDatabaseModel;
import com.hoqii.fxpc.sales.core.LogInformation;
import com.hoqii.fxpc.sales.entity.SalesOrder;
import com.hoqii.fxpc.sales.entity.SalesOrderMenu;
import com.hoqii.fxpc.sales.entity.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by meruvian on 24/07/15.
 */
public class SalesOrderMenuDatabaseAdapter extends DefaultDatabaseAdapter {
    private Uri dbUriSalesOrderMenu = Uri.parse(MidasContentProvider.CONTENT_PATH
            + MidasContentProvider.TABLES[22]);

    private Context context;
    private ProductDatabaseAdapter productDbAdapter;
    
    public SalesOrderMenuDatabaseAdapter(Context context) {
        this.context = context;

        productDbAdapter = new ProductDatabaseAdapter(context);
    }

    public void saveSalesOrderMenu(SalesOrderMenu salesOrderMenu) {
        ContentValues contentValues = new ContentValues();

        if (salesOrderMenu.getId() != null) {
            contentValues.put(SalesOrderMenuDatabaseModel.UPDATE_BY, salesOrderMenu.getLogInformation()
                    .getLastUpdateBy());
            contentValues.put(SalesOrderMenuDatabaseModel.UPDATE_DATE, salesOrderMenu.getLogInformation()
                    .getLastUpdateDate().getTime());
            contentValues.put(SalesOrderMenuDatabaseModel.QUANTITY, salesOrderMenu.getQty());
            contentValues.put(SalesOrderMenuDatabaseModel.QUANTITY_SALES_ORDER, salesOrderMenu.getQtySalesOrder());

            contentValues.put(SalesOrderMenuDatabaseModel.DESC, salesOrderMenu.getDescription());
            contentValues.put(SalesOrderMenuDatabaseModel.STATUS, salesOrderMenu.getStatus().name());

            contentValues.put(DefaultPersistenceModel.SYNC_STATUS, 0);

            context.getContentResolver().update(dbUriSalesOrderMenu, contentValues,
                    SalesOrderMenuDatabaseModel.ID + " = ?", new String[]{salesOrderMenu.getId()});
        } else {
            UUID uuid = UUID.randomUUID();
            String id = String.valueOf(uuid);

            contentValues.put(SalesOrderMenuDatabaseModel.ID, id);
            contentValues.put(SalesOrderMenuDatabaseModel.CREATE_BY, salesOrderMenu.getLogInformation()
                    .getCreateBy());
            contentValues.put(SalesOrderMenuDatabaseModel.CREATE_DATE, salesOrderMenu.getLogInformation()
                    .getCreateDate().getTime());
            contentValues.put(SalesOrderMenuDatabaseModel.UPDATE_BY, salesOrderMenu.getLogInformation()
                    .getLastUpdateBy());
            contentValues.put(SalesOrderMenuDatabaseModel.UPDATE_DATE, salesOrderMenu.getLogInformation()
                    .getLastUpdateDate().getTime());
            contentValues.put(SalesOrderMenuDatabaseModel.SITE_ID, salesOrderMenu.getLogInformation().getSite());
            contentValues.put(SalesOrderMenuDatabaseModel.QUANTITY, salesOrderMenu.getQty());
            contentValues.put(SalesOrderMenuDatabaseModel.QUANTITY_SALES_ORDER, salesOrderMenu.getQtySalesOrder());
            contentValues.put(SalesOrderMenuDatabaseModel.PRODUCT_ID, salesOrderMenu.getProduct().getId());
            contentValues.put(SalesOrderMenuDatabaseModel.SALES_ORDER_ID, salesOrderMenu.getSalesOrder().getId());
            contentValues.put(SalesOrderMenuDatabaseModel.DESC, salesOrderMenu.getDescription());
            contentValues.put(SalesOrderMenuDatabaseModel.PRICE, salesOrderMenu.getSellPrice());
            contentValues.put(SalesOrderMenuDatabaseModel.STATUS, salesOrderMenu.getStatus().name());

            contentValues.put(DefaultPersistenceModel.SYNC_STATUS, 0);

            context.getContentResolver().insert(dbUriSalesOrderMenu, contentValues);
        }
    }

    public SalesOrderMenu findSalesOrderMenuByProductId(String productId) {
        String query = SalesOrderMenuDatabaseModel.PRODUCT_ID + " = ? AND " + SalesOrderMenuDatabaseModel.SYNC_STATUS + " = 0 ";
        String param[] = {productId};

        Cursor cursor = context.getContentResolver().query(dbUriSalesOrderMenu, null, query, param, null);
        SalesOrderMenu salesOrderMenu = null;

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                salesOrderMenu = new SalesOrderMenu();

                LogInformation log = getLogInformationDefault(cursor);
                Product product = productDbAdapter.findAllProductById(cursor.getString(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.PRODUCT_ID)));

                SalesOrder salesOrder = new SalesOrder();
                salesOrder.setId(cursor.getString(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.SALES_ORDER_ID)));

                salesOrderMenu.setLogInformation(log);
                salesOrderMenu.setId(cursor.getString(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.ID)));
                salesOrderMenu.setQty(cursor.getInt(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.QUANTITY)));
                salesOrderMenu.setQtySalesOrder(cursor.getInt(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.QUANTITY_SALES_ORDER)));
                salesOrderMenu.setProduct(product);
                salesOrderMenu.setSalesOrder(salesOrder);
                salesOrderMenu.setSellPrice(cursor.getLong(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.PRICE)));
                salesOrderMenu.setDescription(cursor.getString(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.DESC)));
            }
        }
        cursor.close();
        return salesOrderMenu;
    }

    public List<String> findSalesOrderMenuIdesByOrderId(String orderId) {
        String query = SalesOrderMenuDatabaseModel.SALES_ORDER_ID + " = ? ";
        String[] parameter = {orderId};

        Cursor cursor = context.getContentResolver().query(dbUriSalesOrderMenu, null, query, parameter, null);

        List<String> salesOrderMenuIdes = new ArrayList<String>();

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                try {
                    while (cursor.moveToNext()) {
                        salesOrderMenuIdes.add(cursor.getString(cursor.getColumnIndex(OrderDatabaseModel.ID)));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        cursor.close();

        return salesOrderMenuIdes;
    }

    public List<String> findSalesOrderMenuIdesByOrderIdActive(String orderId) {
        String query = SalesOrderMenuDatabaseModel.SALES_ORDER_ID + " = ? AND " + OrderDatabaseModel.SYNC_STATUS + " = ?";
        String[] parameter = {orderId, "0"};

        Cursor cursor = context.getContentResolver().query(dbUriSalesOrderMenu, null, query, parameter, null);

        List<String> salesOrderMenuIdes = new ArrayList<String>();

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                try {
                    while (cursor.moveToNext()) {
                        salesOrderMenuIdes.add(cursor.getString(cursor.getColumnIndex(OrderDatabaseModel.ID)));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        cursor.close();

        return salesOrderMenuIdes;
    }

    public SalesOrderMenu findSalesOrderMenuById(String id) {
        String criteria = SalesOrderMenuDatabaseModel.ID + " = ?";
        String[] parameter = {id};
        Cursor cursor = context.getContentResolver().query(dbUriSalesOrderMenu,
                null, criteria, parameter, null);

        SalesOrderMenu salesOrderMenu = new SalesOrderMenu();

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                LogInformation log = getLogInformationDefault(cursor);

//                ProductStore productStore = productStoreDbAdapter.findProductStoreById(cursor.getString(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.PRODUCT_STORE_ID)));
                Product product = productDbAdapter.findAllProductById(cursor.getString(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.PRODUCT_ID)));
//                productStore.setProduct(product);
//                ProductStore product = new ProductStore();
//                product.setId(cursor.getString(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.PRODUCT_STORE_ID)));

                SalesOrder salesOrder = new SalesOrder();
                salesOrder.setId(cursor.getString(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.SALES_ORDER_ID)));

                salesOrderMenu.setLogInformation(log);
                salesOrderMenu.setId(cursor.getString(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.ID)));
                salesOrderMenu.setQty(cursor.getInt(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.QUANTITY)));
                salesOrderMenu.setQtySalesOrder(cursor.getInt(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.QUANTITY_SALES_ORDER)));
                salesOrderMenu.setProduct(product);
                salesOrderMenu.setSalesOrder(salesOrder);
                salesOrderMenu.setSellPrice(cursor.getLong(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.PRICE)));
                salesOrderMenu.setDescription(cursor.getString(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.DESC)));

            }
        }
        cursor.close();

        return salesOrderMenu;
    }

    public List<SalesOrderMenu> findSalesOrderMenuByOrderId(String orderId) {
        String criteria = SalesOrderMenuDatabaseModel.SALES_ORDER_ID + " = ?";
        String[] parameter = {orderId};
        Cursor cursor = context.getContentResolver().query(dbUriSalesOrderMenu,
                null, criteria, parameter, null);

        List<SalesOrderMenu> salesOrderMenus = new ArrayList<>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
//                ProductStore productStore = productStoreDbAdapter.findProductStoreById(cursor
//                    .getString(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.PRODUCT_STORE_ID)));
                Product product = productDbAdapter.findAllProductById(cursor.getString(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.PRODUCT_ID)));
//                productStore.setProduct(product);

                SalesOrder salesOrder = new SalesOrder();
                salesOrder.setId(cursor.getString(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.SALES_ORDER_ID)));

                SalesOrderMenu salesOrderMenu = new SalesOrderMenu();
                salesOrderMenu.setId(cursor.getString(cursor
                        .getColumnIndex(SalesOrderMenuDatabaseModel.ID)));
                salesOrderMenu.setQty(cursor.getInt(cursor
                        .getColumnIndex(SalesOrderMenuDatabaseModel.QUANTITY)));
                salesOrderMenu.setQtySalesOrder(cursor.getInt(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.QUANTITY_SALES_ORDER)));
                salesOrderMenu.setProduct(product);
                salesOrderMenu.setSalesOrder(salesOrder);
                salesOrderMenu.setSellPrice(cursor.getLong(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.PRICE)));
                salesOrderMenu.setDescription(cursor.getString(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.DESC)));

                salesOrderMenus.add(salesOrderMenu);
            }
        }

        cursor.close();

        return salesOrderMenus;
    }

    public void deleteSalesOrderMenu(String menuId) {
        String criteria = SalesOrderMenuDatabaseModel.ID + " = ?";
        String[] parameter = {menuId};

        context.getContentResolver()
                .delete(dbUriSalesOrderMenu, criteria, parameter);

    }

    public SalesOrderMenu getSalesOrderMenuById(String salesOrderMenuId) {
        String criteria = SalesOrderMenuDatabaseModel.ID + " = ?";
        String[] parameter = {salesOrderMenuId};
        Cursor cursor = context.getContentResolver().query(dbUriSalesOrderMenu,
                null, criteria, parameter, null);

        SalesOrderMenu salesOrderMenu = new SalesOrderMenu();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Product product = new Product();
//                ProductStore productStore = productStoreDbAdapter.findProductStoreById(cursor
//                    .getString(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.PRODUCT_STORE_ID)));
                product = productDbAdapter.findAllProductById(cursor.getString(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.PRODUCT_ID)));
//                productStore.setProduct(product);

                SalesOrder salesOrder = new SalesOrder();
                salesOrder.setId((cursor.getString(cursor
                        .getColumnIndex(SalesOrderMenuDatabaseModel.SALES_ORDER_ID))));

                salesOrderMenu.setId(cursor.getString(cursor
                        .getColumnIndex(SalesOrderMenuDatabaseModel.ID)));
                salesOrderMenu.setQty(cursor.getInt(cursor
                        .getColumnIndex(SalesOrderMenuDatabaseModel.QUANTITY)));
                salesOrderMenu.setQtySalesOrder(cursor.getInt(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.QUANTITY_SALES_ORDER)));
                salesOrderMenu.setProduct(product);
                salesOrderMenu.setSalesOrder(salesOrder);
                salesOrderMenu.setSellPrice(cursor.getLong(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.PRICE)));
                salesOrderMenu.setDescription(cursor.getString(cursor.getColumnIndex(SalesOrderMenuDatabaseModel.DESC)));

            }
        }

        cursor.close();

        return salesOrderMenu;
    }

    public String getSalesOrderMenuId() {
        Cursor cursor = context.getContentResolver().query(dbUriSalesOrderMenu,
                null, null, null,
                SalesOrderMenuDatabaseModel.CREATE_DATE + " DESC LIMIT 1");

        String id = null;
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                id = cursor.getString(cursor
                        .getColumnIndex(SalesOrderMenuDatabaseModel.ID));
            }
        }

        cursor.close();

        return id;
    }

    public void updateSyncStatusById(String id) {
        ContentValues values = new ContentValues();
        values.put(OrderDatabaseModel.SYNC_STATUS, 1);

        context.getContentResolver().update(dbUriSalesOrderMenu, values, SalesOrderMenuDatabaseModel.ID + " = ? ", new String[]{id});
    }

}
