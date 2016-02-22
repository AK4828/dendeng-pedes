package com.hoqii.fxpc.sales.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.entity.Category;
import com.hoqii.fxpc.sales.entity.Product;

import org.json.JSONException;
import org.json.JSONObject;
import org.meruvian.midas.core.service.TaskService;
import org.meruvian.midas.core.util.ConnectionUtil;

/**
 * Created by ludviantoovandi on 28/01/15.
 */
public class ProductDetailTask extends AsyncTask<String, Void, JSONObject> {
    private Context context;
    private TaskService service;
    private SharedPreferences preferences;

    private String parentCategory = null;
    private String category = null;

    public ProductDetailTask(Context context, TaskService service) {
        this.context = context;
        this.service = service;
        preferences = context.getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
    }

    @Override
    protected void onCancelled() {
        service.onCancel(SignageVariables.PRODUCT_GET_TASK, context.getString(R.string.cancel));
    }

    @Override
    protected void onPreExecute() {
        service.onExecute(SignageVariables.PRODUCT_GET_TASK);
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        return ConnectionUtil.get(preferences.getString("server_url", "") + "/api/product/id/" + params[0]);
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        try {
            if (result != null) {
                Product product = new Product();
                product.setId(result.getString("id"));
                product.setSellPrice(result.getLong("sellPrice"));
                product.setName(result.getString("name"));

                if (!result.isNull("desc")) {
                    product.setDescription(result.getString("desc"));
                }

                if (!result.isNull("parentCategory")) {
                    JSONObject pcJson = result.getJSONObject("parentCategory");
                    Category parentCategory = new Category();
                    parentCategory.setName(pcJson.getString("name"));
                    parentCategory.setId(pcJson.getString("id"));

                    product.setParentCategory(parentCategory);
                }

                if (!result.isNull("category")) {
                    JSONObject cJson = result.getJSONObject("category");
                    Category category = new Category();
                    category.setId(cJson.getString("id"));
                    category.setName(cJson.getString("name"));

                    product.setCategory(category);
                }

                service.onSuccess(SignageVariables.PRODUCT_DETAIL_TASK, product);
            } else {
                service.onError(SignageVariables.PRODUCT_GET_TASK, context.getString(R.string.error));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            service.onError(SignageVariables.PRODUCT_GET_TASK, context.getString(R.string.error));
        }
    }
}