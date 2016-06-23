package com.hoqii.fxpc.sales.service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoqii.fxpc.sales.SignageApplication;
import com.hoqii.fxpc.sales.entity.GcmStatus;

import java.io.IOException;

/**
 * Created by miftakhul on 3/16/16.
 */
public class GcmUtils {

    public static void registerGcmSession() {
        Intent registerService = new Intent(SignageApplication.getInstance(), RegistrationIntentService.class);
        SignageApplication.getInstance().startService(registerService);
    }

    public static void saveGcmToken(String token, boolean isSync) throws JsonProcessingException {
        GcmStatus g = new GcmStatus();
        int status = 0;
        if (isSync) {
            status = 1;
        } else {
            status = 0;
        }
        g.setToken(token);
        g.setStatus(status);
        ObjectMapper mapper = SignageApplication.getObjectMapper();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SignageApplication.getInstance());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("gcmModel", mapper.writeValueAsString(g));
        editor.commit();

        Log.d("token id", "[ " + token + " ]");
    }

    public static GcmStatus getGcmModel() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SignageApplication.getInstance());
        String gcmModel = preferences.getString("gcmModel", null);
        if (gcmModel == null){
            return null;
        }
        ObjectMapper mapper = SignageApplication.getObjectMapper();
        GcmStatus gcmStatus = null;
        try {
            gcmStatus = mapper.readValue(gcmModel, GcmStatus.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("get token id ", "[ " + gcmStatus.getToken() + " ]");
        return gcmStatus;
    }

    public static boolean isRegistered() {
        boolean registered = false;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SignageApplication.getInstance());
        String gcmToken = preferences.getString("gcmModel", null);
        if (gcmToken == null) {
            registered = false;
        } else {
            registered = true;
        }
        return registered;
    }

    public static void clearGcmToken(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SignageApplication.getInstance());
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

    public static void unregisterGcmToken() {

    }

}
