package com.hoqii.fxpc.sales.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.content.database.adapter.CategoryDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.Category;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.meruvian.midas.core.service.TaskService;
import org.meruvian.midas.core.util.ConnectionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ludviantoovandi on 28/01/15.
 */
public class CategorySyncTask extends AsyncTask<String, Void, JSONObject> {
    private Context context;
    private TaskService service;
    private SharedPreferences preferences;

    private int phase = 1;
    private String parentId = "";

    private List<String> idCategories = new ArrayList<String>();
    private List<String> refId = new ArrayList<String>();

    private CategoryDatabaseAdapter categoryAdapter;

    public CategorySyncTask(Context context, TaskService service) {
        this.context = context;
        this.service = service;

        categoryAdapter = new CategoryDatabaseAdapter(context);
        idCategories = categoryAdapter.getAllId();
        preferences = context.getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
    }

    @Override
    protected void onCancelled() {
        service.onCancel(SignageVariables.CATEGORY_GET_TASK, context.getString(R.string.cancel));
    }

    @Override
    protected void onPreExecute() {
        service.onExecute(SignageVariables.CATEGORY_GET_TASK);
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        Log.d(getClass().getSimpleName(), "?access_token= " + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
        return ConnectionUtil.get(preferences.getString("server_url", "") + "/api/product/categories?access_token="
                + AuthenticationUtils.getCurrentAuthentication().getAccessToken() + "&max=" + params[0]);
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        try {
            if (result != null) {
                int totalPages = result.getInt("totalElements");
                JSONArray jsonArray = result.getJSONArray("content");
                List<Category> categories = new ArrayList<Category>();

                for (int a = 0; a < jsonArray.length(); a++) {
                    JSONObject json = jsonArray.getJSONObject(a);

                    Log.d(getClass().getSimpleName(), "JSON CATEGORY: " + json.toString());

                    Category parentCategory = new Category();
                    Category category = new Category();

                    JSONObject jsonParent = new JSONObject();
                    if (!json.isNull("parent")) {
                        jsonParent = json.getJSONObject("parent");
                        parentCategory.setId(jsonParent.getString("id"));
                    }

                    category.setId(json.getString("id"));
                    category.setParentCategory(parentCategory);
                    category.setName(json.getString("name"));

                    categories.add(category);

                    if(idCategories.contains(category.getId())) {
                        idCategories.remove(category.getId());
                    }
                }
                categoryAdapter.saveCategory(categories);
                categoryAdapter.delete(idCategories);

                service.onSuccess(SignageVariables.CATEGORY_GET_TASK, totalPages);
            } else {
                service.onError(SignageVariables.CATEGORY_GET_TASK, context.getString(R.string.error));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            service.onError(SignageVariables.CATEGORY_GET_TASK, context.getString(R.string.error));
        }
    }

    public int getPhase() {
        return phase;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
