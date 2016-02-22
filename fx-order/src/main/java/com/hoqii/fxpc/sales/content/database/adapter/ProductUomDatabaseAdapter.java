package com.hoqii.fxpc.sales.content.database.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

import com.hoqii.fxpc.sales.content.MidasContentProvider;
import com.hoqii.fxpc.sales.content.database.model.ProductUomDatabaseModel;
import com.hoqii.fxpc.sales.entity.ProductUom;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by meruvian on 30/07/15.
 */
public class ProductUomDatabaseAdapter  {
    private Uri dbUriProductUom = Uri.parse(MidasContentProvider.CONTENT_PATH
            + MidasContentProvider.TABLES[6]);

    private Context context;

    public ProductUomDatabaseAdapter(Context context) {
        this.context = context;
    }

    public void saveProductUom(List<ProductUom> productUoms) {
        for (ProductUom pu : productUoms) {
            ContentValues values = new ContentValues();

            if (findProductUomById(pu.getId()) == null) {
                values.put(ProductUomDatabaseModel.ID, pu.getId());
                values.put(ProductUomDatabaseModel.NAME, pu.getName());
                values.put(ProductUomDatabaseModel.DESCRIPTION, pu.getDescription());
                values.put(ProductUomDatabaseModel.STATUS_FLAG, 1);

                context.getContentResolver().insert(dbUriProductUom, values);
            } else {
                values.put(ProductUomDatabaseModel.NAME, pu.getName());
                values.put(ProductUomDatabaseModel.DESCRIPTION, pu.getDescription());
                values.put(ProductUomDatabaseModel.STATUS_FLAG, 1);

                context.getContentResolver().update(dbUriProductUom, values, ProductUomDatabaseModel.ID + " = ?", new String[] { pu.getId() });
            }
        }
    }

    public ProductUom findProductUomById(String id) {
        String query = ProductUomDatabaseModel.ID + " = ?";
        String[] parameter = { id };

        Cursor cursor = context.getContentResolver().query(dbUriProductUom, null, query, parameter, null);

        ProductUom productUom = null;

        if(cursor != null) {
            if(cursor.getCount() > 0) {
                try {
                    cursor.moveToFirst();

                    productUom = new ProductUom();
                    productUom.setId(cursor.getString(cursor.getColumnIndex(ProductUomDatabaseModel.ID)));
                    productUom.setName(cursor.getString(cursor.getColumnIndex(ProductUomDatabaseModel.NAME)));


                } catch (SQLException e) {
                    e.printStackTrace();

                    productUom = null;
                }
            }
        }

        cursor.close();

        return productUom;
    }

    public List<String> getAllId() {
        Cursor cursor = context.getContentResolver().query(dbUriProductUom, null, null, null, null);

        List<String> productUoms = new ArrayList<String>();

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    productUoms.add(cursor.getString(cursor.getColumnIndex(ProductUomDatabaseModel.ID)));
                }
            }
        }

        cursor.close();

        return productUoms;
    }

    public void delete(List<String> ids) {
        for(String id : ids) {
            ContentValues values = new ContentValues();
            values.put(ProductUomDatabaseModel.STATUS_FLAG, 0);

            context.getContentResolver().update(dbUriProductUom, values, ProductUomDatabaseModel.ID + " = ? ", new String[] { id });
        }
    }

}
