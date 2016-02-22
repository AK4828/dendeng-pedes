package com.hoqii.fxpc.sales.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.SignageAppication;
import com.hoqii.fxpc.sales.SignageVariables;
import com.hoqii.fxpc.sales.event.LoginEvent;
import com.hoqii.fxpc.sales.job.LoginManualJob;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.joanzapata.iconify.IconDrawable;
import com.joanzapata.iconify.Iconify;
import com.joanzapata.iconify.fonts.EntypoIcons;
import com.joanzapata.iconify.fonts.EntypoModule;
import com.joanzapata.iconify.fonts.FontAwesomeModule;
import com.joanzapata.iconify.fonts.IoniconsModule;
import com.joanzapata.iconify.fonts.MaterialCommunityModule;
import com.joanzapata.iconify.fonts.MaterialModule;
import com.joanzapata.iconify.fonts.MeteoconsModule;
import com.joanzapata.iconify.fonts.SimpleLineIconsModule;
import com.joanzapata.iconify.fonts.TypiconsModule;
import com.joanzapata.iconify.fonts.WeathericonsModule;
import com.path.android.jobqueue.JobManager;

import org.meruvian.midas.core.defaults.DefaultActivity;

import butterknife.InjectView;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

/**
 * Created by meruvian on 29/07/15.
 */
public class LoginActivity extends DefaultActivity {
    @InjectView(R.id.button_login)
    Button submit;
    @InjectView(R.id.prev_login)
    ImageButton prevLogin;
    @InjectView(R.id.edit_username)
    TextView username;
    @InjectView(R.id.edit_password)
    TextView password;
    @InjectView(R.id.login_progress)
    View loginProgress;

    private JobManager jobManager;
    private SharedPreferences preferences;

    private Toolbar toolbar;

    @Override
    protected int layout() {
        return R.layout.activity_login;
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_url, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if(item.getItemId() == R.id.menu_setup_parameter){
//            openDialogSetupParameter();
//        }
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        toolbar = (Toolbar) findViewById(org.meruvian.midas.core.R.id.toolbar);
//        setSupportActionBar(toolbar);
    }

    @Override
    public void onViewCreated(Bundle bundle) {
        Iconify
                .with(new FontAwesomeModule())
                .with(new EntypoModule())
                .with(new TypiconsModule())
                .with(new MaterialModule())
                .with(new MaterialCommunityModule())
                .with(new MeteoconsModule())
                .with(new WeathericonsModule())
                .with(new SimpleLineIconsModule())
                .with(new IoniconsModule());

        preferences = getSharedPreferences(SignageVariables.PREFS_SERVER, 0);
        EventBus.getDefault().register(this);
        SharedPreferences.Editor editor = preferences.edit();

        if (!(preferences.getBoolean("has_url", false))) {
            editor.putString("server_url", SignageVariables.SERVER_URL);
            editor.commit();
        }
        editor.putBoolean("has_url", true);
        editor.commit();

        jobManager = SignageAppication.getInstance().getJobManager();

        prevLogin.setImageDrawable(new IconDrawable(this, EntypoIcons.entypo_dots_three_vertical).colorRes(R.color.grey).actionBarSize());
        prevLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openDialogSetupParameter();
            }
        });
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

        LoginManualJob loginJob = new LoginManualJob(username.getText().toString(), password.getText().toString());
        jobManager.addJobInBackground(loginJob);
    }

    public void onEventMainThread(LoginEvent.DoLogin doLogin) {
        submit.setVisibility(View.GONE);
        loginProgress.setVisibility(View.VISIBLE);
        username.setEnabled(false);
        password.setEnabled(false);
    }

    private void goToMainActivity() {
        if (preferences.getBoolean("has_sync", false)) {
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
        builder.setTitle("Setup Parameter");
        LayoutInflater inflater = this.getLayoutInflater();
        View convertView = inflater.inflate(R.layout.setup_parameter, null);
        builder.setView(convertView);
        final EditText server = (EditText) convertView.findViewById(R.id.edit_server);
        server.setText(preferences.getString("server_url", ""));
        builder.setCancelable(false);
        builder.setPositiveButton("Simpan", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = preferences.edit();

                editor.putString("server_url", server.getText().toString());
                editor.commit();

                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

}
