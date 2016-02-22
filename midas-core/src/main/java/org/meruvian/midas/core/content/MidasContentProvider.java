package org.meruvian.midas.core.content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import org.meruvian.midas.core.content.database.MidasDatabase;

/**
 * Created by meruvian on 29/01/15.
 */
public abstract class MidasContentProvider extends ContentProvider {
    private MidasDatabase dbHelper;

//    public static final String TABLES[] = {
//        MidasDatabase.CATEGORY_TABLE,
//        MidasDatabase.PRODUCT_TABLE
//    };

    public abstract String[] tables();

    public abstract String databaseName();

    public abstract int version();

    public static final String AUTHORITY = MidasContentProvider.class.getName().toLowerCase();
    public static final String CONTENT_PATH = "content://" + AUTHORITY + "/";
    private UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH) {
        {
            int i = 0;
            for (String table : tables()) {
                addURI(AUTHORITY, table, i);
                i++;
            }
        }
    };


    @Override
    public boolean onCreate() {
        dbHelper = new MidasDatabase(getContext(), databaseName(), version());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        builder.setTables(tables()[matcher.match(uri)]);
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
        long id = database.insert(tables()[matcher.match(uri)], null, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(tables()[matcher.match(uri)] + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int deleted = database.delete(tables()[matcher.match(uri)], selection,
                selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return deleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int updated = database.update(tables()[matcher.match(uri)], values,
                selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return updated;
    }
}
