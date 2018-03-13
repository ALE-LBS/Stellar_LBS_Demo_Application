package com.ale.proserv.geoapp3.services;

import android.util.Log;

import com.ale.proserv.geoapp3.MainActivity;
import com.polestar.naosdk.api.external.NAOERRORCODE;
import com.polestar.naosdk.api.external.NAOGeofenceListener;
import com.polestar.naosdk.api.external.NAOGeofencingHandle;
import com.polestar.naosdk.api.external.NAOGeofencingListener;
import com.polestar.naosdk.api.external.NAOServiceHandle;
import com.polestar.naosdk.api.external.NAOSyncListener;
import com.polestar.naosdk.api.external.NaoAlert;

/**
 * Created by vaymonin on 01/02/2018.
 */

public class GeofencingClient extends NAOServiceHandle implements NAOGeofencingListener, NAOGeofenceListener, NAOSyncListener {
    protected NAOGeofencingHandle handle; // generic service handle

    protected MainActivity mainActivityHandle; // generic service handle
    protected String applicationKey;

    public GeofencingClient (MainActivity mainActivity, String key) {
        mainActivityHandle = mainActivity;
        applicationKey = key;
    }

    public void createHandle() {
        if (handle == null) {
            handle = new NAOGeofencingHandle(mainActivityHandle, AndroidService.class, applicationKey, this, null);
            handle.synchronizeData(this);
        }
    }

    public boolean startService() {
        if (handle != null) {
            Log.i("GeofencingClient ", "startService");
            return handle.start();
        }
        else {
            Log.i("GeofencingClient ", "startService handle is null");
            return false;
        }
    }

    public void stopService() {
        if (handle == null) {
            Log.i("GeofencingClient ", "stopService handle pointer is null");

        }
        else {
            Log.i("GeofencingClient ", "stopService");
            handle.stop();
        }
    }

    @Override
    public void onFireNaoAlert(NaoAlert alert) {
        // Your code here
        long startTime;
        long endTime;

        Log.i("GeofencingClient ", "onFireNaoAlert event received");
        String nameAlert = alert.getName();
        Log.i("onFireNaoAlert", nameAlert);
        startTime = alert.getStartTime();
        endTime = alert.getEndTime();


        if (startTime != 0) {
            Log.i("GeoF client NAO Alert: ", Integer.toString((int) startTime));
        }
        else {
            Log.i("GeoF client NAO Alert: ","date == 0!!!");
        }

        if (endTime != 0) {
            Log.i("GeoF client NAO Alert: ", Integer.toString((int) endTime));
        }
        else {
            Log.i("GeoF client NAO Alert:","date == 0!!!");
        }

        String content = alert.getContent();
        if (content != null && !content.isEmpty()) {
            Log.i("GeoF client NAO Alert: ", content);
        }
        else {
            Log.i("GeoF client NAO Alert: ", "vide!!");
        }




        // send id to main activity
        mainActivityHandle.onFireNaoAlert(alert);

    }

    @Override
    public void onEnterGeofence(int regionId, java.lang.String regionName) {
        Log.i("GeofencingClient ", "onEnterGeofence event received");
        mainActivityHandle.onEnterGeofence( regionId,  regionName);

    }

    @Override
    public void onExitGeofence(int regionId, java.lang.String regionName) {
        Log.i("GeofencingClient ", "v event received");
        mainActivityHandle.onExitGeofence( regionId,  regionName);
    }

    @Override
    public void onError(NAOERRORCODE errCode,
                        String message) {
        Log.i("GeofencingClient ", "onError event received");
        Log.e(this.getClass().getName(), "onError " + message);
        //  mainActivityHandle.notifyUser("onError " + errCode + " " + message);
    }

    @Override
    public void onSynchronizationSuccess() {
        Log.i("GeofencingClient","Synchronization success");
    }

    @Override
    public void onSynchronizationFailure(NAOERRORCODE naoerrorcode, String s) {
        Log.i("GeofencingeClient","Synchronization failed: "+naoerrorcode.name());
    }
}
