package com.hoqii.fxpc.sales.util;

import android.util.Log;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by miftakhul on 1/18/16.
 */
public class AuthenticationCeck {

    public AuthenticationCeck(){

    }

    public boolean isAccess(){
        boolean access = false;
        long expiresIn = AuthenticationUtils.getCurrentAuthentication().getExpiresIn();
        Log.d(getClass().getSimpleName(), "site expiresIn : "+expiresIn);
        Log.d(getClass().getSimpleName(), "login time default : "+AuthenticationUtils.getCurrentAuthentication().getLoginTime());


        long loginTime = AuthenticationUtils.getCurrentAuthentication().getLoginTime();
        Log.d(getClass().getSimpleName(), "login time : "+loginTime);

        long curentTime = System.currentTimeMillis();
        Log.d(getClass().getSimpleName(), "curent time : "+curentTime);

        long realDuration = curentTime - loginTime;
        Log.d(getClass().getSimpleName(), "real duration: "+realDuration);

        long realDurationInSecon = TimeUnit.MILLISECONDS.toSeconds(realDuration);
        Log.d(getClass().getSimpleName(), "real duration insecon: "+realDurationInSecon);

        if (expiresIn > realDurationInSecon){
            access = true;
            Log.d(getClass().getSimpleName(), "access status : granted");
        }else {
            access = false;
        }
        Log.d(getClass().getSimpleName(), "access status : " + String.valueOf(access));
        return access;
    }
}
