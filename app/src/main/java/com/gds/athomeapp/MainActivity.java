package com.gds.athomeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.view.View;
import android.widget.Button;
import android.Manifest;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import com.gds.athomeapp.Helpers.DatabaseHelper;
import com.gds.athomeapp.Helpers.LocationService;
import com.gds.athomeapp.Helpers.LocationTracker;
import com.gds.athomeapp.Helpers.mySharedPreferences;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private LocationTracker locationTracker;

    private DatabaseHelper databaseHelper;

    private mySharedPreferences myPreferenceFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationTracker = new LocationTracker(this);
        requestLocationPermission(); // Chiamata alla funzione per richiedere i permessi
        showLastLocation();

        myPreferenceFile = new mySharedPreferences(this);

        /**
         * Pulsante per avere l'ultima posizione
         */
        final Button btnGetLocation = findViewById(R.id.btn_getGps);
        btnGetLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (locationTracker != null && locationTracker.isLocationAvailable()) {
                    Location currentLocation = locationTracker.getLocation();
                    if (currentLocation != null) {
                        double latitude = currentLocation.getLatitude();
                        double longitude = currentLocation.getLongitude();

                        //databaseHelper.saveLocation(latitude,longitude);
                        // Qui puoi fare qualcosa con le coordinate, come mostrarle
                        Toast.makeText(MainActivity.this, "Latitudine: " + latitude + ", Longitudine: " + longitude, Toast.LENGTH_LONG).show();
                    } else {
                        // Gestisci il caso in cui currentLocation è null
                        Toast.makeText(MainActivity.this, "Posizione non disponibile", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Gestisci il caso in cui i permessi non sono stati concessi o la posizione non è disponibile
                    Toast.makeText(MainActivity.this, "Permessi non concessi o posizione non disponibile", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /**
         * Switch per attivare/disattivare il servizio
         */

        ToggleButton toggleButton = findViewById(R.id.toggle_service);

        boolean isServiceRunning =  myPreferenceFile.getServiceRunning();
        toggleButton.setChecked(isServiceRunning);

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent serviceIntent = new Intent(MainActivity.this, LocationService.class);
                if (isChecked) {
                    startService(serviceIntent);
                    myPreferenceFile.putServiceRunning(true);
                    Toast.makeText(MainActivity.this, "Servizio di localizzazione avviato", Toast.LENGTH_SHORT).show();
                } else {
                    stopService(serviceIntent);
                    myPreferenceFile.putServiceRunning(false);
                    Toast.makeText(MainActivity.this, "Servizio di localizzazione fermato", Toast.LENGTH_SHORT).show();
                }
            }
        });


        /**
         *  GoogleMaps
         */

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    /**
     * Metodo per recuperare dal db l'ultima
     * posizione salvata
     */
    private void showLastLocation() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        Location lastLocation = databaseHelper.getLastLocation();

        TextView tvLastLocation = findViewById(R.id.tv_last_location);
        if (lastLocation != null) {
            String text = "Ultima Posizione:\nLatitudine: " + lastLocation.getLatitude() +
                    "\nLongitudine: " + lastLocation.getLongitude() +
                    // Aggiungiamo la visualizzazione del timestamp
                    "\nData e Ora: " + lastLocation.getExtras().getString("TIMESTAMP");
            tvLastLocation.setText(text);
        } else {
            tvLastLocation.setText("Ultima Posizione: nessuna");
        }
    }



//    private void checkLocationUpdates() {
//        // Crea un nuovo Handler
//        final Handler handler = new Handler();
//        // Esegui il seguente Runnable ogni X secondi
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                if (locationTracker.isLocationAvailable()) {
//                    Location currentLocation = locationTracker.getLocation();
//                    double latitude = currentLocation.getLatitude();
//                    double longitude = currentLocation.getLongitude();
//                    // Fai qualcosa con le coordinate
//                } else {
//                    // La posizione non è ancora disponibile, riprova dopo un intervallo
//                    handler.postDelayed(this, 5000); // Riprova dopo 5 secondi
//                }
//            }
//        }, 5000); // Inizialmente aspetta 5 secondi
//    }

    /**
     * Gestione permessi
     */

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Spiegazione aggiuntiva se necessario
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Mostra una spiegazione all'utente *asincronamente*
            } else {
                // Nessuna spiegazione necessaria; richiedi il permesso
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION);
            }
        }
    }

    /**
     * Gestione permessi
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permesso concesso
                    //locationTracker = new LocationTracker(this); // Assicurati che locationTracker sia istanziato qui
                    //checkLocationUpdates(); // Chiama il metodo per iniziare a controllare gli aggiornamenti sulla posizione
                } else {
                    // Permesso negato
                    // Gestisci il caso in cui il permesso non è stato concesso
                    Toast.makeText(MainActivity.this, "Permessi non concessi", Toast.LENGTH_SHORT).show();
                    requestLocationPermission();
                }
                return;
            }
        }
    }

    /**
     * Legge l'ultima posizione dal db aggiunge il marker sulla mappa
     * @param googleMap
     */

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        Location lastLocation = databaseHelper.getLastLocation();

        if (lastLocation != null) {
            double latitude = lastLocation.getLatitude();
            double longitude = lastLocation.getLongitude();

            // Aggiungi il marker sulla mappa
            LatLng posizione = new LatLng(latitude, longitude);
            googleMap.addMarker(new MarkerOptions().position(posizione).title("Posizione Attuale"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(posizione, 15)); // 15 qui è il livello di zoom

        } else {
            // Gestisci il caso in cui la posizione è null
            // Potresti voler visualizzare un messaggio all'utente o posizionare la mappa in una posizione predefinita
        }

    }
}