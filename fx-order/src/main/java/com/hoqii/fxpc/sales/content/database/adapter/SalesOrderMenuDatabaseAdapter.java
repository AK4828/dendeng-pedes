package com.hoqii.fxpc.sales.content.database.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

import com.hoqii.fxpc.sales.content.MidasContentProvider;
import com.hoqii.fxpc.sales.content.database.model.DefaultPersistenceModel;
import com.hoqii.fxpc.sales.content.database.model.OrderDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.OrderMenuDatabaseModel;
import com.hoqii.fxpc.sales.core.LogInformation;
import com.hoqii.fxpc.sales.entity.Order;
import com.hoqii.fxpc.sales.entity.OrderMenu;
import com.hoqii.fxpc.sales.entity.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by meruvian on 24/07/15.
 */
public class SalesOrderMenuDatabaseAdapter extends DefaultDatabaseAdapter {
    private Uri dbUriOrderMenu = Uri.parse(MidasContentProvider.CONTENT_PATH
            + MidasContentProvider.TABLES[5]);

    private Context context;
    private ProductDatabaseAdapter productDbAdapter;


    public SalesOrderMenuDatabaseAdapter(Context context) {
        this.context = context;

        productDbAdapter = new ProductDatabaseAdapter(context);
    }

    public void saveOrderMenu(OrderMenu orderMenu) {
        ContentValues contentValues = new ContentValues();

        if (orderMenu.getId() != null) {
            contentValues.put(OrderMenuDatabaseModel.UPDATE_BY, orderMenu.getLogInformation()
                    .getLastUpdateBy());
            contentValues.put(OrderMenuDatabaseModel.UPDATE_DATE, orderMenu.getLogInformation()
                    .getLastUpdateDate().getTime());
            contentValues.put(OrderMenuDatabaseModel.QUANTITY, orderMenu.getQty());
            contentValues.put(OrderMenuDatabaseModel.QUANTITY_ORDER, orderMenu.getQtyOrder());

            contentValues.put(OrderMenuDatabaseModel.DESC, orderMenu.getDescription());
            contentValues.put(OrderMenuDatabaseModel.STATUS, orderMenu.getStatus().name());
            contentValues.put(OrderMenuDatabaseModel.TYPE, orderMenu.getType());

            contentValues.put(DefaultPersistenceModel.SYNC_STATUS, 0);

            context.getContentResolver().update(dbUriOrderMenu, contentValues,
                    OrderMenuDatabaseModel.ID + " = ?", new String[]{orderMenu.getId()});
        } else {
            UUID uuid = UUID.randomUUID();
            String id = String.valueOf(uuid);

            contentValues.put(OrderMenuDatabaseModel.ID, id);
            contentValues.put(OrderMenuDatabaseModel.CREATE_BY, orderMenu.getLogInformation()
                    .getCreateBy());
            contentValues.put(OrderMenuDatabaseModel.CREATE_DATE, orderMenu.getLogInformation()
                    .getCreateDate().getTime());
            contentValues.put(OrderMenuDatabaseModel.UPDATE_BY, orderMenu.getLogInformation()
                    .getLastUpdateBy());
            contentValues.put(OrderMenuDatabaseModel.UPDATE_DATE, orderMenu.getLogInformation()
                    .getLastUpdateDate().getTime());
            contentValues.put(OrderMenuDatabaseModel.SITE_ID, orderMenu.getLogInformation().getSite());
            contentValues.put(OrderMenuDatabaseModel.QUANTITY, orderMenu.getQty());
            contentValues.put(OrderMenuDatabaseModel.QUANTITY_ORDER, orderMenu.getQtyOrder());
            contentValues.put(OrderMenuDatabaseModel.PRODUCT_ID, orderMenu.getProduct().getId());
            contentValues.put(OrderMenuDatabaseModel.ORDER_ID, orderMenu.getOrder().getId());
            contentValues.put(OrderMenuDatabaseModel.DESC, orderMenu.getDescription());
            contentValues.put(OrderMenuDatabaseModel.PRICE, orderMenu.getSellPrice());
            contentValues.put(OrderMenuDatabaseModel.STATUS, orderMenu.getStatus().name());
            contentValues.put(OrderMenuDatabaseModel.TYPE, orderMenu.getType());

            contentValues.put(DefaultPersistenceModel.SYNC_STATUS, 0);

            context.getContentResolver().insert(dbUriOrderMenu, contentValues);
        }
    }

    public OrderMenu findOrderMenuByProductId(String productId) {
        String query = OrderMenuDatabaseModel.PRODUCT_ID + " = ? AND " + OrderMenuDatabaseModel.SYNC_STATUS + " = 0 ";
        String param[] = {productId};

        Cursor cursor = context.getContentResolver().query(dbUriOrderMenu, null, query, param, null);
        OrderMenu orderMenu = null;

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                orderMenu = new OrderMenu();

                LogInformation log = getLogInformationDefault(cursor);
                Product product = productDbAdapter.findAllProductById(cursor.getString(cursor.getColumnIndex(OrderMenuDatabaseModel.PRODUCT_ID)));

                Order order = new Order();
                order.setId(cursor.getString(cursor.getColumnIndex(OrderMenuDatabaseModel.ORDER_ID)));

                orderMenu.setLogInformation(log);
                orderMenu.setId(cursor.getString(cursor.getColumnIndex(OrderMenuDatabaseModel.ID)));
                orderMenu.setQty(cursor.getInt(cursor.getColumnIndex(OrderMenuDatabaseModel.QUANTITY)));
                orderMenu.setQtyOrder(cursor.getInt(cursor.getColumnIndex(OrderMenuDatabaseModel.QUANTITY_ORDER)));
                orderMenu.setProduct(product);
                orderMenu.setOrder(order);
                orderMenu.setSellPrice(cursor.getLong(cursor.getColumnIndex(OrderMenuDatabaseModel.PRICE)));
                orderMenu.setDescription(cursor.getString(cursor.getColumnIndex(OrderMenuDatabaseModel.DESC)));
                orderMenu.setType(cursor.getString(cursor.getColumnIndex(OrderMenuDatabaseModel.TYPE)));
            }
        }
        cursor.close();
        return orderMenu;
    }

    public List<String> findOrderMenuIdesByOrderId(String orderId) {
        String query = OrderMenuDatabaseModel.ORDER_ID + " = ? ";
        String[] parameter = {orderId};

        Cursor cursor = context.getContentResolver().query(dbUriOrderMenu, null, query, parameter, null);

        List<String> orderMenuIdes = new ArrayList<String>();

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                try {
                    while (cursor.moveToNext()) {
                        orderMenuIdes.add(cursor.getString(cursor.getColumnIndex(OrderDatabaseModel.ID)));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        cursor.close();

        return orderMenuIdes;
    }

    public List<String> findOrderMenuIdesByOrderIdActive(String orderId) {
        String query = OrderMenuDatabaseModel.ORDER_ID + " = ? AND " + OrderDatabaseModel.SYNC_STATUS + " = ?";
        String[] parameter = {orderId, "0"};

        Cursor cursor = context.getContentResolver().query(dbUriOrderMenu, null, query, parameter, null);

        List<String> orderMenuIdes = new ArrayList<String>();

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                try {
                    while (cursor.moveToNext()) {
                        orderMenuIdes.add(cursor.getString(cursor.getColumnIndex(OrderDatabaseModel.ID)));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        cursor.close();

        return orderMenuIdes;
    }

    public OrderMenu findOrderMenuById(String id) {
        String criteria = OrderMenuDatabaseModel.ID + " = ?";
        String[] parameter = {id};
        Cursor cursor = context.getContentResolver().query(dbUriOrderMenu,
                null, criteria, parameter, null);

        OrderMenu orderMenu = new OrderMenu();

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                LogInformation log = getLogInformationDefault(cursor);

//                ProductStore productStore = productStoreDbAdapter.findProductStoreById(cursor.getString(cursor.getColumnIndex(OrderMenuDatabaseModel.PRODUCT_STORE_ID)));
                Product product = productDbAdapter.findAllProductById(cursor.getString(cursor.getColumnIndex(OrderMenuDatabaseModel.PRODUCT_ID)));
//                productStore.setProduct(product);
//                ProductStore product = new ProductStore();
//                product.setId(cursor.getString(cursor.getColumnIndex(OrderMenuDatabaseModel.PRODUCT_STORE_ID)));

                Order order = new Order();
                order.setId(cursor.getString(cursor.getColumnIndex(OrderMenuDatabaseModel.ORDER_ID)));

                orderMenu.setLogInformation(log);
                orderMenu.setId(cursor.getString(cursor.getColumnIndex(OrderMenuDatabaseModel.ID)));
                orderMenu.setQty(cursor.getInt(cursor.getColumnIndex(OrderMenuDatabaseModel.QUANTITY)));
                orderMenu.setQtyOrder(cursor.getInt(cursor.getColumnIndex(OrderMenuDatabaseModel.QUANTITY_ORDER)));
                orderMenu.setProduct(product);
                orderMenu.setOrder(order);
                orderMenu.setSellPrice(cursor.getLong(cursor.getColumnIndex(OrderMenuDatabaseModel.PRICE)));
                orderMenu.setDescription(cursor.getString(cursor.getColumnIndex(OrderMenuDatabaseModel.DESC)));
                orderMenu.setType(cursor.getString(cursor.getColumnIndex(OrderMenuDatabaseModel.TYPE)));
            }
        }
        cursor.close();

        return orderMenu;
    }

    public List<OrderMenu> findOrderMenuByOrderId(String orderId) {
        String criteria = OrderMenuDatabaseModel.ORDER_ID + " = ?";
        String[] parameter = {orderId};
        Cursor cursor = context.getContentResolver().query(dbUriOrderMenu,
                null, criteria, parameter, null);

        List<OrderMenu> orderMenus = new ArrayList<>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
//                ProductStore productStore = productStoreDbAdapter.findProductStoreById(cursor
//                    .getString(cursor.getColumnIndex(OrderMenuDatabaseModel.PRODUCT_STORE_ID)));
                Product product = productDbAdapter.findAllProductById(cursor.getString(cursor.getColumnIndex(OrderMenuDatabaseModel.PRODUCT_ID)));
//                productStore.setProduct(product);

                Order order = new Order();
                order.setId(cursor.getString(cursor.getColumnIndex(OrderMenuDatabaseModel.ORDER_ID)));

                OrderMenu orderMenu = new OrderMenu();
                orderMenu.setId(cursor.getString(cursor
                        .getColumnIndex(OrderMenuDatabaseModel.ID)));
                orderMenu.setQty(cursor.getInt(cursor
                        .getColumnIndex(OrderMenuDatabaseModel.QUANTITY)));
                orderMenu.setQtyOrder(cursor.getInt(cursor.getColumnIndex(OrderMenuDatabaseModel.QUANTITY_ORDER)));
                orderMenu.setProduct(product);
                orderMenu.setOrder(order);
                orderMenu.setSellPrice(cursor.getLong(cursor.getColumnIndex(OrderMenuDatabaseModel.PRICE)));
                orderMenu.setDescription(cursor.getString(cursor.getColumnIndex(OrderMenuDatabaseModel.DESC)));
                orderMenu.setType(cursor.getString(cursor.getColumnIndex(OrderMenuDatabaseModel.TYPE)));

                orderMenus.add(orderMenu);
            }
        }

        cursor.close();

        return orderMenus;
    }

    public void deleteOrderMenu(String menuId) {
        String criteria = OrderMenuDatabaseModel.ID + " = ?";
        String[] parameter = {menuId};

        context.getContentResolver()
                .delete(dbUriOrderMenu, criteria, parameter);

    }

    public OrderMenu getOrderMenuById(String orderMenuId) {
        String criteria = OrderMenuDatabaseModel.ID + " = ?";
        String[] parameter = {orderMenuId};
        Cursor cursor = context.getContentResolver().query(dbUriOrderMenu,
                null, criteria, parameter, null);

        OrderMenu orderMenu = new OrderMenu();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Product product = new Product();
//                ProductStore productStore = productStoreDbAdapter.findProductStoreById(cursor
//                    .getString(cursor.getColumnIndex(OrderMenuDatabaseModel.PRODUCT_STORE_ID)));
                product = productDbAdapter.findAllProductById(cursor.getString(cursor.getColumnIndex(OrderMenuDatabaseModel.PRODUCT_ID)));
//                productStore.setProduct(product);

                Order order = new Order();
                order.setId((cursor.getString(cursor
                        .getColumnIndex(OrderMenuDatabaseModel.ORDER_ID))));

                orderMenu.setId(cursor.getString(cursor
                        .getColumnIndex(OrderMenuDatabaseModel.ID)));
                orderMenu.setQty(cursor.getInt(cursor
                        .getColumnIndex(OrderMenuDatabaseModel.QUANTITY)));
                orderMenu.setQtyOrder(cursor.getInt(cursor.getColumnIndex(OrderMenuDatabaseModel.QUANTITY_ORDER)));
                orderMenu.setProduct(product);
                orderMenu.setOrder(order);
                orderMenu.setSellPrice(cursor.getLong(cursor.getColumnIndex(OrderMenuDatabaseModel.PRICE)));
                orderMenu.setDescription(cursor.getString(cursor.getColumnIndex(OrderMenuDatabaseModel.DESC)));
                orderMenu.setType(cursor.getString(cursor.getColumnIndex(OrderMenuDatabaseModel.TYPE)));

            }
        }

        cursor.close();

        return orderMenu;
    }

    public String getOrderMenuId() {
        Cursor cursor = context.getContentResolver().query(dbUriOrderMenu,
                null, null, null,
                OrderMenuDatabaseModel.CREATE_DATE + " DESC LIMIT 1");

        String id = null;
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                id = cursor.getString(cursor
                        .getColumnIndex(OrderMenuDatabaseModel.ID));
            }
        }

        cursor.close();

        return id;
    }

    public void updateSyncStatusById(String id) {
        ContentValues values = new ContentValues();
        values.put(OrderDatabaseModel.SYNC_STATUS, 1);

        context.getContentResolver().update(dbUriOrderMenu, values, OrderMenuDatabaseModel.ID + " = ? ", new String[]{id});
    }

}
