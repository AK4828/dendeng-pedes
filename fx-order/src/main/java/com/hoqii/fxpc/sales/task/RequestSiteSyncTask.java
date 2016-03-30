package com.hoqii.fxpc.sales.task;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.content.database.adapter.SiteDatabaseAdapter;
import com.hoqii.fxpc.sales.core.LogInformation;
import com.hoqii.fxpc.sales.core.commons.Site;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.meruvian.midas.core.service.TaskService;
import org.meruvian.midas.core.util.ConnectionUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by miftakhul on 1/14/16.
 */
public class RequestSiteSyncTask extends AsyncTask<String, Void, JSONObject> {

    private Context context;
    private TaskService taskService;
    private SharedPreferences preferences;
    private SiteDatabaseAdapter siteDatabaseAdapter;

    public RequestSiteSyncTask(Context context, TaskService taskService) {
        this.context = context;
        this.taskService = taskService;

        preferences = context.getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        siteDatabaseAdapter = new SiteDatabaseAdapter(context);
    }


    @Override
    protected JSONObject doInBackground(String... JsonObject) {
        Log.d(getClass().getSimpleName(), "?acces_token= " + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
        return ConnectionUtil.get(preferences.getString("server_url", "") + "/api/sites?access_token="
                + AuthenticationUtils.getCurrentAuthentication().getAccessToken() + "&max=" + Integer.MAX_VALUE);
    }

    @Override
    protected void onCancelled() {
        taskService.onCancel(SignageVariables.SITE_GET_TASK, "Batal");
    }

    @Override
    protected void onPreExecute() {
        taskService.onExecute(SignageVariables.SITE_GET_TASK);
    }

    @Override
    protected void onPostExecute(JSONObject result) {
        try {
            if (result != null) {

                List<Site> sites = new ArrayList<Site>();

                Log.d("result site =====", result.toString());
                JSONArray jsonArray = result.getJSONArray("content");
                for (int a = 0; a < jsonArray.length(); a++) {
                    JSONObject object = jsonArray.getJSONObject(a);

                    Site site = new Site();
                    site.setId(object.getString("id"));

//                    JSONObject logInformationObject = new JSONObject();
//                    if (!object.isNull("logInformation")) {
//                        logInformationObject = object.getJSONObject("logInformation");
//
//                        LogInformation logInformation = new LogInformation();
//                        logInformation.setCreateDate(new Date(logInformationObject.getLong("createDate")));
//
//                        site.setLogInformation(logInformation);
//                    }

                    site.setName(object.getString("name"));
                    site.setDescription(object.getString("description"));
                    site.setTitle(object.getString("title"));
                    site.setUrlBranding(object.getString("urlBranding"));
                    site.setSiteUrl(object.getString("siteUrl"));
                    site.setAdminEmail(object.getString("adminEmail"));
                    site.setNotifyFlag(object.getInt("notifyFlag"));
                    site.setNotifyEmail(object.getString("notifyEmail"));
                    site.setNotifyFrom(object.getString("notifyFrom"));
                    site.setNotifyMessage(object.getString("notifyMessage"));
                    site.setWorkspaceType(object.getString("workspaceType"));
                    site.setVirtualhost(object.getString("virtualhost"));
                    site.setPath(object.getString("path"));
                    site.setLevel(object.getInt("level"));
                    site.setTheme(object.getString("theme"));
                    site.setVerytransId(object.getString("verytransId"));
//                        site.setCategory();
                    site.setAddress(object.getString("address"));
                    site.setPhone(object.getString("phone"));
                    site.setFax(object.getString("fax"));
                    site.setEmail(object.getString("email"));
                    site.setNpwp(object.getString("npwp"));
                    site.setPostalCode(object.getString("postalCode"));
                    site.setCity(object.getString("city"));
                    site.setType(object.getString("type"));

                    sites.add(site);
                }

                siteDatabaseAdapter.deleteSite();
                siteDatabaseAdapter.saveSite(sites);
                taskService.onSuccess(SignageVariables.SITE_GET_TASK, true);
            } else {
                taskService.onError(SignageVariables.SITE_GET_TASK, "Error");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            taskService.onError(SignageVariables.SITE_GET_TASK, "Error");
        }


    }
}
