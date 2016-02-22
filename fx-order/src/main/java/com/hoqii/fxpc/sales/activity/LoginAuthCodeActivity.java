package com.hoqii.fxpc.sales.activity;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import org.meruvian.midas.core.defaults.DefaultActivity;
import org.meruvian.midas.core.service.TaskService;

/**
 * Created by meruvian on 13/08/15.
 */
public class LoginAuthCodeActivity extends DefaultActivity implements TaskService {
    private ProgressDialog progressDialog;
    private SharedPreferences preferences;

    @Override
    protected int layout() {
        return 0;
    }

    @Override
    public void onViewCreated(Bundle bundle) {

    }

    @Override
    public void onExecute(int code) {

    }

    @Override
    public void onSuccess(int code, Object result) {

    }

    @Override
    public void onCancel(int code, String message) {

    }

    @Override
    public void onError(int code, String message) {

    }
}
