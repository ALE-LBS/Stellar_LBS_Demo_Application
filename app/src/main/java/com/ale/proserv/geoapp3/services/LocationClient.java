package com.ale.proserv.geoapp3.services;

import android.util.Log;

import com.polestar.naosdk.api.external.NAOERRORCODE;
import com.polestar.naosdk.api.external.NAOLocationHandle;
import com.polestar.naosdk.api.external.NAOLocationListener;
import com.polestar.naosdk.api.external.NAOSensorsListener;
import com.polestar.naosdk.api.external.NAOServiceHandle;
import com.polestar.naosdk.api.external.NAOSyncListener;
import com.polestar.naosdk.api.external.TNAOFIXSTATUS;

import com.ale.proserv.geoapp3.services.AndroidService;
import com.ale.proserv.geoapp3.MainActivity;



public class LocationClient extends NAOServiceHandle implements NAOLocationListener, NAOSensorsListener, NAOSyncListener {

    protected NAOLocationHandle handle; // generic service handle
    protected MainActivity mainActivityHandle; // generic service handle
    protected String applicationKey;



    public LocationClient(MainActivity mainActivity, String key) {
        Log.i("LocationClient","Location Client Created");
        mainActivityHandle = mainActivity;
        applicationKey = key;
    }

    public void createHandle() {
        Log.i("LocationClient","Handle");
        Log.i("LocationClient",applicationKey);
        if (handle == null) {
            handle = new NAOLocationHandle(mainActivityHandle, AndroidService.class, applicationKey, this, this);
            handle.synchronizeData(this);
            Log.i("LocationClient","Handle Synchronize");
        }
    }

    public boolean startService() {
        if (handle != null) {
            Log.i("LocationClient ","startService");
            return handle.start();
        }
        else {
            Log.i("LocationClient ", "startService handle is null");
            return false;
        }
    }

    public void stopService() {
        if (handle == null) {
            Log.i("LocationClient ", "stopService handle pointer is null");
        }
        else {
            handle.stop();
        }
    }

    // NAOLocationListener interface
    @Override
    public void onLocationChanged(android.location.Location location) {
        Log.i("onLocationChanged", location.getLatitude() + "," + location.getLongitude() + "," + location.getAltitude() + "," + location.getAccuracy() + "," + location.getBearing());
        mainActivityHandle.setLocation(location);
    }

    @Override
    public void onLocationStatusChanged(TNAOFIXSTATUS status) {
        // Your code here
        Log.i("locationClient ", "onLocationStatusChanged event received");
        if (status.equals(TNAOFIXSTATUS.NAO_FIX_AVAILABLE)) {
            Log.i("locationClient ", "onLocationStatusChanged NAO_FIX_AVAILABLE event received");
        }
        else if (status.equals(TNAOFIXSTATUS.NAO_OUT_OF_SERVICE)) {
            Log.i("locationClient ", "onLocationStatusChanged NAO_OUT_OF_SERVICE event received");
        }
        else if (status.equals(TNAOFIXSTATUS.NAO_TEMPORARY_UNAVAILABLE)) {
            Log.i("locationClient ", "onLocationStatusChanged NAO_TEMPORARY_UNAVAILABLE event received");
        }
        else if (status.equals(TNAOFIXSTATUS.NAO_FIX_UNAVAILABLE)) {
            Log.i("locationClient ", "onLocationStatusChanged NAO_FIX_UNAVAILABLE event received");
        }
    }

    @Override
    public void onEnterSite(java.lang.String name) {
        // Your code here
        Log.i("locationClient ", "onEnterSite event received");
        mainActivityHandle.onEnterSite(name);
    }

    @Override
    public void onExitSite (java.lang.String name) {
        // Your code here
        Log.i("locationClient ", "onExitSite event received");
        mainActivityHandle.onExitSite(name);
    }

    @Override
    public void onError(NAOERRORCODE errCode,
                        java.lang.String message) {
        Log.i("locationClient onError", "event received");
        Log.e(this.getClass().getName(), "onError " + message);
        mainActivityHandle.notifyUser("onError " + errCode + " " + message);
    }

    // NAOSensorsListener interface
    @Override
    public void requiresCompassCalibration() {
        Log.i("locationClient ", "requiresCompassCalibration event received");
        /* Receives notification that the client app needs to display calibration message to the user. */
    }

    @Override
    public void requiresWifiOn() {
        Log.i("locationClient ", "requiresWifiOn event received");
        /*  Receives notification that the Wifi needs to be activated on the device */
    }

    @Override
    public void requiresBLEOn() {
        Log.i("locationClient  ", "requiresBLEOn event received");
        /* Receives notification that the Bluetooth needs to be activated on the device */
    }

    @Override
    public void requiresLocationOn() {
        Log.i("locationClient ", "requiresLocationOn event received");
    /* Receives notification that the Localisation needs to be activated on the device */
    }

    // NAOSyncListener interface
    @Override
    public void onSynchronizationSuccess() {
        Log.i("locationClient ", " onSynchronizationSuccess event received");
    }

    @Override
    public void onSynchronizationFailure(NAOERRORCODE errorCode,
                                         java.lang.String message) {
        Log.i("locationClient ", "onSynchronizationFailure event received " + errorCode.toString());
    }
}
