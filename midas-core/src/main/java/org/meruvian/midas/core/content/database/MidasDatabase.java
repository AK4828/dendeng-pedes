package org.meruvian.midas.core.content.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.meruvian.midas.core.content.database.model.DefaultPersistenceModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by meruvian on 29/01/15.
 */

public class MidasDatabase extends SQLiteOpenHelper {
    private List<String> tables = new ArrayList<String>();

    public MidasDatabase(Context context, String databaseName, int version) {
        super(context, databaseName, null, version);
    }

    public void addTable(String name, String... columns) {
        String query = "CREATE TABLE " + name + "("
                + DefaultPersistenceModel.ID + " TEXT PRIMARY KEY, "
                + DefaultPersistenceModel.CREATE_BY + " TEXT, "
                + DefaultPersistenceModel.CREATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.UPDATE_BY + " TEXT, "
                + DefaultPersistenceModel.UPDATE_DATE + " INTEGER, "
                + DefaultPersistenceModel.STATUS_FLAG + " INTEGER, ";
        for (int index = 0; index < columns.length; index++) {
            if (index == (columns.length - 1)) {
                query += columns[index] + ")";
            } else {
                query += columns[index] + ",";
            }
        }

        tables.add(query);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        for (String table : tables) {
            db.execSQL(table);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {

        }
    }
}
