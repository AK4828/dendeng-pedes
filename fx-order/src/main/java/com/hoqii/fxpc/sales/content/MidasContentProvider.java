package com.hoqii.fxpc.sales.content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.hoqii.fxpc.sales.content.database.MidasDatabase;

/**
 * Created by meruvian on 29/01/15.
 */
public class MidasContentProvider extends ContentProvider {
    private MidasDatabase dbHelper;

    public static final String TABLES[] = {
            MidasDatabase.CATEGORY_TABLE,//0
            MidasDatabase.PRODUCT_TABLE,//1
            MidasDatabase.CAMPAIGN_TABLE,//2
            MidasDatabase.CAMPAIGN_DETAIL_TABLE,//3
            MidasDatabase.ORDER_TABLE,//4
            MidasDatabase.ORDER_MENU_TABLE,//5
            MidasDatabase.PRODUCT_UOM,//6
            MidasDatabase.CONTACT,//7
            MidasDatabase.CART_TABLE,//8
            MidasDatabase.CART_MENU_TABLE,//9
            MidasDatabase.PRODUCT_STORE_TABLE,//10
            MidasDatabase.USER_TABLE,//11
            MidasDatabase.ORDER_MENU_IMEI_TABLE,//12
            MidasDatabase.ASSIGMENT_TABLE,//13
            MidasDatabase.ASSIGMENT_DETAIL_TABLE,//14
            MidasDatabase.ASSIGMENT_DETAIL_ITEM_TABLE,//15
            MidasDatabase.SETTLE_TABLE,//16
            MidasDatabase.SUPPLIER_TABLE,//17
            MidasDatabase.BUSINESS_PARTNER, //18
            MidasDatabase.SERIAL_NUMBER,//19
            MidasDatabase.SITE,//20
            MidasDatabase.SO_TABLE,//21
            MidasDatabase.SO_MENU_TABLE,//22
            MidasDatabase.SO_MENU_SERIAL_TABLE//23

    };

    public static final String AUTHORITY = MidasContentProvider.class.getName().toLowerCase();
    public static final String CONTENT_PATH = "content://" + AUTHORITY + "/";
    private UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH) {
        {
            int i = 0;
            for (String table : TABLES) {
                addURI(AUTHORITY, table, i);
                i++;
            }
        }
    };


    @Override
    public boolean onCreate() {
        dbHelper = new MidasDatabase(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        builder.setTables(TABLES[matcher.match(uri)]);
        Cursor cursor = builder.query(database, projection, selection,
                selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        long id = database.insert(TABLES[matcher.match(uri)], null, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(TABLES[matcher.match(uri)] + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int deleted = database.delete(TABLES[matcher.match(uri)], selection,
                selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return deleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int updated = database.update(TABLES[matcher.match(uri)], values,
                selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return updated;
    }
}
