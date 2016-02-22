package com.hoqii.fxpc.sales.fragment.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.hoqii.fxpc.sales.R;
import com.hoqii.fxpc.sales.activity.SyncActivity;

/**
 * Created by ludviantoovandi on 30/01/15.
 */
public class DataFragment extends PreferenceFragment {

    protected boolean isValidFragment(String fragmentName) {
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(getActivity(), R.xml.setting_data, false);

        addPreferencesFromResource(R.xml.setting_data);

        Preference preference = findPreference("start_sync");
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getActivity(), SyncActivity.class);
                intent.putExtra("just_sync", true);

                startActivity(intent);

                return false;
            }
        });
    }
}
