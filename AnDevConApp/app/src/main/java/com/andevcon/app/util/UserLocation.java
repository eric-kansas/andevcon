package com.andevcon.app.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

public class UserLocation {

    private static final String TAG = "UserLocation";
    private static final int NUMBER_OF_PROVIDERS = 2;


    public interface LocationListener {
        void onLocation(String loc);
        void onLocationFailed();
    }

    private LocationManager mLocationManager;
    private LocationFinder mGps;
    private LocationFinder mNetwork;
    private LocationListener listener;

    private int failedCount = 0;
    private boolean foundLocation = false;


    public UserLocation(Context context, LocationListener listener) {
        this.listener = listener;

        try {
            mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            if (mLocationManager.getProvider(LocationManager.GPS_PROVIDER) != null) {
                mGps = new LocationFinder(context, this, LocationManager.GPS_PROVIDER);
            }

            if (mLocationManager.getProvider(LocationManager.NETWORK_PROVIDER) != null) {
                mNetwork = new LocationFinder(context, this, LocationManager.NETWORK_PROVIDER);
            }

        } catch (SecurityException e) {

        }
    }

    private static String formatLocation(Location loc)
    {
        return "{ lat: " + loc.getLatitude() + ", lon: " + loc.getLongitude() + ", acc: " + loc.getAccuracy() +"}";
    }

    public void getLocation() {
        foundLocation = false;
        failedCount = 0;

        if (mNetwork != null) {
            mNetwork.start();
        }

        if (mGps != null) {
            mGps.start();
        }
    }

    public void didFindLocation(Location loc) {
        if (!foundLocation) {
            listener.onLocation(formatLocation(loc));
        }
        foundLocation = true;
    }

    public void failedToFindLocation(String provider) {
        Log.e(TAG, "Location can't be determined by " + provider);
        failedCount++;

        if (!foundLocation && failedCount >= NUMBER_OF_PROVIDERS) {
            listener.onLocationFailed();
        }
    }
}
