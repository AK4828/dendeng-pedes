package com.hoqii.fxpc.sales.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.entity.Category;
import com.hoqii.fxpc.sales.entity.Product;
import com.hoqii.fxpc.sales.entity.ProductUom;
import com.hoqii.fxpc.sales.entity.Stock;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.meruvian.midas.core.service.TaskService;
import org.meruvian.midas.core.util.ConnectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by miftakhul on 3/5/16.
 */
public class StockSync extends AsyncTask<String, Void, JSONObject>{

    private Context context;
    private TaskService taskService;
    private SharedPreferences preferences;
    private StockUri currentUri = StockUri.defaultUri;

    public enum StockUri{
        defaultUri, bySerialUri, byProductIdUri, bySiteUri
    }

    public StockSync(Context context, TaskService taskService, String enumUri) {
        this.context = context;
        this.taskService = taskService;
        currentUri = StockUri.valueOf(enumUri);

        preferences = context.getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        Log.d(getClass().getSimpleName(), "?acces_token= " + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
        switch (currentUri){
            case defaultUri:
                return ConnectionUtil.get(preferences.getString("server_url", "") + "/api/stocks?access_token="
                        + AuthenticationUtils.getCurrentAuthentication().getAccessToken()+"&max="+Integer.MAX_VALUE);
            case bySerialUri:
                return ConnectionUtil.get(preferences.getString("server_url", "") + "/api/stocks/product/"+params[0]+"/serial/"+params[1]+"?access_token="
                        + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
            case byProductIdUri:
                return ConnectionUtil.get(preferences.getString("server_url", "") + "/api/stocks/product/"+params[0]+"/"+params[1]+"?access_token="
                        + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
            case bySiteUri:
                return ConnectionUtil.get(preferences.getString("server_url", "") + "/api/stocks/site/"+params[0]+"?access_token="
                        + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
            default:
                return ConnectionUtil.get(preferences.getString("server_url", "") + "/api/stocks?access_token="
                        + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
        }
    }

    @Override
    protected void onCancelled() {
        taskService.onCancel(SignageVariables.STOCK_GET_TASK, "Batal");
    }

    @Override
    protected void onPreExecute() {
        taskService.onExecute(SignageVariables.STOCK_GET_TASK);
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        switch (currentUri){
            case defaultUri:
                try {
                    if (result !=null){
                        List<Stock> stocks = new ArrayList<Stock>();
                        JSONArray jsonArray = result.getJSONArray("content");
                        for (int a = 0; a < jsonArray.length(); a++){
                            JSONObject object = jsonArray.getJSONObject(a);
                            Stock s = new Stock();
                            s.setId(object.getString("id"));
                            if (!object.isNull("product")){
                                JSONObject productObject = new JSONObject();
                                productObject = object.getJSONObject("product");

                                Category parentCategory = new Category();
                                if (!productObject.isNull("parentCategory")) {
                                    parentCategory.setId(productObject.getJSONObject("parentCategory").getString("id"));
                                    parentCategory.setName(productObject.getJSONObject("parentCategory").getString("name"));
                                }

                                Category category = new Category();
                                if (!productObject.isNull("category")) {
                                    category.setId(productObject.getJSONObject("category").getString("id"));
                                }

                                ProductUom uom = new ProductUom();
                                if (!productObject.isNull("uom")) {
                                    uom.setId(productObject.getJSONObject("uom").getString("id"));
                                }

                                Product product = new Product();
                                product.setId(productObject.getString("id"));
                                product.setName(productObject.getString("name"));
                                if (!productObject.isNull("sellPrice")) {
                                    product.setSellPrice(productObject.getLong("sellPrice"));
                                } else {
                                    product.setSellPrice(0);
                                }
                                product.setParentCategory(parentCategory);
                                product.setCategory(category);
                                product.setUom(uom);
                                product.setCode(productObject.getString("code"));
                                product.setDescription(productObject.getString("description"));
                                product.setReward(new Double(productObject.getString("reward")));

                                s.setProduct(product);
                            }
                            s.setQty(object.getInt("qty"));
                            stocks.add(s);
                        }
                        taskService.onSuccess(SignageVariables.STOCK_GET_TASK, stocks);
                    }else {
                        taskService.onError(SignageVariables.STOCK_GET_TASK, "Batal");
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    taskService.onError(SignageVariables.STOCK_GET_TASK, "Batal");
                }
                break;

            case bySerialUri:
                try {
                    if (result != null) {
                        JSONObject object = result;
                        boolean status = object.getBoolean("status");
                        Log.d(getClass().getSimpleName(), "status serial : " + String.valueOf(status));

                        taskService.onSuccess(SignageVariables.STOCK_GET_TASK, status);
                    }else {
                        taskService.onError(SignageVariables.STOCK_GET_TASK, "Batal");
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    taskService.onError(SignageVariables.STOCK_GET_TASK, "Batal");
                }
                break;

            case byProductIdUri:
                try {
                    if (result !=null){
                        JSONObject object = result;
                        Stock s = new Stock();
                        s.setId(object.getString("id"));
                        if (!object.isNull("product")){
                            JSONObject productObject = new JSONObject();
                            productObject = object.getJSONObject("product");

                            Category parentCategory = new Category();
                            if (!productObject.isNull("parentCategory")) {
                                parentCategory.setId(productObject.getJSONObject("parentCategory").getString("id"));
                            }

                            Category category = new Category();
                            if (!productObject.isNull("category")) {
                                category.setId(productObject.getJSONObject("category").getString("id"));
                            }

                            ProductUom uom = new ProductUom();
                            if (!productObject.isNull("uom")) {
                                uom.setId(productObject.getJSONObject("uom").getString("id"));
                            }

                            Product product = new Product();
                            product.setId(productObject.getString("id"));
                            product.setName(productObject.getString("name"));
                            if (!productObject.isNull("sellPrice")) {
                                product.setSellPrice(productObject.getLong("sellPrice"));
                            } else {
                                product.setSellPrice(0);
                            }
                            product.setParentCategory(parentCategory);
                            product.setCategory(category);
                            product.setUom(uom);
                            product.setCode(productObject.getString("code"));
                            product.setDescription(productObject.getString("description"));
                            product.setReward(new Double(productObject.getString("reward")));

                            s.setProduct(product);
                        }
                        s.setQty(object.getInt("qty"));

                        taskService.onSuccess(SignageVariables.STOCK_GET_TASK, s);
                    }else {
                        taskService.onError(SignageVariables.STOCK_GET_TASK, "Batal");
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    taskService.onError(SignageVariables.STOCK_GET_TASK, "Batal");
                }
                break;

            case bySiteUri:
                try {
                    if (result !=null){
                        List<Stock> stocks = new ArrayList<Stock>();
                        JSONArray jsonArray = result.getJSONArray("content");
                        for (int a = 0; a < jsonArray.length(); a++){
                            JSONObject object = jsonArray.getJSONObject(a);
                            Stock s = new Stock();
                            s.setId(object.getString("id"));
                            if (!object.isNull("product")){
                                JSONObject productObject = new JSONObject();
                                productObject = object.getJSONObject("product");

                                Category parentCategory = new Category();
                                if (!productObject.isNull("parentCategory")) {
                                    parentCategory.setId(productObject.getJSONObject("parentCategory").getString("id"));
                                    parentCategory.setName(productObject.getJSONObject("parentCategory").getString("name"));
                                }

                                Category category = new Category();
                                if (!productObject.isNull("category")) {
                                    category.setId(productObject.getJSONObject("category").getString("id"));
                                }

                                ProductUom uom = new ProductUom();
                                if (!productObject.isNull("uom")) {
                                    uom.setId(productObject.getJSONObject("uom").getString("id"));
                                }

                                Product product = new Product();
                                product.setId(productObject.getString("id"));
                                product.setName(productObject.getString("name"));
                                if (!productObject.isNull("sellPrice")) {
                                    product.setSellPrice(productObject.getLong("sellPrice"));
                                } else {
                                    product.setSellPrice(0);
                                }
                                product.setParentCategory(parentCategory);
                                product.setCategory(category);
                                product.setUom(uom);
                                product.setCode(productObject.getString("code"));
                                product.setDescription(productObject.getString("description"));
                                product.setReward(new Double(productObject.getString("reward")));

                                s.setProduct(product);
                            }
                            s.setQty(object.getInt("qty"));
                            stocks.add(s);
                        }
                        taskService.onSuccess(SignageVariables.STOCK_GET_TASK, stocks);
                    }else {
                        taskService.onError(SignageVariables.STOCK_GET_TASK, "Batal");
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    taskService.onError(SignageVariables.STOCK_GET_TASK, "Batal");
                }
                break;

        }

    }
}
