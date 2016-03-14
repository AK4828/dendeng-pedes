package com.hoqii.fxpc.sales.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.event.GenericEvent;
import com.hoqii.fxpc.sales.util.AuthenticationCeck;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;
import com.hoqii.fxpc.sales.util.LocaleHelper;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by miftakhul on 12/22/15.
 */
public class PreferenceActivity extends android.preference.PreferenceActivity {
    private static final int REFRESH_TOKEN_SYNC = 300;
    private static final int REFRESH_TOKEN_MANUAL = 301;
    private static AuthenticationCeck authenticationCeck = new AuthenticationCeck();
    private static String[] languages = new String[]{"English", "Chinese"};

    private static SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new preferenceFragment()).commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_header, target);
    }

    public static class preferenceFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
        private Preference sync, logout, refreshToken, language;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
            sync = (Preference) findPreference("pref_sync");
            refreshToken = (Preference) findPreference("pref_refresh_token");
            logout = (Preference) findPreference("pref_logout");
            language = findPreference(this.getString(R.string.key_pref_language));

            sync.setOnPreferenceClickListener(this);
            refreshToken.setOnPreferenceClickListener(this);
            logout.setOnPreferenceClickListener(this);


        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (preference == sync) {
                if (authenticationCeck.isNetworkAvailable()) {
                    if (authenticationCeck.isAccess()) {
                        Log.d(getClass().getSimpleName(), "[ application access granted ]");
                        startActivity(new Intent(getActivity(), SyncActivity.class));
                        getActivity().setResult(RESULT_OK);
                        getActivity().finish();
                    } else {
                        Log.d(getClass().getSimpleName(), "[ application access need to refresh ]");
                        authenticationCeck.refreshToken(getActivity(), REFRESH_TOKEN_SYNC);
                    }
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getResources().getString(R.string.message_title_internet_access));
                    builder.setMessage(getResources().getString(R.string.message_no_internet));
                    builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }
            } else if (preference == refreshToken) {
                if (authenticationCeck.isNetworkAvailable()) {
                    Log.d(getClass().getSimpleName(), "[ application access need to refresh ]");
                    authenticationCeck.refreshToken(getActivity(), REFRESH_TOKEN_MANUAL);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(getResources().getString(R.string.message_title_internet_access));
                    builder.setMessage(getResources().getString(R.string.message_no_internet));
                    builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }
            } else if (preference == logout) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setCancelable(false);
                builder.setTitle(getResources().getString(R.string.message_title_logout));
                builder.setMessage(getResources().getString(R.string.message_logout));
                builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        AuthenticationUtils.logout();
                        SharedPreferences.Editor editorHas = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                        editorHas.putBoolean("has_sync", false);
                        editorHas.commit();
                        getActivity().setResult(RESULT_OK);
                        getActivity().finish();

                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            } else if (preference == language) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setSingleChoiceItems(R.array.language_names_array, 0, null);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String lang = mSharedPreferences.getString(getString(R.string.key_pref_language), "en");
                        LocaleHelper.setLocale(getActivity(), lang);
                        getActivity().recreate();
                    }
                });

                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });



            }
            return false;
        }
    }

    public void onEventMainThread(GenericEvent.RequestSuccess requestSuccess) {
        Log.d(getClass().getSimpleName(), "RequestSuccess: " + requestSuccess.getProcessId());
        switch (requestSuccess.getProcessId()) {
            case REFRESH_TOKEN_SYNC:
                startActivity(new Intent(this, SyncActivity.class));
                setResult(RESULT_OK);
                finish();
                break;
            case REFRESH_TOKEN_MANUAL:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setTitle(getString(R.string.message_refresh_title));
                builder.setMessage(getString(R.string.messsage_refresh_complated));
                builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                break;
        }
    }
}
