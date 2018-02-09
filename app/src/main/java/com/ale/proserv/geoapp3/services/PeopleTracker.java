package com.ale.proserv.geoapp3.services;

import com.ale.proserv.geoapp3.util.HttpHelper;

/**
 * Created by vaymonin on 24/10/2017.
 */

public class PeopleTracker {
    private static final String ROOT_URL 		= "https://www.nao-cloud.com";
    private static final String AUTH_TOKEN 		= "nqtThzCxSturzqpwdkzsPD99GheArA"; // Authentication API of Jerome Elleouet
    private static final String PEOPLE_TRACKING_URL 		= ROOT_URL + "/nao_trackables/record_location?site_id=ID&auth_token=" + AUTH_TOKEN;
    private static final PeopleTracker instance = new PeopleTracker();

    public static final PeopleTracker instance() {
        return instance;
    }

    private boolean isEnabled = true;
    private boolean isSendingLocation = false;

    public boolean isEnabled(){
        return this.isEnabled;
    }

    public void setEnabled(boolean isEnabled){
        this.isEnabled = isEnabled;
    }

    public void recordLocation(final int siteId, final String user, final double lon, final double lat, final double alt){
        if (isEnabled && !isSendingLocation) {
            isSendingLocation = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String url = PEOPLE_TRACKING_URL.replace("ID", String.valueOf(siteId));
                    HttpHelper.postText(url,
                            "nao_trackable[name]", user,
                            "nao_trackable[lon]", String.valueOf(lon),
                            "nao_trackable[lat]", String.valueOf(lat),
                            "nao_trackable[alt]", String.valueOf(alt)
                    );
                    isSendingLocation = false;
                }
            }).start();


        }
    }
}
