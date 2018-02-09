package com.ale.proserv.geoapp3.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ale.proserv.geoapp3.MainActivity;
import com.ale.proserv.geoapp3.R;
import com.visioglobe.visiomoveessential.VMEMapView;
import com.visioglobe.visiomoveessential.interfaces.VMEMapListener;
import com.visioglobe.visiomoveessential.interfaces.VMESearchViewInterface;
import com.visioglobe.visiomoveessential.model.VMEPosition;

/**
 * Created by vaymonin on 23/10/2017.
 */

public class VisioglobeSearchFragment extends android.support.v4.app.Fragment{

    /** The fragment's map view. */
    private VMEMapView mMapView;

    private MainActivity mainActivity;

    /**
     * This is the callback that will be notified of search view events.
     */
    private VMESearchViewInterface.SearchViewCallback mSearchViewCallback = new VMESearchViewInterface.SearchViewCallback() {
        @Override public void searchView(VMEMapView pMapView, String pPlaceID) {
            mainActivity.setSelectedArea(pPlaceID);
        }
        @Override public void searchViewDidCancel(VMEMapView pMapView) {
        }
    };

    /**
     * The location demo fragment's map listener that will be notified of map events.
     */
    private VMEMapListener mMapListener = new VMEMapListener() {
        @Override public void mapReadyForPlaceUpdate(VMEMapView pMapBlockView) {
            Log.i("VG Search Fragment", "mapReadyForPlaceUpdate ");
        }
        @Override public void mapDidLoad(VMEMapView pMapBlockView) {
            Log.i("VG Search Fragment", "mapDidLoad ");
            mMapView.showSearchViewWithTitle("Search place", mSearchViewCallback);
        }

        @Override public boolean mapDidSelectPlace(VMEMapView pMapBlock, String pPlaceId, VMEPosition pPosition) {
            Log.i("VG Search Fragment", "mapDidSelectPlace ");
            return false;
        }
    };

    public VisioglobeSearchFragment() {
    }

    public void rootMemorize (MainActivity father) {
        mainActivity = father;
    }

    @Override
    public View onCreateView(LayoutInflater pInflater, ViewGroup pContainer, Bundle pSavedInstanceState) {
        if (mMapView == null) {
            // Inflate the fragment's layout
            if (mainActivity.getVenue() == R.id.brestmap) {
                mMapView = (VMEMapView) pInflater.inflate(R.layout.search_fragment_brest_visioglobe, pContainer , false);
            }
            else if (mainActivity.getVenue() == R.id.colombesmap) {
                mMapView = (VMEMapView) pInflater.inflate(R.layout.search_fragment_colombes_visioglobe, pContainer , false);
            }
            /*else if (mainActivity.getVenue() == R.id.simultracking) {
                mMapView = (VMEMapView) pInflater.inflate(R.layout.search_fragment_monaco_visioglobe, pContainer , false);
            }
            else if (mainActivity.getVenue() == R.id.simulgeofence) {
                mMapView = (VMEMapView) pInflater.inflate(R.layout.search_fragment_monaco_visioglobe, pContainer , false);
            }*/
            // Set up map listener to know when map view has loaded.
            mMapView.setMapListener(mMapListener);
            // Load the map
            mMapView.loadMap();
        }
        return mMapView;
    }

    @Override
    public void onPause () {
        try {
            Log.i("VG Search Fragment", " onPause");
            super.onPause();
            mMapView.onPause();
        }
        catch (Exception e) {
            Log.i("SearchFr pauseEx", e.getMessage());
        }
    }

    @Override
    public void onResume () {
        try {
            Log.i("VG Search Fragment", " onResume");
            super.onResume();
            mMapView.onResume();
        }
        catch (Exception e) {
            Log.i("SearchFr resumeEx", e.getMessage());
        }
    }

}
