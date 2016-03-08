package com.hoqii.fxpc.sales.job;

import android.content.SharedPreferences;
import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.core.commons.Role;
import com.hoqii.fxpc.sales.core.commons.Site;
import com.hoqii.fxpc.sales.core.commons.User;
import com.hoqii.fxpc.sales.entity.Authentication;
import com.hoqii.fxpc.sales.entity.PageEntity;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.event.LoginEvent;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.hoqii.fxpc.sales.util.JsonRequestUtils;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by meruvian on 29/07/15.
 */
public abstract class LoginJob extends Job {
    private SharedPreferences preferences;
    protected LoginJob(Params params) {
        super(params);
    }

    @Override
    public void onAdded() {
        EventBus.getDefault().post(new LoginEvent.DoLogin());
    }

    protected void registerAuthentication(JsonRequestUtils.HttpResponseWrapper<Authentication> responseWrapper) {
        HttpResponse response = responseWrapper.getHttpResponse();

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            Authentication authentication = responseWrapper.getContent();
            AuthenticationUtils.registerAuthentication(authentication);
            User user = requestUser();
            Site site = requestSite();
            Site parentSite = requestParentSite(site.getId());

            user.setRoles(new ArrayList<Role>());
            for (Role role : requestRoles().getContent()) {
                user.getRoles().add(role);
            }

            authentication.setUser(user);
            authentication.setSite(site);
            authentication.setParentSite(parentSite);

            long loginTime = System.currentTimeMillis();
            Log.d(getClass().getSimpleName(), "login time : "+loginTime);
            authentication.setLoginTime(loginTime);

            AuthenticationUtils.registerAuthentication(authentication);
            Log.i(getClass().getSimpleName(), "ACCESS_TOKEN : " + authentication.getAccessToken());

            EventBus.getDefault().post(new LoginEvent.LoginSuccess(responseWrapper.getContent()));
        } else {
            EventBus.getDefault().post(new LoginEvent.LoginFailed(response.getStatusLine().getStatusCode()));
        }
    }


    protected User requestUser() {
        preferences = SignageApplication.getInstance().getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        JsonRequestUtils requestUtils = new JsonRequestUtils(preferences.getString("server_url", "") + SignageVariables.PGA_CURRENT_ME);
        return requestUtils.get(new TypeReference<User>() {}).getContent();
    }

    protected Site requestSite() {
        preferences = SignageApplication.getInstance().getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        JsonRequestUtils requestUtils = new JsonRequestUtils(preferences.getString("server_url", "") + SignageVariables.PGA_CURRENT_SITE);
        return requestUtils.get(new TypeReference<Site>() {}).getContent();
    }

    protected Site requestParentSite(String siteId) {
        preferences = SignageApplication.getInstance().getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        JsonRequestUtils requestUtils = new JsonRequestUtils(preferences.getString("server_url", "") + SignageVariables.PGA_PARENT_SITE + siteId + "/parents");
        return requestUtils.get(new TypeReference<Site>() {}).getContent();
    }

    protected PageEntity<Role> requestRoles() {
        preferences = SignageApplication.getInstance().getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        JsonRequestUtils requestUtils = new JsonRequestUtils(preferences.getString("server_url", "") + SignageVariables.PGA_CURRENT_ROLE);
        return requestUtils.get(new TypeReference<PageEntity<Role>>() {}).getContent();
    }

    @Override
    protected void onCancel() {
        EventBus.getDefault().post(new LoginEvent.LoginFailed(0));
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        Log.e(LoginJob.class.getSimpleName(), throwable.getMessage(), throwable);
        return false;
    }
}