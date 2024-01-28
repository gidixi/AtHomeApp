package com.gds.athomeapp.Helpers;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class LocationTracker implements LocationListener {

    private final Context context;
    private LocationManager locationManager;
    private Location lastLocation;
    private boolean isLocationAvailable = false;

    public LocationTracker(Context context) {
        this.context = context;
        initializeLocationManager();
    }

    private void initializeLocationManager() {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this);
        } catch (SecurityException ex) {
            Log.e("LocationTracker", "Errore di sicurezza: permessi non concessi.", ex);
            // Aggiungi qui ulteriori azioni per gestire l'assenza dei permessi
        } catch (IllegalArgumentException ex) {
            Log.e("LocationTracker", "Errore di argomento non valido.", ex);
            // Gestire altri tipi di eccezioni legate alla richiesta di aggiornamenti
        }
    }


    public boolean isLocationAvailable() {
        return isLocationAvailable;
    }

    public Location getLocation() {
        return isLocationAvailable ? lastLocation : null;
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        isLocationAvailable = true;
    }

    @Override
    public void onProviderDisabled(String provider) {
        isLocationAvailable = false;
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Implementa se necessario
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Implementa se necessario
    }
}