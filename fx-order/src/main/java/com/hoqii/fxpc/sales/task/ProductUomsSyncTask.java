package com.hoqii.fxpc.sales.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.content.database.adapter.ProductUomDatabaseAdapter;
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
public class ProductUomsSyncTask extends AsyncTask<String, Void, JSONObject> {
    private Context context;
    private TaskService service;
    private SharedPreferences preferences;

    private int phase = 1;
    private String parentId = "";

    private List<String> idProductUoms = new ArrayList<String>();
    private ProductUomDatabaseAdapter productUomDatabaseAdapter;

    public ProductUomsSyncTask(Context context, TaskService service) {
        this.context = context;
        this.service = service;

        preferences = context.getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        productUomDatabaseAdapter = new ProductUomDatabaseAdapter(context);
        idProductUoms = productUomDatabaseAdapter.getAllId();
    }

    @Override
    protected void onCancelled() {
        service.onCancel(SignageVariables.PRODUCT_UOM_GET_TASK, context.getString(R.string.cancel));
    }

    @Override
    protected void onPreExecute() {
        service.onExecute(SignageVariables.PRODUCT_UOM_GET_TASK);
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        Log.d(getClass().getSimpleName(), "?access_token= " + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
        return ConnectionUtil.get(preferences.getString("server_url", "") + "/api/product/uoms?access_token=" + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        try {
            if (result != null) {
                JSONArray jsonArray = result.getJSONArray("content");
                List<ProductUom> productUoms = new ArrayList<ProductUom>();

                for (int a = 0; a < jsonArray.length(); a++) {
                    JSONObject json = jsonArray.getJSONObject(a);

                    Log.d(getClass().getSimpleName(), "JSON UOM: " + json.toString());

                    ProductUom productUom = new ProductUom();

                    productUom.setId(json.getString("id"));
                    productUom.setName(json.getString("name"));
                    productUom.setDescription(json.getString("description"));

                    productUoms.add(productUom);

                    if(idProductUoms.contains(productUom.getId())) {
                        idProductUoms.remove(productUom.getId());
                    }
                }

                productUomDatabaseAdapter.saveProductUom(productUoms);
                productUomDatabaseAdapter.delete(idProductUoms);

                service.onSuccess(SignageVariables.PRODUCT_UOM_GET_TASK, true);
            } else {
                service.onError(SignageVariables.PRODUCT_UOM_GET_TASK, context.getString(R.string.error));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            service.onError(SignageVariables.PRODUCT_UOM_GET_TASK, context.getString(R.string.error));
        }
    }
}
