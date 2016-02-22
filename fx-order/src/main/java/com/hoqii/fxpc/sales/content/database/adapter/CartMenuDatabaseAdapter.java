package com.hoqii.fxpc.sales.content.database.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.util.Log;

import com.hoqii.fxpc.sales.content.MidasContentProvider;
import com.hoqii.fxpc.sales.content.database.model.DefaultPersistenceModel;
import com.hoqii.fxpc.sales.content.database.model.CartDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.CartMenuDatabaseModel;
import com.hoqii.fxpc.sales.entity.Cart;
import com.hoqii.fxpc.sales.entity.CartMenu;
import com.hoqii.fxpc.sales.entity.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by meruvian on 24/07/15.
 */
public class CartMenuDatabaseAdapter {
    private Uri dbUriCartMenu = Uri.parse(MidasContentProvider.CONTENT_PATH
            + MidasContentProvider.TABLES[9]);

    private Context context;
    private ProductDatabaseAdapter productDbAdapter;

    public CartMenuDatabaseAdapter(Context context) {
        this.context = context;

        productDbAdapter = new ProductDatabaseAdapter(context);
    }

    public void saveCartMenu(CartMenu cartMenu) {
        ContentValues contentValues = new ContentValues();

        if (cartMenu.getId() != null) {

            Log.d("2222", "222");
            contentValues.put(CartMenuDatabaseModel.UPDATE_BY, cartMenu
                    .getLogInformation().getLastUpdateBy());
            contentValues.put(CartMenuDatabaseModel.UPDATE_DATE, cartMenu
                    .getLogInformation().getLastUpdateDate().getTime());
            contentValues.put(CartMenuDatabaseModel.QUANTITY,
                    cartMenu.getQty());
            contentValues.put(CartMenuDatabaseModel.DESC,
                    cartMenu.getDescription());
            contentValues.put(DefaultPersistenceModel.SYNC_STATUS, 0);

            context.getContentResolver().update(dbUriCartMenu, contentValues,
                    CartMenuDatabaseModel.ID + " = ?",
                    new String[] { cartMenu.getId() });
        } else {
            UUID uuid = UUID.randomUUID();
            String id = String.valueOf(uuid);
            Log.d("11", "111");

            contentValues.put(CartMenuDatabaseModel.ID, id);
            contentValues.put(CartMenuDatabaseModel.CREATE_BY, cartMenu
                    .getLogInformation().getCreateBy());
            contentValues.put(CartMenuDatabaseModel.CREATE_DATE, cartMenu
                    .getLogInformation().getCreateDate().getTime());
            contentValues.put(CartMenuDatabaseModel.UPDATE_BY, cartMenu
                    .getLogInformation().getLastUpdateBy());
            contentValues.put(CartMenuDatabaseModel.UPDATE_DATE, cartMenu
                    .getLogInformation().getLastUpdateDate().getTime());
            contentValues.put(CartMenuDatabaseModel.QUANTITY,
                    cartMenu.getQty());
            contentValues.put(CartMenuDatabaseModel.PRODUCT_ID, cartMenu.getProduct().getId());
            contentValues.put(CartMenuDatabaseModel.CART_ID, cartMenu.getCart().getId());
            contentValues.put(CartMenuDatabaseModel.DESC, cartMenu.getDescription());
            contentValues.put(CartMenuDatabaseModel.PRICE, cartMenu.getSellPrice());
            contentValues.put(DefaultPersistenceModel.SYNC_STATUS, 0);

            context.getContentResolver().insert(dbUriCartMenu, contentValues);
        }
    }

    public List<String> findCartMenuIdesByCartId(String cartId) {
        String query = CartMenuDatabaseModel.CART_ID + " = ?";
        String[] parameter = { cartId };

        Cursor cursor = context.getContentResolver().query(dbUriCartMenu, null, query, parameter, null);

        List<String> cartMenuIdes = new ArrayList<String>();

        if(cursor != null) {
            if(cursor.getCount() > 0) {
                try {
                    while (cursor.moveToNext()) {
                        cartMenuIdes.add(cursor.getString(cursor.getColumnIndex(CartDatabaseModel.ID)));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        cursor.close();

        return cartMenuIdes;
    }

    public CartMenu findCartMenuById(String id) {
        String criteria = CartMenuDatabaseModel.ID + " = ?";
        String[] parameter = { id };
        Cursor cursor = context.getContentResolver().query(dbUriCartMenu,
                null, criteria, parameter, null);

        CartMenu cartMenu = new CartMenu();

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                Product product = productDbAdapter.findAllProductById(cursor.getString(cursor.getColumnIndex(CartMenuDatabaseModel.PRODUCT_ID)));
//                product.setId();

                Cart cart = new Cart();
                cart.setId(cursor.getString(cursor.getColumnIndex(CartMenuDatabaseModel.CART_ID)));

                cartMenu.setId(cursor.getString(cursor
                        .getColumnIndex(CartMenuDatabaseModel.ID)));
                cartMenu.setQty(cursor.getInt(cursor
                        .getColumnIndex(CartMenuDatabaseModel.QUANTITY)));
                cartMenu.setProduct(product);
                cartMenu.setCart(cart);
                cartMenu.setSellPrice(cursor.getLong(cursor.getColumnIndex(CartMenuDatabaseModel.PRICE)));
                cartMenu.setDescription(cursor.getString(cursor.getColumnIndex(CartMenuDatabaseModel.DESC)));
            }
        }

        cursor.close();

        return cartMenu;
    }

    public List<CartMenu> findCartMenuByCartId(String cartId) {
        String criteria = CartMenuDatabaseModel.CART_ID + " = ?";
        String[] parameter = { cartId };
        Cursor cursor = context.getContentResolver().query(dbUriCartMenu,
                null, criteria, parameter, null);

        List<CartMenu> cartMenus = new ArrayList<>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Product product = productDbAdapter.findAllProductById(cursor.getString(cursor.getColumnIndex(CartMenuDatabaseModel.PRODUCT_ID)));
//                product.setId();

                Cart cart = new Cart();
                cart.setId(cursor.getString(cursor.getColumnIndex(CartMenuDatabaseModel.CART_ID)));

                CartMenu cartMenu = new CartMenu();
                cartMenu.setId(cursor.getString(cursor
                        .getColumnIndex(CartMenuDatabaseModel.ID)));
                cartMenu.setQty(cursor.getInt(cursor
                        .getColumnIndex(CartMenuDatabaseModel.QUANTITY)));
                cartMenu.setProduct(product);
                cartMenu.setCart(cart);
                cartMenu.setSellPrice(cursor.getLong(cursor.getColumnIndex(CartMenuDatabaseModel.PRICE)));
                cartMenu.setDescription(cursor.getString(cursor.getColumnIndex(CartMenuDatabaseModel.DESC)));

                cartMenus.add(cartMenu);
            }
        }

        cursor.close();

        return cartMenus;
    }

    public void deleteCartMenu(String menuId) {
        String criteria = CartMenuDatabaseModel.ID + " = ?";
        String[] parameter = { menuId };

        context.getContentResolver()
                .delete(dbUriCartMenu, criteria, parameter);

    }

    public CartMenu getCartMenuById(String cartMenuId) {
        String criteria = CartMenuDatabaseModel.ID + " = ?";
        String[] parameter = { cartMenuId };
        Cursor cursor = context.getContentResolver().query(dbUriCartMenu,
                null, criteria, parameter, null);

        CartMenu cartMenu = new CartMenu();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Product product = new Product();
                product.setId((cursor.getString(cursor
                        .getColumnIndex(CartMenuDatabaseModel.PRODUCT_ID))));

                Cart cart = new Cart();
                cart.setId((cursor.getString(cursor
                        .getColumnIndex(CartMenuDatabaseModel.CART_ID))));

                cartMenu.setId(cursor.getString(cursor
                        .getColumnIndex(CartMenuDatabaseModel.ID)));
                cartMenu.setQty(cursor.getInt(cursor
                        .getColumnIndex(CartMenuDatabaseModel.QUANTITY)));
                cartMenu.setProduct(product);
                cartMenu.setCart(cart);
                cartMenu.setSellPrice(cursor.getLong(cursor.getColumnIndex(CartMenuDatabaseModel.PRICE)));
                cartMenu.setDescription(cursor.getString(cursor.getColumnIndex(CartMenuDatabaseModel.DESC)));

            }
        }

        cursor.close();

        return cartMenu;
    }

    public String getCartMenuId() {
        Cursor cursor = context.getContentResolver().query(dbUriCartMenu,
                null, null, null,
                CartMenuDatabaseModel.CREATE_DATE + " DESC LIMIT 1");

        String id = null;
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                id = cursor.getString(cursor
                        .getColumnIndex(CartMenuDatabaseModel.ID));
            }
        }

        cursor.close();

        return id;
    }

    public void updateSyncStatusById(String id) {
        ContentValues values = new ContentValues();
        values.put(CartDatabaseModel.SYNC_STATUS, 1);

        context.getContentResolver().update(dbUriCartMenu, values, CartDatabaseModel.ID + " = ? ", new String[] { id });
    }

}
