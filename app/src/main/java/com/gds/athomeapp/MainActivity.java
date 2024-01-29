package com.gds.athomeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.Manifest;
import android.widget.TextView;
import android.widget.Toast;


import com.gds.athomeapp.Helpers.DatabaseHelper;
import com.gds.athomeapp.Helpers.LocationService;
import com.gds.athomeapp.Helpers.LocationTracker;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private LocationTracker locationTracker;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationTracker = new LocationTracker(this);
        requestLocationPermission(); // Chiamata alla funzione per richiedere i permessi

        showLastLocation();

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
         final Button btnStartService = findViewById(R.id.btn_startService);
         btnStartService.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent serviceIntent = new Intent(MainActivity.this, LocationService.class);
                 startService(serviceIntent);
                 Toast.makeText(MainActivity.this, "Servizio di localizzazione avviato", Toast.LENGTH_SHORT).show();
             }
         });

        Button btnStopService = findViewById(R.id.btn_stop_service);
        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent serviceIntent = new Intent(MainActivity.this, LocationService.class);
                stopService(serviceIntent);
                Toast.makeText(MainActivity.this, "Servizio di localizzazione fermato", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLastLocation() {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        Location lastLocation = databaseHelper.getLastLocation();

        TextView tvLastLocation = findViewById(R.id.tv_last_location);
        if (lastLocation != null) {
            String text = "Ultima Posizione:\nLatitudine: " + lastLocation.getLatitude() +
                    "\nLongitudine: " + lastLocation.getLongitude();
                    tvLastLocation.setText(text);
        } else {
            tvLastLocation.setText("Ultima Posizione: nessuna");
        }
    }


    private void checkLocationUpdates() {
        // Crea un nuovo Handler
        final Handler handler = new Handler();
        // Esegui il seguente Runnable ogni X secondi
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (locationTracker.isLocationAvailable()) {
                    Location currentLocation = locationTracker.getLocation();
                    double latitude = currentLocation.getLatitude();
                    double longitude = currentLocation.getLongitude();
                    // Fai qualcosa con le coordinate
                } else {
                    // La posizione non è ancora disponibile, riprova dopo un intervallo
                    handler.postDelayed(this, 5000); // Riprova dopo 5 secondi
                }
            }
        }, 5000); // Inizialmente aspetta 5 secondi
    }


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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permesso concesso
                    locationTracker = new LocationTracker(this); // Assicurati che locationTracker sia istanziato qui
                    checkLocationUpdates(); // Chiama il metodo per iniziare a controllare gli aggiornamenti sulla posizione
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

}