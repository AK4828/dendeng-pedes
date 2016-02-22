package com.hoqii.fxpc.sales.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.content.database.adapter.ProductDatabaseAdapter;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.hoqii.fxpc.sales.util.ImageUtil;

import org.meruvian.midas.core.service.TaskService;

import java.io.IOException;

/**
 * Created by ludviantoovandi on 04/02/15.
 */
public class ImageProductTask extends AsyncTask<Void, Void, Void> {
    private Context context;
    private TaskService service;
    private SharedPreferences preferences;

    private ProductDatabaseAdapter productDbAdapter;

    public ImageProductTask(Context context, TaskService service) {
        this.context = context;
        this.service = service;

        preferences = context.getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        productDbAdapter = new ProductDatabaseAdapter(context);
    }

    @Override
    protected void onPreExecute() {
        service.onExecute(SignageVariables.IMAGE_PRODUCT_TASK);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        for (String id : productDbAdapter.getProductByImage()) {
            try {
                Log.d(getClass().getSimpleName(), "?access_token= " + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
                ImageUtil.save(context, "/api/products", id, "/image?access_token=" + AuthenticationUtils.getCurrentAuthentication().getAccessToken(), preferences.getString("server_url", ""));
            } catch (IOException e) {
                Log.e(getClass().getName(), e.getMessage(), e);
            }
        }

        ImageUtil.clearImageCache();

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        service.onSuccess(SignageVariables.IMAGE_PRODUCT_TASK, true);
    }
}
