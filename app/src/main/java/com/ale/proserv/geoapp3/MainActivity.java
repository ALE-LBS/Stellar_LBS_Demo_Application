package com.ale.proserv.geoapp3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ale.proserv.geoapp3.fragment.VisioglobeLocationFragment;
import com.ale.proserv.geoapp3.fragment.VisioglobeSearchFragment;
import com.ale.proserv.geoapp3.services.GeofencingClient;
import com.ale.proserv.geoapp3.services.LocationClient;
import com.ale.proserv.geoapp3.services.ManualIndoorLocationProvider;
import com.ale.proserv.geoapp3.services.PeopleTracker;
import com.ale.proserv.geoapp3.services.WebFragment;
import com.ale.proserv.geoapp3.util.MapwizeSearchResultsListAdapter;
import com.ale.proserv.geoapp3.util.MapwizeSuggestionWrapper;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.arlib.floatingsearchview.util.Util;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.SphericalUtil;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;
import com.polestar.naosdk.api.external.NAOAlertRule;
import com.polestar.naosdk.api.external.NaoAlert;
import com.polestar.naosdk.api.external.TALERTRULE;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import io.indoorlocation.core.IndoorLocation;
import io.mapwize.mapwizeformapbox.MapOptions;
import io.mapwize.mapwizeformapbox.MapwizePlugin;
import io.mapwize.mapwizeformapbox.api.Api;
import io.mapwize.mapwizeformapbox.api.ApiCallback;
import io.mapwize.mapwizeformapbox.api.SearchParams;
import io.mapwize.mapwizeformapbox.model.Direction;
import io.mapwize.mapwizeformapbox.model.LatLngFloor;
import io.mapwize.mapwizeformapbox.model.MapwizeObject;
import io.mapwize.mapwizeformapbox.model.Place;
import io.mapwize.mapwizeformapbox.model.Venue;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {
    //TODO Log into file (debug mode in preference)
    private String selectedArea = "";

    private int venue;

    private LocationClient locationClient;
    private GeofencingClient geofencingClient;

    private VisioglobeLocationFragment visioglobeLocationFragment;
    private VisioglobeSearchFragment visioglobeSearchFragment;

    private MapView mapView;
    private MapwizePlugin mapwizePlugin;
    private SupportMapFragment mapFragment = null;
    private ManualIndoorLocationProvider manualIndoorLocationProvider;
    private String venueId;
    private MapboxMap mapboxMapp;

    boolean isServiceStarted;

    private FusedLocationProviderClient mFusedLocationClient;

    private FloatingSearchView floatingSearchView;
    private Stack<Handler> handlerStack = new Stack<>();

    private Place lastClickedPlace;
    private boolean debugMode = false;
    private Location debugLocation;

    private Direction saveDirection;

    private WebFragment webFragment;
    private boolean isWebFragmentVisible = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        setVenue(sharedPref.getInt("Venue", R.id.colombesmap));
        Log.i("debugg", String.valueOf(getVenue()));
        manualIndoorLocationProvider = new ManualIndoorLocationProvider();
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.READ_CONTACTS}, 0);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.main_content).setElevation(5);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.app_bar_logo);
        Mapbox.getInstance(this, Keys.getMpbxApiKey());
        mapView = new MapView(this);
        floatingSearchView = (FloatingSearchView) findViewById(R.id.floating_search_view);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        autoLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MainApplication.activityPaused();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainApplication.activityResumed();
    }

    @Override
    protected void onDestroy() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        Log.i("debugg", "destroy" + getVenue());
        sharedPref.edit().putInt("Venue", getVenue()).apply();
        super.onDestroy();
    }

    //TODO Multisite API Key and test map provider instead of site?
    //TODO find a way to center on coordinate without testing venue, see changeMap method (base loading in GPS/default map)
    public void fetchLocationData(int venue) {
        if (venue == R.id.brestmapbox) {
            findViewById(R.id.floating_search_view).setVisibility(View.VISIBLE);
            locationClient = new LocationClient(this, Keys.getAppBrestKey());
            geofencingClient = new GeofencingClient(this, Keys.getAppBrestKey());
            //geofencingClient = new GeofencingClient(this, Keys.getAppBrestKey());
            startServices();
            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            LatLng patagonia = new LatLng(48.44159, -4.41268);
            MapboxMapOptions options = new MapboxMapOptions();
            options.camera(new CameraPosition.Builder().target(patagonia).zoom(18).build());
            if (mapFragment == null) {
                mapFragment = SupportMapFragment.newInstance(options);
                transaction.replace(R.id.main_content, mapFragment, "com.mapbox.map");
                transaction.commit();
                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(MapboxMap mapboxMap) {
                        mapboxMapp = mapboxMap;
                        MapOptions options = new MapOptions.Builder().showUserPositionControl(true).build();
                        mapwizePlugin = new MapwizePlugin(mapView, mapboxMap, options);
                        configureMapbox(mapboxMap);
                    }
                });
            }
        }
        if (venue == R.id.ibmmap) {
            findViewById(R.id.floating_search_view).setVisibility(View.VISIBLE);
            locationClient = new LocationClient(this, Keys.getAppIbmKey());
            geofencingClient = new GeofencingClient(this, Keys.getAppIbmKey());
            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            LatLng patagonia = new LatLng(48.9063873, 2.262095);
            MapboxMapOptions options = new MapboxMapOptions();
            options.camera(new CameraPosition.Builder().target(patagonia).zoom(18).build());
            if (mapFragment == null) {
                mapFragment = SupportMapFragment.newInstance(options);
                transaction.replace(R.id.main_content, mapFragment, "com.mapbox.map");
                transaction.commit();
                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(MapboxMap mapboxMap) {
                        MapOptions options = new MapOptions.Builder().showUserPositionControl(true).build();
                        mapwizePlugin = new MapwizePlugin(mapView, mapboxMap, options);
                        configureMapbox(mapboxMap);
                    }
                });
            }

        }
        if (venue == R.id.buenosairesmap) {
            findViewById(R.id.floating_search_view).setVisibility(View.VISIBLE);
            locationClient = new LocationClient(this, Keys.getAppArgKey());
            geofencingClient = new GeofencingClient(this, Keys.getAppArgKey());
            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            LatLng patagonia = new LatLng(-34.526517, -58.470869);
            MapboxMapOptions options = new MapboxMapOptions();
            options.camera(new CameraPosition.Builder().target(patagonia).zoom(18).build());
            if (mapFragment == null) {
                mapFragment = SupportMapFragment.newInstance(options);
                transaction.replace(R.id.main_content, mapFragment, "com.mapbox.map");
                transaction.commit();
                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(MapboxMap mapboxMap) {
                        MapOptions options = new MapOptions.Builder().showUserPositionControl(true).build();
                        mapwizePlugin = new MapwizePlugin(mapView, mapboxMap, options);
                        configureMapbox(mapboxMap);
                    }
                });
            }

        }
        if (venue == R.id.brestmap) {
            mapFragment = null;
            findViewById(R.id.floating_search_view).setVisibility(GONE);
            findViewById(R.id.button_container).setVisibility(GONE);
            locationClient = new LocationClient(this, Keys.getAppBrestKey());
            geofencingClient = new GeofencingClient(this, Keys.getAppBrestKey());
            FragmentManager fragmentManager = getSupportFragmentManager();
            visioglobeLocationFragment = new VisioglobeLocationFragment();
            visioglobeLocationFragment.rootMemorize(this);
            visioglobeSearchFragment = new VisioglobeSearchFragment();
            visioglobeSearchFragment.rootMemorize(this);
            fragmentManager.beginTransaction().replace(R.id.main_content, visioglobeLocationFragment).commit();
        }
        if (venue == R.id.colombesmap) {
            mapFragment = null;
            findViewById(R.id.floating_search_view).setVisibility(GONE);
            findViewById(R.id.button_container).setVisibility(GONE);
            locationClient = new LocationClient(this, Keys.getAppColKey());
            geofencingClient = new GeofencingClient(this, Keys.getAppColKey());
            FragmentManager fragmentManager = getSupportFragmentManager();
            visioglobeLocationFragment = new VisioglobeLocationFragment();
            visioglobeLocationFragment.rootMemorize(this);
            visioglobeSearchFragment = new VisioglobeSearchFragment();
            visioglobeSearchFragment.rootMemorize(this);
            fragmentManager.beginTransaction().replace(R.id.main_content, visioglobeLocationFragment).commit();
        }
        if (venue == R.id.positionSimul) {
            //TODO just reload LocationClient with simulation key?
            findViewById(R.id.floating_search_view).setVisibility(View.VISIBLE);
            locationClient = new LocationClient(this, Keys.getAppSimulKey());
            geofencingClient = new GeofencingClient(this, Keys.getAppIbmKey());
            startServices();
            final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            LatLng patagonia = new LatLng(48.44159, -4.41268);
            MapboxMapOptions options = new MapboxMapOptions();
            options.camera(new CameraPosition.Builder().target(patagonia).zoom(18).build());
            if (mapFragment == null) {
                mapFragment = SupportMapFragment.newInstance(options);
                transaction.replace(R.id.main_content, mapFragment, "com.mapbox.map");
                transaction.commit();
                mapFragment.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(MapboxMap mapboxMap) {
                        MapOptions options = new MapOptions.Builder().showUserPositionControl(true).build();
                        mapwizePlugin = new MapwizePlugin(mapView, mapboxMap, options);
                        configureMapbox(mapboxMap);
                    }
                });
            }

        }
    }

    public void autoLocation() {

        class VenueLocation{
            private com.google.android.gms.maps.model.LatLng latLng;
            private int id;
            private double distanceToUser;

            public VenueLocation(double latitude, double longitude, int id, Location location){
                this.latLng = new com.google.android.gms.maps.model.LatLng(latitude,longitude);
                this.id = id;
                this.distanceToUser = SphericalUtil.computeDistanceBetween(new com.google.android.gms.maps.model.LatLng(location.getLatitude(),location.getLongitude()), this.latLng);
            }

            public int getId() {
                return id;
            }

            public double getDistanceToUser() {
                return distanceToUser;
            }
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location==null){
                    fetchLocationData(getVenue());
                    return;
                }
                ArrayList<VenueLocation> venues = new ArrayList<>();
                venues.add(new VenueLocation(48.44159,-4.41268, R.id.brestmapbox,location));
                venues.add(new VenueLocation(48.9065881, 2.2620319, R.id.ibmmap, location));
                venues.add(new VenueLocation(48.9339509, 2.2523098, R.id.colombesmap, location));
                venues.add(new VenueLocation(-34.5265248, -58.4709851, R.id.buenosairesmap, location));
                VenueLocation tampon = venues.get(0);
                for(VenueLocation object : venues){
                    if(object.getDistanceToUser()<tampon.getDistanceToUser()){
                        tampon = object;
                    }
                }
                setVenue(tampon.getId());
                fetchLocationData(getVenue());
            }
        });
    }

    //********************OMNIACCESS STELLAR LBS********************************
    //TODO ->GeofencingClient
    public void onFireNaoAlert(NaoAlert alert) {
        java.util.ArrayList<NAOAlertRule> lList = alert.getRules();
        if (lList != null) {
            if (!lList.isEmpty()) {
                NAOAlertRule lRule = lList.get(0);
                if (lRule != null) {
                    if (lRule.getType() == TALERTRULE.ENTERGEOFENCERULE) {
                        String lContent = alert.getContent();
                        if (lContent != null) {
                            if (!lContent.isEmpty()) {
                                if (lContent.startsWith("http")) {
                                    Log.i("Webview Geofence", "open: "+alert.getContent());
                                    Log.i("Webview","Activity Visible: " + MainApplication.isActivityVisible());
                                    if(MainApplication.isActivityVisible()) {
                                        isWebFragmentVisible = true;
                                        findViewById(R.id.button_container).setVisibility(GONE);
                                        findViewById(R.id.floating_search_view).setVisibility(GONE);
                                        webFragment = new WebFragment();
                                        webFragment.setUrl(alert.getContent());
                                        getFragmentManager().beginTransaction().add(R.id.main_content, webFragment, "webFragment").addToBackStack(null).commit();
                                        try {
                                            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                                        } catch (Exception e) {
                                            Log.e("Exception", e.getMessage());
                                        }
                                        getSupportActionBar().setDisplayShowHomeEnabled(true);
                                    }else{
                                        Intent launchActivity = new Intent(MainActivity.this, BrowserActivity.class);
                                        launchActivity.putExtra("url", lContent);
                                        startActivity(launchActivity);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void onEnterGeofence(int regionId, java.lang.String regionName) {

    }

    public void onExitGeofence(int regionId, java.lang.String regionName) {
    }

    public void onEnterSite(String name) {

    }

    public void onExitSite(String name) {

    }

    public void notifyUser(String msg) {
    }

    public void setLocation(Location location) {
        //TODO mapwize: convert altitude to floor
        Log.i("MainActivityLocation", "setLocation ?");
        Log.i("MainActivityLocation", String.valueOf(location.getAltitude()));
        if (debugMode == true) {
            location = debugLocation;
        }
        if (visioglobeLocationFragment != null) {
            visioglobeLocationFragment.updateLocation(location);
        }
        if (getVenue() == R.id.brestmapbox || getVenue() == R.id.ibmmap || getVenue() == R.id.buenosairesmap || getVenue() == R.id.positionSimul) {
            this.updateLocation(location);
        }
        //NAO track
        if (getVenue() == R.id.brestmapbox) {
            PeopleTracker.instance().recordLocation(Keys.getBrestSiteId(), getDeviceName(), location.getLongitude(), location.getLatitude(), location.getAltitude());
        } else if (getVenue() == R.id.colombesmap) {
            PeopleTracker.instance().recordLocation(Keys.getColsiteId(), getDeviceName(), location.getLongitude(), location.getLatitude(), location.getAltitude());
        }
    }

    private String getDeviceName() {
        String deviceName = null;
        try {
            android.bluetooth.BluetoothAdapter myDevice = android.bluetooth.BluetoothAdapter.getDefaultAdapter();
            deviceName = myDevice.getName();
            if (deviceName == null) {
                deviceName = android.os.Build.MODEL + "-" + Build.MANUFACTURER;
            }
        } catch (Exception exc) {
            // just to protect against app crash
        }
        return deviceName;
    }
    //********************OMNIACCESS STELLAR LBS********************************


    //********************VENUE SELECTION***************************************
    public int getVenue() {
        return venue;
    }

    public void setVenue(int venue) {
        this.venue = venue;
    }
    //********************VENUE SELECTION***************************************


    //******************************MENU****************************************
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    public void changeMap(MenuItem menuItem) {
        if (getVenue() != menuItem.getItemId()) {
            setVenue(menuItem.getItemId());
            if (mapFragment != null) {
                if (menuItem.getItemId() == R.id.brestmapbox) {
                    mapboxMapp.setLatLng(new LatLng(48.44159, -4.41268));
                }
                if (menuItem.getItemId() == R.id.ibmmap) {
                    mapboxMapp.setLatLng(new LatLng(48.9063873, 2.262095));
                }
                if (menuItem.getItemId() == R.id.buenosairesmap) {
                    mapboxMapp.setLatLng(new LatLng(-34.526517, -58.470869));
                }
            } else {
                fetchLocationData(menuItem.getItemId());
            }
        }
        fetchLocationData(menuItem.getItemId());
    }
    //******************************MENU****************************************

    //*****************************VISIOGLOBE***********************************
    public void setSelectedArea(String area) {
        selectedArea = area;
    }

    public String getSelectedArea() {
        return selectedArea;
    }

    public boolean isDisplayLocationActive() {
        return true;
    }

    public void displaySearchFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_content, visioglobeSearchFragment).commit();
    }
    //*****************************VISIOGLOBE***********************************

    //******************************MAPBOX**************************************
    public void updateLocation(Location location) {
        Log.i("MapboxLocation", "UpdateLocation: " + location.getLatitude() + " " + location.getLongitude());
        if (isDisplayLocationActive()) {
            manualIndoorLocationProvider.setIndoorLocation(new IndoorLocation("PoleStar", location.getLatitude(), location.getLongitude(), location.getAltitude(), System.currentTimeMillis()));
        }
    }

    public void configureMapbox(final MapboxMap mapboxMap) {
        findViewById(R.id.floating_search_view).setElevation(10);
        setupFloatingSearch();
        setupResultsList();
        this.mapwizePlugin.setLocationProvider(manualIndoorLocationProvider);
        this.mapwizePlugin.setOnVenueEnterListener(new MapwizePlugin.OnVenueEnterListener() {
            @Override
            public void onVenueEnter(Venue venue) {
                venueId = venue.getId();
                Log.i("Mapbox", "onVenueEnter: Id: " + venueId);
            }

            @Override
            public void willEnterInVenue(Venue venue) {

            }
        });
        this.mapwizePlugin.setOnPlaceClickListener(new MapwizePlugin.OnPlaceClickListener() {
            @Override
            public boolean onPlaceClick(Place place) {
                lastClickedPlace = place;
                findViewById(R.id.button_container).setVisibility(View.VISIBLE);
                return true;
            }
        });
    }
    //******************************MAPBOX**************************************

    //*********************************LOCATION*********************************
    public boolean startServices() {
        Log.i("MainActivityStart", "startServices");
        // init service
        if (locationClient != null && geofencingClient != null) {
            Log.i("MainActivityStart", "creating handles");
            locationClient.createHandle();
            geofencingClient.createHandle();
            if (locationClient.startService()) {
                Log.i("MainActivityStart", "startService ok");
                isServiceStarted = true;
                if (geofencingClient.startService()) {
                    return true;
                }
            }
        }
        return false;
    }
    //*********************************LOCATION*********************************

    //*******************************SEARCHVIEW*********************************
    private void setupFloatingSearch() {
        floatingSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {
                if (newQuery != "" && newQuery.length() > oldQuery.length()) {
                    floatingSearchView.showProgress();

                    final Handler handler = new Handler();
                    handlerStack.push(handler);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (handlerStack.size() > 0 && handlerStack.peek() == handler) {
                                SearchParams searchParams = new SearchParams.Builder().setQuery(newQuery).setVenueId(mapwizePlugin.getVenue().getId()).build();
                                Api.search(searchParams, new ApiCallback<List<MapwizeObject>>() {
                                    @Override
                                    public void onSuccess(List<MapwizeObject> mapwizeObjects) {
                                        Log.i("SearchSuccess", "Succes " + mapwizeObjects.toString());
                                        final List<SearchSuggestion> searchSuggestions = new ArrayList<SearchSuggestion>();
                                        for (MapwizeObject o : mapwizeObjects) {
                                            searchSuggestions.add(new MapwizeSuggestionWrapper(o));
                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                floatingSearchView.swapSuggestions(searchSuggestions);
                                                floatingSearchView.hideProgress();
                                            }
                                        });

                                    }

                                    @Override
                                    public void onFailure(Throwable throwable) {
                                    }
                                });
                            }
                        }
                    }).run();
                }
            }
        });

        floatingSearchView.setOnHomeActionClickListener(new FloatingSearchView.OnHomeActionClickListener() {
            @Override
            public void onHomeClicked() {
                Log.i("Search", "Home Clicked");
                floatingSearchView.clearSearchFocus();
                floatingSearchView.clearQuery();
            }
        });

        floatingSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
                Log.i("Search", searchSuggestion.getBody());
                floatingSearchView.clearQuery();
                floatingSearchView.clearSuggestions();
                Api.getPlaceWithName(searchSuggestion.getBody(), mapwizePlugin.getVenue(), new ApiCallback<Place>() {
                    @Override
                    public void onSuccess(Place place) {
                        Log.i("Search", "Place clicked: " + place.getName() + " " + place.getAlias());
                        mapwizePlugin.addMarker(place);
                        lastClickedPlace = place;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.button_container).setVisibility(View.VISIBLE);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.i("Search", "getPlace failed: " + throwable.getMessage());
                    }
                });
                floatingSearchView.clearQuery();
                floatingSearchView.clearSuggestions();
            }

            @Override
            public void onSearchAction(String currentQuery) {
                Log.i("Search", "Query: " + currentQuery);
            }
        });

        floatingSearchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                Log.i("Search", "OnFocus/clearQuery");
                floatingSearchView.clearQuery();
            }

            @Override
            public void onFocusCleared() {
                Log.i("Search", "OnFocusCleared");
            }
        });

        floatingSearchView.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(View suggestionView, ImageView leftIcon,
                                         TextView textView, SearchSuggestion item, int itemPosition) {
                MapwizeSuggestionWrapper searchObjectSuggestion = (MapwizeSuggestionWrapper) item;

                leftIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_history_black_24dp, null));
                Util.setIconColor(leftIcon, Color.BLUE);
                leftIcon.setAlpha(.36f);

                int index = searchObjectSuggestion.getBody().toUpperCase().indexOf(floatingSearchView.getQuery().toUpperCase());

                if (index != -1) {
                    String oldSequence = searchObjectSuggestion.getBody().substring(index, index + floatingSearchView.getQuery().length());
                    String text = searchObjectSuggestion.getBody();
                    text = text.replaceFirst(oldSequence, "<font color=\"" + "#C51586" + "\">" + oldSequence + "</font>");

                    textView.setText(Html.fromHtml(text));
                }

            }

        });
    }

    private void setupResultsList() {
        MapwizeSearchResultsListAdapter mapwizeSearchResultsListAdapter = new MapwizeSearchResultsListAdapter();
    }

    private void getDirection(Place place) {
        LatLngFloor from;
        try {
            from = new LatLngFloor(mapwizePlugin.getUserPosition().getLatitude(), mapwizePlugin.getUserPosition().getLongitude(), mapwizePlugin.getUserPosition().getFloor());
        } catch (Exception e) {
            from = new LatLngFloor(0, 0, 0.0);
        }
        Api.getDirection(from, place, true, new ApiCallback<Direction>() {
            @Override
            public void onSuccess(Direction direction) {
                mapwizePlugin.setDirection(direction);
                saveDirection = direction;
            }
            @Override
            public void onFailure(Throwable throwable) {
                Log.i("Direction", "failed: " + throwable.getMessage());
            }
        });

    }
    //*******************************SEARCHVIEW*********************************

    //*********************************BUTTONS**********************************
    public void startDirection(View v) {
        getDirection(lastClickedPlace);
        v.setVisibility(GONE);
        findViewById(R.id.buttonStop).setVisibility(View.VISIBLE);
        mapwizePlugin.removeMarkers();
    }

    public void stopDirection(View v) {
        mapwizePlugin.setDirection(null);
        saveDirection = null;
        v.setVisibility(GONE);
        findViewById(R.id.buttonStart).setVisibility(View.VISIBLE);
        findViewById(R.id.button_container).setVisibility(GONE);
    }

    public void showInfos(View v) {
        findViewById(R.id.button_container).setVisibility(GONE);
        findViewById(R.id.floating_search_view).setVisibility(GONE);
        webFragment = new WebFragment();
        try{
            Log.i("showInfos",lastClickedPlace.getData().getString("url"));
            webFragment.setUrl(lastClickedPlace.getData().getString("url"));
        }catch (Exception e){
            Log.i("showInfos","error: "+e.getMessage());
        }
        getFragmentManager().beginTransaction().add(R.id.main_content,webFragment,"webFragment").commit();
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch (Exception e){
            Log.e("Exception",e.getMessage());
        }        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        findViewById(R.id.floating_search_view).setVisibility(View.VISIBLE);
        getFragmentManager().beginTransaction().remove(webFragment).commit();
        try{
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }catch (Exception e){
            Log.e("Exception",e.getMessage());
        }
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        return true;
    }
    //*********************************BUTTONS***********************************

    //*********************************DEBUG*************************************
    public void toDebugActivity(MenuItem menuItem) {
        Intent intent = new Intent(MainActivity.this, DebugActivity.class);
        if (saveDirection != null) {
            intent.putExtra("Direction", (Parcelable) saveDirection);
        }
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        sharedPref.edit().putInt("Venue", getVenue()).apply();
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        debugMode = true;
        Log.i("DebugPlace", "onActivityResult");
        if (resultCode == RESULT_OK) {
            if (getVenue() == R.id.brestmapbox) {
                Api.getVenueWithAlias("alebrest_office", new ApiCallback<Venue>() {
                    @Override
                    public void onSuccess(Venue venue) {
                        Api.getPlaceWithName(data.getStringExtra("BrestMWZ"), venue, new ApiCallback<Place>() {
                            @Override
                            public void onSuccess(Place place) {
                                Location l = new Location("Debug");
                                l.setLatitude(place.getLatitudeMax());
                                l.setLongitude(place.getLongitudeMax());
                                l.setAltitude(place.getFloor());
                                debugLocation = l;
                                setLocation(l);

                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                Log.i("onActivityResult", "Getting Place failed: " + throwable.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.i("onActivityResult", "Getting Venue failed: " + throwable.getMessage());
                    }
                });
            }
            if (getVenue() == R.id.ibmmap) {
                Api.getVenueWithAlias("ibm", new ApiCallback<Venue>() {
                    @Override
                    public void onSuccess(Venue venue) {
                        Api.getPlaceWithName(data.getStringExtra("IBM"), venue, new ApiCallback<Place>() {
                            @Override
                            public void onSuccess(Place place) {
                                Location l = new Location("Debug");
                                l.setLatitude(place.getLatitudeMax());
                                l.setLongitude(place.getLongitudeMax());
                                l.setAltitude(place.getFloor());
                                debugLocation = l;
                                setLocation(l);
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                Log.i("onActivityResult", "Getting Place failed: " + throwable.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.i("onActivityResult", "Getting Venue failed: " + throwable.getMessage());
                    }
                });
            }
            if (getVenue() == R.id.buenosairesmap) {
                Api.getVenueWithAlias("alcatel_buenos_aires", new ApiCallback<Venue>() {
                    @Override
                    public void onSuccess(Venue venue) {
                        Api.getPlaceWithName(data.getStringExtra("Argentine"), venue, new ApiCallback<Place>() {
                            @Override
                            public void onSuccess(Place place) {
                                Location l = new Location("Debug");
                                l.setLatitude(place.getLatitudeMax());
                                l.setLongitude(place.getLongitudeMax());
                                l.setAltitude(place.getFloor());
                                debugLocation = l;
                                setLocation(l);
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                Log.i("onActivityResult", "Getting Place failed: " + throwable.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.i("onActivityResult", "Getting Venue failed: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    public void positioningSimulation(MenuItem menuItem) {
        setVenue(R.id.positionSimul);
        fetchLocationData(getVenue());
    }
    //*********************************DEBUG*************************************

    //*********************************INTENT************************************
    @Override
    protected void onNewIntent(Intent intent) {
        Toast.makeText(this,"onNewIntent",Toast.LENGTH_SHORT).show();
        Log.i("onNewIntent","onNewIntent");
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        String method;
        String whereToGo;
        if(uri != null){
            Log.i("onNewIntent","uri: "+uri.toString());
            method = uri.getQueryParameter("action");
            if(method != null){
                if(method.equalsIgnoreCase("wayfinding")){
                    whereToGo = uri.getQueryParameter("poi");
                    if(whereToGo != null){
                        if(visioglobeLocationFragment != null){
                            visioglobeLocationFragment.gotoPlaceId(whereToGo);
                            Log.i("onNewIntent","method: "+method);
                            Log.i("onNewIntent","whereToGo: "+whereToGo);
                        }
                    }
                }
            }
        } else {
            method = intent.getStringExtra("action");
            Log.i("onNewIntent","method: "+intent.getStringExtra("action"));
            if(method != null){
                if(method.equalsIgnoreCase("wayfinding")){
                    whereToGo = getIntent().getStringExtra("poi");
                    if(whereToGo != null){
                        if(visioglobeLocationFragment != null){
                            visioglobeLocationFragment.setDestination(whereToGo);
                            Log.i("onNewIntent","method: "+method);
                            Log.i("onNewIntent","whereToGo: "+whereToGo);
                        }
                    }
                }
            }
        }

    }
    //*********************************INTENT************************************


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(isWebFragmentVisible){
            try {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            } catch (Exception e) {
                Log.e("Exception", e.getMessage());
            }
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            isWebFragmentVisible = false;
        }
    }
}