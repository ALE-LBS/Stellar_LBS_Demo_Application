package com.ale.proserv.geoapp3.fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ale.proserv.geoapp3.MainActivity;
import com.ale.proserv.geoapp3.R;
import com.visioglobe.visiomoveessential.VMEMapView;
import com.visioglobe.visiomoveessential.interfaces.VMEComputeRouteInterface;
import com.visioglobe.visiomoveessential.interfaces.VMEMapListener;
import com.visioglobe.visiomoveessential.model.VMELocation;
import com.visioglobe.visiomoveessential.model.VMEPosition;

import java.util.Arrays;
import java.util.List;

//import static com.ale.proserv.geoapp.demo.R.id.visit_button;

/**
 * This fragment is a demo for VisioMove Essential's VMELocationInterface API.
 */
public class VisioglobeLocationFragment extends android.support.v4.app.Fragment {

    /** The fragment's map view. */
    private VMEMapView mMapView;

    private String mDest;

    /** The fragment's layout. */
    private ViewGroup mFragment;

    private MainActivity mainActivity;


    private Location currentLocation;

    private LocationManager mLocationManager;

    public VisioglobeLocationFragment() {
        mDest = null;
    }

    public void setDestination(String lDest)
    {
        mDest = lDest;
    }

    public void rootMemorize (MainActivity father) {
        mainActivity = father;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater pInflater, ViewGroup pContainer, Bundle pSavedInstanceState) {

        if (mFragment == null)
        {
            mLocationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            /*if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                // If permission to access location was denied, we null out the location manager to
                // prevent trying to use it
                mLocationManager = null;
                // toast notification on app
                Toast.makeText(getContext(), "The application's Location permission must be enabled before the location services will work.", Toast.LENGTH_LONG).show();
            }*/

            // Inflate the fragment's layout
            int a  = mainActivity.getVenue();
            if (mainActivity.getVenue() == R.id.brestmap) {
                mFragment = (ViewGroup) pInflater.inflate(R.layout.location_fragment_brest_visioglobe, pContainer, false);
            }
            else if (mainActivity.getVenue() == R.id.colombesmap)
            {
                mFragment = (ViewGroup) pInflater.inflate(R.layout.location_fragment_colombes_visioglobe, pContainer, false);
            }
            /*else if (mainActivity.getVenue() == R.id.positionSimul)
            {
                mFragment = (ViewGroup) pInflater.inflate(R.layout.location_fragment_brest_visioglobe, pContainer, false);
            }

            else if (mainActivity.getVenue() == R.id.simulgeofence)
            {
                mFragment = (ViewGroup) pInflater.inflate(R.layout.location_fragment_monaco_visioglobe, pContainer, false);
            }*/

            // Add Menu
            setHasOptionsMenu(true);
            // Fetch the views
            mMapView = (VMEMapView) mFragment.findViewById(R.id.map_view);

            // Set up map listener to know when map view has loaded.
            mMapView.setMapListener(mMapListener);
            // Load the map
            mMapView.loadMap();



        }
        else if (mMapView == null) {
            Log.i("VG Fragment", " onCreateView mMapView sans fragment");
        }

        return mFragment;
    }

    public void gotoPlaceId(String placeID)
    {
      /*  Location lLoc = new Location(LocationManager.);
        lLoc.setLatitude(45.7424829);
        lLoc.setLongitude(4.8800862);
        lLoc.setAltitude(52);*/

        List<Object> lDests = new java.util.ArrayList<>();
        // final destination = input parameter
        lDests.addAll(Arrays.asList(placeID));
        VMEComputeRouteInterface.RouteDestinationsOrder lDestOrder = VMEComputeRouteInterface.RouteDestinationsOrder.OPTIMAL;

        VMEComputeRouteInterface.RouteRequest lRouteRequest = new VMEComputeRouteInterface.RouteRequest(VMEComputeRouteInterface.RouteRequestType.FASTEST, lDestOrder, true);

        if (currentLocation != null) {
            VMEPosition lVMEPosition = mMapView.createPositionFromLocation(currentLocation);
            lRouteRequest.setOrigin(lVMEPosition);
        }
        else
        {
            // default origin for BREST GROUND FLOORS
            lRouteRequest.setOrigin("welcome_desk");
            //lRouteRequest.setOrigin("Entrance");
        }

        lRouteRequest.addDestinations(lDests);

        if (mMapView != null) { // avoid NPE
            mMapView.computeRoute(lRouteRequest, mRouteCallback);//no cbk
        }

        //reset
        mDest = null;

    }

    public void updateLocation(Location location) {
        Log.i("VG Fragment"," updateLocation");
        if (mainActivity.isDisplayLocationActive()) {
            Log.i("VG Fragment"," updateLocation on map");
            currentLocation = location;

            //JEL+
            // generic code+
            VMELocation lVMELocation = mMapView.createLocationFromLocation(location);
            mMapView.updateLocation(lVMELocation);
            // generic code-
            // WAIT for updateLocation instead of OnCreateView to go to a direction that has been received from an intent
            if (mDest != null)
            {
                gotoPlaceId(mDest);
            }
            //JEL-
        }
    }

    @Override
    public void onPause () {
        try {
            Log.i("VG Fragment"," onPause");

            super.onPause();
            mMapView.onPause();

            // these lines were causing crash when pausing the application while load the map
    /*        if (mainActivity.isDisplayLocationActive()){
                // Stop location updates.
                mMapView.updateLocation((VMELocation)null);
            }*/
        }
        catch (Exception e) {
            Log.i("LocationFr pauseEx", e.getMessage());
        }
    }

    @Override
    public void onResume () {
        try {

            Log.i("VG Fragment", " onResume");
            super.onResume();
            mMapView.onResume();
        }
        catch (Exception e) {
            Log.i("LocationFr resumeEx", e.getMessage());
        }
    }

    /**
     * The location demo fragment's map listener that will be notified of map events.
     */
    private VMEMapListener mMapListener = new VMEMapListener() {
        @Override public void mapReadyForPlaceUpdate(VMEMapView pMapBlockView) {
            Log.i("VG Fragment", "mapReadyForPlaceUpdate ");
        }

        @Override public void mapDidLoad(VMEMapView pMapBlockView) {
            Log.i("VG Fragment", "mapDidLoad");
            if (mainActivity.isDisplayLocationActive()) {
                if (currentLocation != null) {
                    VMELocation lVMELocation = mMapView.createLocationFromLocation(currentLocation);
                    Log.i("VG Fragment", "mapDidLoad updatelocation");

                    mMapView.updateLocation(lVMELocation);
                }
                //not the best place to apply code : need at least 1 position to be set by NAO
               /* if (mDest != null) {
                    gotoPlaceId(mDest);
                }*/
            }

            if (mainActivity.startServices()) {
                Log.i("VG Fragment ", "startService ok ");
            }
            else {
                Log.i("VG Fragment ", "startService Nok ");
            }
        }

        @Override public boolean mapDidSelectPlace(VMEMapView pMapBlock, String pPlaceId, VMEPosition pPosition) {
            Log.i("VG Fragment", "mapDidSelectPlace ");
            return false;
        }
    };

    public void ComputeRoute() {
        Log.i(" VG Fragment", "ComputeRoute ");
        String dest = null;

        String area = mainActivity.getSelectedArea();

        if (area == null || area.isEmpty()) {
            dest = "service_custo";
            Log.i(" VGFragment", "ComputeRoute -->service_custo");
        } else {
            Log.i(" VG Fragment", "ComputeRoute -->"+area);
            dest = area;
        }

        VMEComputeRouteInterface.RouteRequest lRouteRequest = new VMEComputeRouteInterface.RouteRequest(VMEComputeRouteInterface.RouteRequestType.FASTEST, VMEComputeRouteInterface.RouteDestinationsOrder.OPTIMAL);


        if (currentLocation != null) {
            VMEPosition lVMEPosition = mMapView.createPositionFromLocation(currentLocation);
            lRouteRequest.setOrigin(lVMEPosition);
        }
        else lRouteRequest.setOrigin((VMEPosition)null);

        lRouteRequest.addDestination(dest);

        Log.i(" VG Fragment", " DisplayComputeRoute ");
        mMapView.computeRoute(lRouteRequest, mRouteCallback);
        Log.i(" VG Fragment", "ended ComputeRoute ");
    }

    /**
     * The callback that will be notified of route events.
     */
    private VMEComputeRouteInterface.ComputeRouteCallback mRouteCallback = new VMEComputeRouteInterface.ComputeRouteCallback() {
        @Override public boolean computeRouteDidFinish(VMEMapView pMapView, VMEComputeRouteInterface.RouteRequest pRouteRequest, VMEComputeRouteInterface.RouteResult var3) {
            if (getContext() != null) {
                // toast notification on app
                // Toast.makeText(getContext(), "computeRouteDidFinish", Toast.LENGTH_LONG).show();
            }

            Log.i("VG Fragment ","computeRouteDidFinish");
            return true;
        }
        @Override public void computeRouteDidFail(VMEMapView pMapView, VMEComputeRouteInterface.RouteRequest pRouteRequest, String pError) {
            if (getContext() != null) {

                Toast.makeText(getContext(), "computeRouteDidFail" + pError, Toast.LENGTH_LONG).show();
                mainActivity.displaySearchFragment();
            }
            Log.i("VG Fragment ","computeRouteDidFail"+pError);
        }
    };
}
