package com.andevcon.app.util;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class LocationFinder implements LocationListener {

    private UserLocation userLocation;

    private LocationManager locationManager;

    private String provider;

    public LocationFinder(Context context, UserLocation userLocation, String provider) {
        this.userLocation = userLocation;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.provider = provider;
    }

    public void onProviderDisabled(String provider) {
        userLocation.failedToFindLocation(provider);
        stop();

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (status == 0) {
            userLocation.failedToFindLocation(provider);
            stop();
        }
    }

    public void onLocationChanged(Location location) {
        userLocation.didFindLocation(location);
        stop();
    }

    private void stop() {
        locationManager.removeUpdates(this);
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    public void start() {
        locationManager.requestLocationUpdates(provider, 0, 0, this);
    }

}
