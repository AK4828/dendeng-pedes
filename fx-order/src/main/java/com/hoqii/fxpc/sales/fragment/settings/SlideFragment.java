package com.hoqii.fxpc.sales.fragment.settings;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;

import com.hoqii.fxpc.sales.R;

/**
 * Created by ludviantoovandi on 16/02/15.
 */
public class SlideFragment extends PreferenceFragment {
    private ListPreference timerList, idleList;
    private SwitchPreference slideShow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(getActivity(), R.xml.setting_slide, false);

        addPreferencesFromResource(R.xml.setting_slide);

        slideShow = (SwitchPreference) findPreference("slide_show");
        timerList = (ListPreference) findPreference("slide_timer");
        idleList = (ListPreference) findPreference("slide_idle");

        slideShow.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object obj) {
                if (obj != null) {
                    boolean check = (Boolean) obj;
                    initialize(check);

                    return true;
                }

                return false;
            }
        });

//        timerList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object obj) {
//                if (obj != null) {
//                    Integer result = Integer.getInteger(obj.toString(), 0);
//
//                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
//                    editor.putInt("slide_timer", result);
//                    editor.commit();
//
//                    return true;
//                }
//
//                return false;
//            }
//        });

//        idleList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object obj) {
//                if (obj != null) {
//                    Integer result = Integer.getInteger(obj.toString(), 0);
//
//                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
//                    editor.putInt("slide_idle", result);
//                    editor.commit();
//
//                    return true;
//                }
//                return false;
//            }
//        });
    }

    private void initialize(boolean isAuto) {
        if (isAuto) {
            timerList.setEnabled(true);
            idleList.setEnabled(true);
            timerList.setSelectable(true);
            idleList.setSelectable(true);
        } else {
            timerList.setEnabled(false);
            idleList.setEnabled(false);
            timerList.setSelectable(false);
            idleList.setSelectable(false);
        }
    }
}
