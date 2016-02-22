package org.meruvian.midas.core.content.database.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import org.meruvian.midas.core.content.MidasContentProvider;

/**
 * Created by ludviantoovandi on 25/02/15.
 */
public abstract class DatabaseAdapter {
    private Context context;

    public DatabaseAdapter(Context context) {
        this.context = context;
    }

    private Uri databaseUri(String name) {
        return Uri.parse(MidasContentProvider.CONTENT_PATH + name);
    }

//    public String save(ContentValues contentValues) {
//        if (contentValues.get)
//        context.getContentResolver().insert()
//    }
}
