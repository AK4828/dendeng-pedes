package com.hoqii.fxpc.sales.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.event.LoginEvent;
import com.hoqii.fxpc.sales.job.LoginManualJob;
import com.hoqii.fxpc.sales.util.AuthenticationCeck;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.hoqii.fxpc.sales.util.LocaleHelper;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.fonts.EntypoIcons;
import com.path.android.jobqueue.JobManager;

import org.meruvian.midas.core.defaults.DefaultActivity;

import java.util.Locale;

import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by meruvian on 29/07/15.
 */
public class LoginActivity extends DefaultActivity {
    @InjectView(R.id.button_login)
    Button submit;
    @InjectView(R.id.edit_username)
    TextView username;
    @InjectView(R.id.edit_password)
    TextView password;
    @InjectView(R.id.login_progress)
    View loginProgress;

    private JobManager jobManager;
    private SharedPreferences preferences, pref;
    private SharedPreferences.Editor editor;
    private AuthenticationCeck authenticationCeck = new AuthenticationCeck();
    private Toolbar toolbar;

    @Override
    protected int layout() {
        return R.layout.activity_login;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pref_list, menu);
        return true;
    }

    private void changeLang(String language) {
        editor = pref.edit();
        editor.putString("language", language);
        editor.commit();

        Resources res = getBaseContext().getResources();
        android.content.res.Configuration resConfig = res.getConfiguration();
        Locale locale = new Locale(language);
        resConfig.locale = locale;
        res.updateConfiguration(resConfig, res.getDisplayMetrics());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_server_prev:
                openDialogSetupParameter();
                break;
            case R.id.menu_lang:
                openDialogSetupLang();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        toolbar = (Toolbar) findViewById(R.id.toolbarBottom);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
    }

    @Override
    public void onViewCreated(Bundle bundle) {
        Log.d(getClass().getSimpleName(), LocaleHelper.getLanguage(this));

        preferences = getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        EventBus.getDefault().register(this);
        SharedPreferences.Editor editor = preferences.edit();

        if (!(preferences.getBoolean("has_url", false))) {
            editor.putString("server_url", SignageVariables.SERVER_URL);
            editor.commit();
        }
        editor.putBoolean("has_url", true);
        editor.commit();

        jobManager = SignageApplication.getInstance().getJobManager();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AuthenticationUtils.getCurrentAuthentication() != null) {
            goToMainActivity();
        }
    }

    @OnClick(R.id.button_login)
    public void submitLogin(Button button) {
//        startActivity(new Intent(this, MainActivity.class));
        if (authenticationCeck.isNetworkAvailable()) {
            LoginManualJob loginJob = new LoginManualJob(username.getText().toString(), password.getText().toString());
            jobManager.addJobInBackground(loginJob);

            SharedPreferences.Editor editor = pref.edit();
            editor.putString("username", username.getText().toString());
            editor.putString("password", password.getText().toString());
            editor.commit();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setTitle(getString(R.string.message_title_internet_access));
            builder.setMessage(getString(R.string.message_no_internet));
            builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    public void onEventMainThread(LoginEvent.DoLogin doLogin) {
        submit.setVisibility(View.GONE);
        loginProgress.setVisibility(View.VISIBLE);
        username.setEnabled(false);
        password.setEnabled(false);
    }

    private void goToMainActivity() {
        if (pref.getBoolean("has_sync", false)) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            startActivity(new Intent(this, SyncActivity.class));
            finish();
            this.finish();
        }
    }

    public void onEventMainThread(LoginEvent.LoginSuccess loginSuccess) {
        goToMainActivity();
    }

    public void onEventMainThread(LoginEvent.LoginFailed loginFailed) {
        loginProgress.setVisibility(View.GONE);
        submit.setVisibility(View.VISIBLE);
        username.setEnabled(true);
        password.setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void openDialogSetupParameter() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.setting_setup_parameter));
        LayoutInflater inflater = this.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.setup_parameter, null);
        builder.setView(convertView);
        final EditText server = (EditText) convertView.findViewById(R.id.edit_server);
        server.setText(preferences.getString("server_url", ""));
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.save), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = preferences.edit();

                editor.putString("server_url", server.getText().toString());
                editor.commit();

                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void openDialogSetupLang() {
        String lang = LocaleHelper.getLanguage(LoginActivity.this);
        final String[] langSelect = {null};
        int langUse = 0;
        switch (lang){
            case "en":
                langUse = 0;
                break;
            case "zh":
                langUse = 1;
                break;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.message_title_language));
        builder.setSingleChoiceItems(new String[]{"English", "Chinese"}, langUse, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        langSelect[0] = "en";
                        break;
                    case 1:
                        langSelect[0] = "zh";
                        break;
                }
            }
        });
        builder.setCancelable(false);
        builder.setPositiveButton(getResources().getString(R.string.save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LocaleHelper.setLocale(LoginActivity.this, langSelect[0]);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

}
