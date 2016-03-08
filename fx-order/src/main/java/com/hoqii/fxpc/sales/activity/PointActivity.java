package com.hoqii.fxpc.sales.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.core.LogInformation;
import com.hoqii.fxpc.sales.core.commons.Site;
import com.hoqii.fxpc.sales.entity.SitePoint;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.event.LoginEvent;
import com.hoqii.fxpc.sales.job.RefreshTokenJob;
import com.hoqii.fxpc.sales.util.AuthenticationCeck;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.TypiconsIcons;
import com.joanzapata.iconify.widget.IconTextView;
import com.path.android.jobqueue.JobManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.meruvian.midas.core.service.TaskService;
import org.meruvian.midas.core.util.ConnectionUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by miftakhul on 12/8/15.
 */
public class PointActivity extends AppCompatActivity implements TaskService {
    private static final int REFRESH_TOKEN_POINT = 300;
    private List<SitePoint> PointList = new ArrayList<SitePoint>();
    private SharedPreferences preferences;
    private Toolbar toolbar;
    private SitePoint point;
    private IconTextView email, reward, spinner;
    private TextView descript, siteName;
    private LinearLayout pointInfo;
    private AuthenticationCeck authenticationCeck = new AuthenticationCeck();

    private String pointUrl = "/api/points/current";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward);
        EventBus.getDefault().register(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setEnterTransition(new Fade());
            getWindow().setExitTransition(new Fade());
        }

        preferences = getSharedPreferences(SignageVariables.PREFS_SERVER, 0);

        toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(R.string.nav_point);
        actionBar.setHomeAsUpIndicator(new IconDrawable(this, TypiconsIcons.typcn_chevron_left).colorRes(R.color.white).actionBarSize());

        init();

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (authenticationCeck.isAccess()) {
                    PointSync pointSync = new PointSync(PointActivity.this, PointActivity.this);
                    pointSync.execute(AuthenticationUtils.getCurrentAuthentication().getSite().getId());
                    Log.d(getClass().getSimpleName(), "[ acces true / refreshing token not needed]");
                } else {
                    Log.d(getClass().getSimpleName(), "[ acces false / refreshing token]");
                    authenticationCeck.refreshToken(PointActivity.this, REFRESH_TOKEN_POINT);
                }
            }
        });

    }

    private void init() {
        descript = (TextView) findViewById(R.id.descrip);
        siteName = (TextView) findViewById(R.id.site_name);
        email = (IconTextView) findViewById(R.id.site_email);
        reward = (IconTextView) findViewById(R.id.point_count);
        spinner = (IconTextView) findViewById(R.id.spinner);
        pointInfo = (LinearLayout) findViewById(R.id.point_info);

        descript.setText(AuthenticationUtils.getCurrentAuthentication().getSite().getDescription());
        siteName.setText(AuthenticationUtils.getCurrentAuthentication().getSite().getName());
        email.setText("{typcn-mail} " + AuthenticationUtils.getCurrentAuthentication().getSite().getAdminEmail());
        reward.setText("{typcn-star-outline} Point : ---");
    }

    private void initSet() {
        reward.setText("{typcn-star-outline} Point : " + Double.toString(point.getPoint()));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onExecute(int code) {
        spinner.setVisibility(View.VISIBLE);
        pointInfo.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onSuccess(int code, Object result) {
        if (point != null) {
            initSet();
        }
        spinner.setVisibility(View.GONE);
        pointInfo.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCancel(int code, String message) {
        spinner.setVisibility(View.GONE);
    }

    @Override
    public void onError(int code, String message) {
        spinner.setVisibility(View.GONE);
    }

    class PointSync extends AsyncTask<String, Void, JSONObject> {

        private Context context;
        private TaskService taskService;

        public PointSync(Context context, TaskService taskService) {
            this.context = context;
            this.taskService = taskService;
        }


        @Override
        protected JSONObject doInBackground(String... JsonObject) {
            Log.d(getClass().getSimpleName(), "?acces_token= " + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
            return ConnectionUtil.get(preferences.getString("server_url", "") + pointUrl + "?access_token="
                    + AuthenticationUtils.getCurrentAuthentication().getAccessToken());
        }

        @Override
        protected void onCancelled() {
            taskService.onCancel(SignageVariables.POINT_GET_TASK, "Batal");
        }

        @Override
        protected void onPreExecute() {
            taskService.onExecute(SignageVariables.POINT_GET_TASK);
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if (result != null) {

                    Log.d("result point =====", result.toString());
                    JSONObject object = result;

                    point = new SitePoint();
                    point.setId(object.getString("id"));
                    point.setPoint(new Double(object.getString("point")));

                    JSONObject logInformationObject = new JSONObject();
                    if (!object.isNull("logInformation")) {
                        logInformationObject = object.getJSONObject("logInformation");

                        LogInformation logInformation = new LogInformation();
                        logInformation.setCreateDate(new Date(logInformationObject.getLong("createDate")));

                        point.setLogInformation(logInformation);
                    }

                    JSONObject siteObject = new JSONObject();
                    if (!object.isNull("site")) {
                        siteObject = object.getJSONObject("site");

                        Site site = new Site();
                        site.setId(object.getString("id"));

                        site.setName(siteObject.getString("name"));
                        site.setDescription(siteObject.getString("description"));
                        site.setTitle(siteObject.getString("title"));
                        site.setUrlBranding(siteObject.getString("urlBranding"));
                        site.setSiteUrl(siteObject.getString("siteUrl"));
                        site.setAdminEmail(siteObject.getString("adminEmail"));
                        site.setNotifyFlag(siteObject.getInt("notifyFlag"));
                        site.setNotifyEmail(siteObject.getString("notifyEmail"));
                        site.setNotifyFrom(siteObject.getString("notifyFrom"));
                        site.setNotifyMessage(siteObject.getString("notifyMessage"));
                        site.setWorkspaceType(siteObject.getString("workspaceType"));
                        site.setVirtualhost(siteObject.getString("virtualhost"));
                        site.setPath(siteObject.getString("path"));
                        site.setLevel(siteObject.getInt("level"));
                        site.setTheme(siteObject.getString("theme"));
                        site.setVerytransId(siteObject.getString("verytransId"));
//                        site.setCategory();
                        site.setAddress(siteObject.getString("address"));
                        site.setPhone(siteObject.getString("phone"));
                        site.setFax(siteObject.getString("fax"));
                        site.setEmail(siteObject.getString("email"));
                        site.setNpwp(siteObject.getString("npwp"));
                        site.setPostalCode(siteObject.getString("postalCode"));
                        site.setCity(siteObject.getString("city"));
                        site.setType(siteObject.getString("type"));

                        point.setSite(site);
                    }

                    taskService.onSuccess(SignageVariables.POINT_GET_TASK, true);
                } else {
                    taskService.onError(SignageVariables.POINT_GET_TASK, "Error");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                taskService.onError(SignageVariables.POINT_GET_TASK, "Error");
            }


        }
    }

    public void onEventMainThread(GenericEvent.RequestSuccess requestSuccess) {
        Log.d(getClass().getSimpleName(), "RequestSuccess: " + requestSuccess.getProcessId());
        switch (requestSuccess.getProcessId()) {
            case REFRESH_TOKEN_POINT:
                PointSync pointSync = new PointSync(PointActivity.this, PointActivity.this);
                pointSync.execute(AuthenticationUtils.getCurrentAuthentication().getSite().getId());
                break;
        }
    }

}
