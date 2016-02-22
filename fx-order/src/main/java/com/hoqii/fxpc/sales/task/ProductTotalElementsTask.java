package com.hoqii.fxpc.sales.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.meruvian.midas.core.service.TaskService;
import org.meruvian.midas.core.util.ConnectionUtil;

/**
 * Created by ludviantoovandi on 28/01/15.
 */
public class ProductTotalElementsTask extends AsyncTask<Void, Void, JSONObject> {
    private Context context;
    private TaskService service;

    private SharedPreferences preferences;

    public ProductTotalElementsTask(Context context, TaskService service) {
        this.context = context;
        this.service = service;

        preferences = context.getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
    }

    @Override
    protected void onCancelled() {
        service.onCancel(SignageVariables.PRODUCT_ELEMENTS_TASK, context.getString(R.string.cancel));
    }

    @Override
    protected void onPreExecute() {
        service.onExecute(SignageVariables.PRODUCT_ELEMENTS_TASK);
    }

    @Override
    protected JSONObject doInBackground(Void... arg0) {
        Log.d(getClass().getSimpleName(), "?access_token= " + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
        return ConnectionUtil.get(preferences.getString("server_url", "") + "/api/products?access_token="
                + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        try {
            if (result != null) {
                int totalElements = result.getInt("totalElements");

                service.onSuccess(SignageVariables.PRODUCT_ELEMENTS_TASK, totalElements);
            } else {
                service.onError(SignageVariables.PRODUCT_ELEMENTS_TASK, context.getString(R.string.error));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            service.onError(SignageVariables.PRODUCT_ELEMENTS_TASK, context.getString(R.string.error));
        }
    }

}