package com.hoqii.fxpc.sales.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.util.AuthenticationUtils;

import java.util.List;

/**
 * Created by miftakhul on 12/22/15.
 */
public class PreferenceActivity extends android.preference.PreferenceActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new preferenceFragment()).commit();
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_header, target);
    }

    public static class preferenceFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener{
        private Preference sync, logout;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
            sync = (Preference) findPreference("pref_sync");
            logout = (Preference) findPreference("pref_logout");

            sync.setOnPreferenceClickListener(this);
            logout.setOnPreferenceClickListener(this);


        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if (preference == sync){
                startActivity(new Intent(getActivity(), SyncActivity.class));
                getActivity().setResult(RESULT_OK);
                getActivity().finish();
            }else if (preference == logout){
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
            return false;
        }
    }
}
