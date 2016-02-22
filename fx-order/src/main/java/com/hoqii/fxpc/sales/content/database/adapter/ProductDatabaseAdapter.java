package com.hoqii.fxpc.sales.content.database.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.content.MidasContentProvider;
import com.hoqii.fxpc.sales.content.database.model.ProductDatabaseModel;
import com.hoqii.fxpc.sales.content.database.model.ProductStoreDatabaseModel;
import com.hoqii.fxpc.sales.entity.Category;
import com.hoqii.fxpc.sales.entity.Product;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by meruvian on 29/01/15.
 */
public class ProductDatabaseAdapter {
    private Uri dbUriProduct = Uri.parse(MidasContentProvider.CONTENT_PATH
            + MidasContentProvider.TABLES[1]);

    private Context context;

    public ProductDatabaseAdapter(Context context) {
        this.context = context;
    }

    public Product findActiveProductById(String id) {
        String criteria = ProductDatabaseModel.ID + " = ?  AND "
                + ProductDatabaseModel.STATUS_FLAG + " = " + SignageVariables.ACTIVE;

        Cursor cursor = context.getContentResolver().query(dbUriProduct, null,
                criteria, new String[] { id }, null);

        Product product = null;

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                Category parentCategory = new Category();
                parentCategory.setId(cursor.getString(cursor
                        .getColumnIndex(ProductDatabaseModel.PARENT_CATEGORY_ID)));

                Category category = new Category();
                category.setId(cursor.getString(cursor
                        .getColumnIndex(ProductDatabaseModel.CATEGORY_ID)));

                product = new Product();
                product.setId(cursor.getString(cursor.getColumnIndex(ProductDatabaseModel.ID)));
                product.setName(cursor.getString(cursor.getColumnIndex(ProductDatabaseModel.NAME)));
                product.setSellPrice(cursor.getLong(cursor.getColumnIndex(ProductDatabaseModel.PRICE)));
                product.setDescription(cursor.getString(cursor.getColumnIndex(ProductDatabaseModel.DESCRIPTION)));
                product.setReward(cursor.getDouble(cursor.getColumnIndex(ProductDatabaseModel.REWARD)));

                product.setCategory(category);
                product.setParentCategory(parentCategory);

            }
        }

        cursor.close();

        return product;
    }

    public Product findAllProductById(String id) {
        String criteria = ProductDatabaseModel.ID + " = ? ";

        Cursor cursor = context.getContentResolver().query(dbUriProduct, null,
                criteria, new String[] { id }, null);

        Product product = null;

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                Category parentCategory = new Category();
                parentCategory.setId(cursor.getString(cursor
                        .getColumnIndex(ProductDatabaseModel.PARENT_CATEGORY_ID)));

                Category category = new Category();
                category.setId(cursor.getString(cursor
                        .getColumnIndex(ProductDatabaseModel.CATEGORY_ID)));

                product = new Product();
                product.setId(cursor.getString(cursor.getColumnIndex(ProductDatabaseModel.ID)));
                product.setName(cursor.getString(cursor.getColumnIndex(ProductDatabaseModel.NAME)));
                product.setSellPrice(cursor.getLong(cursor.getColumnIndex(ProductDatabaseModel.PRICE)));
                product.setDescription(cursor.getString(cursor.getColumnIndex(ProductDatabaseModel.DESCRIPTION)));
                product.setReward(cursor.getDouble(cursor.getColumnIndex(ProductDatabaseModel.REWARD)));

                product.setCategory(category);
                product.setParentCategory(parentCategory);

            }
        }

        cursor.close();

        return product;
    }

    public void saveProduct(Product product) {
        ContentValues values = new ContentValues();

        if(findAllProductById(product.getId()) == null) {
            values.put(ProductDatabaseModel.ID, product.getId());
            values.put(ProductDatabaseModel.NAME, product.getName());
            values.put(ProductDatabaseModel.PRICE, product.getSellPrice());
            values.put(ProductDatabaseModel.PARENT_CATEGORY_ID, product
                .getParentCategory().getId());
            values.put(ProductDatabaseModel.CATEGORY_ID, product.getCategory().getId());
            values.put(ProductDatabaseModel.COMPOSITION_STATUS, product.getcStats());
            values.put(ProductDatabaseModel.SELL_ABLE, product.getSellAble());
            values.put(ProductDatabaseModel.UOM_ID, product.getUom().getId());
            values.put(ProductDatabaseModel.CODE, product.getCode());
            values.put(ProductDatabaseModel.FG, product.getFg());
            values.put(ProductDatabaseModel.PRODUCT_VALUE, product.getProductValue());
            values.put(ProductDatabaseModel.MIN_QUANTITY, product.getMinQuantity());
            values.put(ProductDatabaseModel.MAX_QUANTITY, product.getMaxQuantity());
            values.put(ProductDatabaseModel.IMAGE, product.getImage());
            values.put(ProductDatabaseModel.DESCRIPTION, product.getDescription());
            values.put(ProductDatabaseModel.STATUS_FLAG , 1);
            values.put(ProductDatabaseModel.REWARD, product.getReward());

            context.getContentResolver().insert(dbUriProduct, values);
        } else {
            values.put(ProductDatabaseModel.NAME, product.getName());
            values.put(ProductDatabaseModel.PRICE, product.getSellPrice());
            values.put(ProductDatabaseModel.PARENT_CATEGORY_ID, product
                .getParentCategory().getId());
            values.put(ProductDatabaseModel.CATEGORY_ID, product.getCategory().getId());
            values.put(ProductDatabaseModel.COMPOSITION_STATUS, product.getcStats());
            values.put(ProductDatabaseModel.SELL_ABLE, product.getSellAble());
            values.put(ProductDatabaseModel.UOM_ID, product.getUom().getId());
            values.put(ProductDatabaseModel.CODE, product.getCode());
            values.put(ProductDatabaseModel.FG, product.getFg());
            values.put(ProductDatabaseModel.PRODUCT_VALUE, product.getProductValue());
            values.put(ProductDatabaseModel.MIN_QUANTITY, product.getMinQuantity());
            values.put(ProductDatabaseModel.MAX_QUANTITY, product.getMaxQuantity());
            values.put(ProductDatabaseModel.IMAGE, product.getImage());
            values.put(ProductDatabaseModel.DESCRIPTION, product.getDescription());
            values.put(ProductDatabaseModel.STATUS_FLAG , 1);
            values.put(ProductDatabaseModel.REWARD, product.getReward());
            context.getContentResolver().update(dbUriProduct, values, ProductDatabaseModel.ID + " = ?", new String[] { product.getId() });
        }
    }

    public void saveProducts(List<Product> products) {
        for (Product product : products) {
            ContentValues values = new ContentValues();

            if(findAllProductById(product.getId()) == null) {
                values.put(ProductDatabaseModel.ID, product.getId());
                values.put(ProductDatabaseModel.NAME, product.getName());
                values.put(ProductDatabaseModel.PRICE, product.getSellPrice());
                values.put(ProductDatabaseModel.PARENT_CATEGORY_ID, product
                        .getParentCategory().getId());
                values.put(ProductDatabaseModel.CATEGORY_ID, product.getCategory().getId());
                values.put(ProductDatabaseModel.COMPOSITION_STATUS, product.getcStats());
                values.put(ProductDatabaseModel.SELL_ABLE, product.getSellAble());
                values.put(ProductDatabaseModel.UOM_ID, product.getUom().getId());
                values.put(ProductDatabaseModel.CODE, product.getCode());
                values.put(ProductDatabaseModel.FG, product.getFg());
                values.put(ProductDatabaseModel.PRODUCT_VALUE, product.getProductValue());
                values.put(ProductDatabaseModel.MIN_QUANTITY, product.getMinQuantity());
                values.put(ProductDatabaseModel.MAX_QUANTITY, product.getMaxQuantity());
                values.put(ProductDatabaseModel.IMAGE, product.getImage());
                values.put(ProductDatabaseModel.DESCRIPTION, product.getDescription());
                values.put(ProductDatabaseModel.REWARD, product.getReward());

                values.put(ProductDatabaseModel.STATUS_FLAG , 1);

                context.getContentResolver().insert(dbUriProduct, values);
            } else {
                values.put(ProductDatabaseModel.NAME, product.getName());
                values.put(ProductDatabaseModel.PRICE, product.getSellPrice());
                values.put(ProductDatabaseModel.PARENT_CATEGORY_ID, product
                        .getParentCategory().getId());
                values.put(ProductDatabaseModel.CATEGORY_ID, product.getCategory().getId());
                values.put(ProductDatabaseModel.COMPOSITION_STATUS, product.getcStats());
                values.put(ProductDatabaseModel.SELL_ABLE, product.getSellAble());
                values.put(ProductDatabaseModel.UOM_ID, product.getUom().getId());
                values.put(ProductDatabaseModel.CODE, product.getCode());
                values.put(ProductDatabaseModel.FG, product.getFg());
                values.put(ProductDatabaseModel.PRODUCT_VALUE, product.getProductValue());
                values.put(ProductDatabaseModel.MIN_QUANTITY, product.getMinQuantity());
                values.put(ProductDatabaseModel.MAX_QUANTITY, product.getMaxQuantity());
                values.put(ProductDatabaseModel.IMAGE, product.getImage());
                values.put(ProductDatabaseModel.DESCRIPTION, product.getDescription());
                values.put(ProductDatabaseModel.REWARD, product.getReward());

                values.put(ProductDatabaseModel.STATUS_FLAG , 1);

                context.getContentResolver().update(dbUriProduct, values, ProductDatabaseModel.ID + " = ?", new String[] { product.getId() });
            }
        }
    }

    public List<Product> getMenu() {
        String query = ProductDatabaseModel.STATUS_FLAG + " = " + SignageVariables.ACTIVE;

        Cursor cursor = context.getContentResolver().query(dbUriProduct, null,
                query, null, ProductDatabaseModel.NAME);

        List<Product> products = new ArrayList<Product>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Product product = new Product();

                product.setId(cursor.getString(cursor
                        .getColumnIndex(ProductDatabaseModel.ID)));
                product.setName(cursor.getString(cursor
                        .getColumnIndex(ProductDatabaseModel.NAME)));
                product.setSellPrice(cursor.getLong(cursor
                        .getColumnIndex(ProductDatabaseModel.PRICE)));
                product.setReward(cursor.getDouble(cursor.getColumnIndex(ProductDatabaseModel.REWARD)));
//                product.setImage(ImageUtil.getImage(context, product.getId()));

                products.add(product);
            }
        }

        cursor.close();

        return products;
    }

    public List<Product> getMenuByCategory(String categoryId) {
        String query = ProductDatabaseModel.CATEGORY_ID + " = ? AND "
                + ProductDatabaseModel.STATUS_FLAG + " = " + SignageVariables.ACTIVE;


        Cursor cursor = context.getContentResolver().query(dbUriProduct, null,
                query, new String[] { categoryId },
                ProductDatabaseModel.NAME);

        List<Product> products = new ArrayList<Product>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Product product = new Product();

                product.setId(cursor.getString(cursor
                        .getColumnIndex(ProductDatabaseModel.ID)));
                product.setName(cursor.getString(cursor
                        .getColumnIndex(ProductDatabaseModel.NAME)));
                product.setSellPrice(cursor.getLong(cursor
                        .getColumnIndex(ProductDatabaseModel.PRICE)));
                product.setReward(cursor.getDouble(cursor.getColumnIndex(ProductDatabaseModel.REWARD)));
//                product.setImage(ImageUtil.getImage(context, product.getId()));

                products.add(product);
            }
        }

        cursor.close();

        return products;
    }

    public List<Product> getMenuByName(String name) {
        String query = ProductDatabaseModel.NAME + " like ? AND "
                + ProductDatabaseModel.STATUS_FLAG + " = " + SignageVariables.ACTIVE;

        String[] parameter = { "%" + name + "%" };

        Cursor cursor = context.getContentResolver().query(dbUriProduct, null,
                query, parameter, ProductDatabaseModel.NAME);

        List<Product> products = new ArrayList<Product>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Product product = new Product();

                product.setId(cursor.getString(cursor
                        .getColumnIndex(ProductDatabaseModel.ID)));
                product.setName(cursor.getString(cursor
                        .getColumnIndex(ProductDatabaseModel.NAME)));
                product.setSellPrice(cursor.getLong(cursor
                        .getColumnIndex(ProductDatabaseModel.PRICE)));
                product.setReward(cursor.getDouble(cursor.getColumnIndex(ProductDatabaseModel.REWARD)));
//                product.setImage(ImageUtil.getImage(context, product.getId()));

                products.add(product);
            }
        }

        cursor.close();

        return products;
    }

    public List<Product> getMenuByParentCategory(String categoryId) {
        String query = ProductDatabaseModel.PARENT_CATEGORY_ID + " = ? AND "
                + ProductDatabaseModel.STATUS_FLAG + " = " + SignageVariables.ACTIVE;

        String[] parameter = { categoryId };

        Cursor cursor = context.getContentResolver().query(dbUriProduct, null,
                query, parameter, ProductDatabaseModel.NAME);

        List<Product> products = new ArrayList<Product>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Product product = new Product();

                product.setId(cursor.getString(cursor
                        .getColumnIndex(ProductDatabaseModel.ID)));
                product.setName(cursor.getString(cursor
                        .getColumnIndex(ProductDatabaseModel.NAME)));
                product.setSellPrice(cursor.getLong(cursor
                        .getColumnIndex(ProductDatabaseModel.PRICE)));
                product.setReward(cursor.getDouble(cursor.getColumnIndex(ProductDatabaseModel.REWARD)));
//                product.setImage(ImageUtil.getImage(context, product.getId()));

                products.add(product);
            }
        }

        cursor.close();

        return products;
    }

    public List<String> getAllProductId() {
        String query = ProductDatabaseModel.STATUS_FLAG + " = " + SignageVariables.ACTIVE;

        Cursor cursor = context.getContentResolver().query(dbUriProduct, null, query, null,
                null);

        List<String> products = new ArrayList<String>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                products.add(cursor.getString(cursor
                        .getColumnIndex(ProductDatabaseModel.ID)));
            }
        }

        cursor.close();

        return products;
    }

    public List<String> getProductByImage() {
        String query = ProductDatabaseModel.STATUS_FLAG + " = " + SignageVariables.ACTIVE + " AND " + ProductDatabaseModel.IMAGE + " = 1";

        Cursor cursor = context.getContentResolver().query(dbUriProduct, null, query, null,
                null);

        List<String> products = new ArrayList<String>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                products.add(cursor.getString(cursor
                        .getColumnIndex(ProductDatabaseModel.ID)));
            }
        }

        cursor.close();

        return products;
    }

    public void deleteProduct(List<String> ids) {
        for(String id : ids) {
            context.getContentResolver().delete(dbUriProduct, ProductDatabaseModel.ID + " = ? ", new String[] { id });
        }
    }

    public void voidProduct(String id) {
        ContentValues values = new ContentValues();
        values.put(ProductDatabaseModel.STATUS_FLAG, 0);

        context.getContentResolver().update(dbUriProduct, values, ProductDatabaseModel.ID + " = ? ", new String[] { id });
    }

    public void voidProduct(List<String> ids) {
        for(String id : ids) {
            ContentValues values = new ContentValues();
            values.put(ProductDatabaseModel.STATUS_FLAG, 0);

            context.getContentResolver().update(dbUriProduct, values, ProductDatabaseModel.ID + " = ? ", new String[] { id });
        }
    }

    public void voidAllProducts() {
        ContentValues values = new ContentValues();
        values.put(ProductStoreDatabaseModel.STATUS_FLAG, 0);

        context.getContentResolver().update(dbUriProduct, values, null, null);
    }

}