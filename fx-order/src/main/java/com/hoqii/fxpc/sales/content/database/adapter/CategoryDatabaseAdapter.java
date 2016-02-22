package com.hoqii.fxpc.sales.content.database.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.content.MidasContentProvider;
import com.hoqii.fxpc.sales.content.database.model.CategoryDatabaseModel;
import com.hoqii.fxpc.sales.entity.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by meruvian on 29/01/15.
 */
public class CategoryDatabaseAdapter {
    private Uri dbUriCategory = Uri.parse(MidasContentProvider.CONTENT_PATH
            + MidasContentProvider.TABLES[0]);

    private Context context;

    public CategoryDatabaseAdapter(Context context) {
        this.context = context;
    }

    public void saveCategory(List<Category> categories) {
        for (Category ca : categories) {
            ContentValues values = new ContentValues();

            if (findCategoryById(ca.getId()) == null) {
                values.put(CategoryDatabaseModel.ID, ca.getId());
                values.put(CategoryDatabaseModel.PARENT_CATEGORY_ID, ca.getParentCategory().getId());
                values.put(CategoryDatabaseModel.CATEGORY_NAME, ca.getName());
                values.put(CategoryDatabaseModel.STATUS_FLAG, 1);

                context.getContentResolver().insert(dbUriCategory, values);
            } else {
                values.put(CategoryDatabaseModel.PARENT_CATEGORY_ID, ca.getParentCategory().getId());
                values.put(CategoryDatabaseModel.CATEGORY_NAME, ca.getName());
                values.put(CategoryDatabaseModel.STATUS_FLAG, 1);

                context.getContentResolver().update(dbUriCategory, values, CategoryDatabaseModel.ID + " = ?", new String[] { ca.getId() });
            }
        }
    }

    public List<Category> getParentCategoryMenu() {
        String query = "(" + CategoryDatabaseModel.PARENT_CATEGORY_ID + " is null OR " + CategoryDatabaseModel.PARENT_CATEGORY_ID + " = '') AND "
                + CategoryDatabaseModel.STATUS_FLAG + " = " + SignageVariables.ACTIVE;

        Cursor cursor = context.getContentResolver().query(dbUriCategory, null, query,
                null, CategoryDatabaseModel.CATEGORY_NAME);

        List<Category> categories = new ArrayList<Category>();

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    Category category = new Category();
                    category.setId(cursor.getString(cursor
                            .getColumnIndex(CategoryDatabaseModel.ID)));
                    category.setName(cursor.getString(cursor
                            .getColumnIndex(CategoryDatabaseModel.CATEGORY_NAME)));

                    categories.add(category);
                }
            }
        }

        cursor.close();

        return categories;
    }

    public List<Category> getCategoryMenuByIdParent(String parentId) {

        String query = CategoryDatabaseModel.PARENT_CATEGORY_ID + " = ? AND " + CategoryDatabaseModel.STATUS_FLAG + " = " + SignageVariables.ACTIVE;
        String[] parentCatId = { parentId };
        Cursor cursor = context.getContentResolver().query(dbUriCategory, null,
                query,
                parentCatId, CategoryDatabaseModel.CATEGORY_NAME);

        List<Category> categories = new ArrayList<Category>();

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    Category category = new Category();
                    category.setId(cursor.getString(cursor
                            .getColumnIndex(CategoryDatabaseModel.ID)));
                    category.setName(cursor.getString(cursor
                            .getColumnIndex(CategoryDatabaseModel.CATEGORY_NAME)));

                    categories.add(category);
                }
            }
        }

        cursor.close();

        return categories;
    }

    public Category findCategoryById(String id) {
        String query = CategoryDatabaseModel.ID + " = ?";
        String[] parameter = { id };

        Cursor cursor = context.getContentResolver().query(dbUriCategory, null, query, parameter, null);

        Category category = null;

        if(cursor != null) {
            if(cursor.getCount() > 0) {
                try {
                    cursor.moveToFirst();

                    category = new Category();
                    category.setId(cursor.getString(cursor.getColumnIndex(CategoryDatabaseModel.ID)));
                    category.setName(cursor.getString(cursor.getColumnIndex(CategoryDatabaseModel.CATEGORY_NAME)));

                    if(!cursor.isNull(cursor.getColumnIndex(CategoryDatabaseModel.PARENT_CATEGORY_ID))) {
                        Category parentCategory = null;
                        parentCategory = findCategoryById(cursor.getString(cursor.getColumnIndex(CategoryDatabaseModel.PARENT_CATEGORY_ID)));

                        category.setParentCategory(parentCategory);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();

                    category = null;
                }
            }
        }

        cursor.close();

        return category;
    }

    public List<String> getAllId() {
        Cursor cursor = context.getContentResolver().query(dbUriCategory, null, null, null, null);

        List<String> categories = new ArrayList<String>();

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    categories.add(cursor.getString(cursor
                            .getColumnIndex(CategoryDatabaseModel.ID)));
                }
            }
        }

        cursor.close();

        return categories;
    }

    public void delete(List<String> ids) {
        for(String id : ids) {
            ContentValues values = new ContentValues();
            values.put(CategoryDatabaseModel.STATUS_FLAG, 0);

            context.getContentResolver().update(dbUriCategory, values, CategoryDatabaseModel.ID + " = ? ", new String[] { id });
        }
    }


}
