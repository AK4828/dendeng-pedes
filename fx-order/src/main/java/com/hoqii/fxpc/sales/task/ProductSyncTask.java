package com.hoqii.fxpc.sales.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.content.database.adapter.ProductDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.Category;
import com.hoqii.fxpc.sales.entity.Product;
import com.hoqii.fxpc.sales.entity.ProductUom;
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
public class ProductSyncTask extends AsyncTask<String, Void, JSONObject> {
    private Context context;
    private TaskService service;

    private String parentCategory = null;
    private String category = null;

    private ProductDatabaseAdapter productAdapter;
    private List<String> idProducts = new ArrayList<String>();
    private SharedPreferences preferences;

    public ProductSyncTask(Context context, TaskService service) {
        this.context = context;
        this.service = service;

        preferences = context.getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        productAdapter = new ProductDatabaseAdapter(context);
        idProducts = productAdapter.getAllProductId();
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
        Log.d(getClass().getSimpleName(), "?access_token= " + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
        return ConnectionUtil.get(preferences.getString("server_url", "") + "/api/products?access_token="
                + AuthenticationUtils.getCurrentAuthentication().getAccessToken()+ "&max=" + params[0]);
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        try {
            if (result != null) {
                List<Product> products = new ArrayList<Product>();

                JSONArray jsonArray = result.getJSONArray("content");
                for (int a = 0; a < jsonArray.length(); a++) {
                    JSONObject json = jsonArray.getJSONObject(a);

                    Category parentCategory = new Category();
                    if (!json.isNull("parentCategory")) {
                        parentCategory.setId(json.getJSONObject("parentCategory").getString("id"));
                    }

                    Category category = new Category();
                    if (!json.isNull("category")) {
                        category.setId(json.getJSONObject("category").getString("id"));
                    }

                    ProductUom uom = new ProductUom();
                    if (!json.isNull("uom")) {
                        uom.setId(json.getJSONObject("uom").getString("id"));
                    }

                    Product product = new Product();
                    product.setId(json.getString("id"));
                    product.setName(json.getString("name"));
                    if (!json.isNull("sellPrice")) {
                        product.setSellPrice(json.getLong("sellPrice"));
                    } else {
                        product.setSellPrice(0);
                    }
                    product.setParentCategory(parentCategory);
                    product.setCategory(category);
                    product.setUom(uom);
                    product.setCode(json.getString("code"));
                    product.setDescription(json.getString("description"));
                    product.setReward(new Double(json.getString("reward")));

                    products.add(product);

                    product.setImage(1);

                    if(idProducts.contains(product.getId())) {
                        idProducts.remove(product.getId());
                    }
                }
                productAdapter.saveProducts(products);
                productAdapter.voidProduct(idProducts);

                service.onSuccess(SignageVariables.PRODUCT_GET_TASK, true);
            } else {
                service.onError(SignageVariables.PRODUCT_GET_TASK, context.getString(R.string.error));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            service.onError(SignageVariables.PRODUCT_GET_TASK, context.getString(R.string.error));
        }
    }
}