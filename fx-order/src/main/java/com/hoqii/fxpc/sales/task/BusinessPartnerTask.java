package com.hoqii.fxpc.sales.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.content.database.adapter.BusinessPartnerDatabaseAdapter;
import com.hoqii.fxpc.sales.entity.BusinessPartner;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.meruvian.midas.core.service.TaskService;
import org.meruvian.midas.core.util.ConnectionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by miftakhul on 12/6/15.
 */
public class BusinessPartnerTask extends AsyncTask<String, Void, JSONObject>{

    private Context context;
    private TaskService taskService;
    private SharedPreferences preferences;

    private BusinessPartnerDatabaseAdapter businessPartnerDatabaseAdapter;

    public BusinessPartnerTask(Context context, TaskService taskService){
        this.context = context;
        this.taskService = taskService;
        preferences = context.getSharedPreferences(SignageVariables.PREFS_SERVER, 0);

        businessPartnerDatabaseAdapter = new BusinessPartnerDatabaseAdapter(context);
    }

    @Override
    protected JSONObject doInBackground(String... params) {
        Log.d(getClass().getSimpleName(), "?acces_token= " + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
        return ConnectionUtil.get(preferences.getString("server_url", "") + "/api/business?access_token="
                + AuthenticationUtils.getCurrentAuthentication().getAccessToken());

    }

    @Override
    protected void onCancelled() {
        taskService.onCancel(SignageVariables.BUSINESS_PARTNER_GET_TASK, "Batal");
    }

    @Override
    protected void onPreExecute() {
        taskService.onExecute(SignageVariables.BUSINESS_PARTNER_GET_TASK);
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        try {
            if (result != null){
                List<BusinessPartner> businessPartners = new ArrayList<BusinessPartner>();

                Log.d("result ===========", result.toString());
                JSONArray jsonArray = result.getJSONArray("content");
                for (int a = 0; a < jsonArray.length(); a++){
                    JSONObject object = jsonArray.getJSONObject(a);

                    BusinessPartner businessPartner = new BusinessPartner();
                    businessPartner.setId(object.getString("id"));
                    businessPartner.setName(object.getString("name"));
                    businessPartner.setOfficePhone(object.getString("officePhone"));
                    businessPartner.setFax(object.getString("fax"));
                    businessPartner.setEmail(object.getString("email"));
                    businessPartner.setOtherEmail(object.getString("otherEmail"));
                    businessPartner.setAddress(object.getString("address"));
                    businessPartner.setCity(object.getString("city"));
                    businessPartner.setZipCode(object.getString("zipCode"));
                    businessPartner.setCountry(object.getString("country"));
                    businessPartner.setDescription(object.getString("description"));

                    businessPartners.add(businessPartner);

                }
                Log.d("businesspartner object", businessPartners.toString());

                businessPartnerDatabaseAdapter.saveBusinessPartner(businessPartners);
                taskService.onSuccess(SignageVariables.BUSINESS_PARTNER_GET_TASK, true);
            }else {
                taskService.onError(SignageVariables.BUSINESS_PARTNER_GET_TASK, "Error");
            }
        }catch (JSONException e){
            e.printStackTrace();
            taskService.onError(SignageVariables.BUSINESS_PARTNER_GET_TASK, "Error");
        }


    }
}
