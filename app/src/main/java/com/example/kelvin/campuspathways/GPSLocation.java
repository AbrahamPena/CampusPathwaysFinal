package com.example.kelvin.campuspathways;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by Abraham on 3/20/18.
 * The purpose of this class is to get the starting location.
 * This is done by using the LocationListener.
 * In this instance, we prioritize Accuracy over battery life.
 */

public class GPSLocation implements LocationListener {

    private Context context;
    private LocationManager locationManager;

    public GPSLocation(Context context) {
        super();
        this.context = context;
    }

    //This method gets the starting location of the app
    public Location getLocation(){
        if (ContextCompat.checkSelfPermission( context, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            Log.e("fist","error");
            return null;
        }
        try {
            locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
            boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (isGPSEnabled){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500,0,this);
                Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                return loc;
            }else{
                Log.e("sec","error");
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void turnOffGPS() {
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

}
