package com.ale.proserv.geoapp3;

import android.app.Application;

import io.mapwize.mapwizeformapbox.AccountManager;

/**
 * Created by vaymonin on 20/10/2017.
 */

public class MainApplication extends Application {

    private static boolean activityVisible;

    @Override
    public void onCreate() {
        AccountManager.start(this, Keys.getMwzApiKey());
        super.onCreate();
    }

    public static void activityPaused(){
        activityVisible = false;
    }

    public static void activityResumed(){
        activityVisible = true;
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }
}
