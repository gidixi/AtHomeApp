package com.gds.athomeapp.Helpers;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Handler;

public class LocationService extends Service {

    private LocationTracker locationTracker;
    private DatabaseHelper databaseHelper;

    private static final int NOTIFICATION_ID = 1;


    private Handler handler = new Handler();

    private Runnable saveLocationTask = new Runnable() {
        private final int maxAttempts = 5;
        private int currentAttempt = 0;

        @Override
        public void run() {
            if (locationTracker.isLocationAvailable()) {
                Location currentLocation = locationTracker.getLocation();
                if (currentLocation != null) {
                    Location lastLocation = databaseHelper.getLastLocation();

                    if (lastLocation == null || currentLocation.distanceTo(lastLocation) > 50) {
                        databaseHelper.saveLocation(currentLocation.getLatitude(), currentLocation.getLongitude());
                        handler.postDelayed(this, 900000); // 15 minuti in millisecondi
                    } else {
                        handler.postDelayed(this, 300000); // 5 minuti in millisecondi
                    }
                    currentAttempt = 0; // Resetta il contatore
                } else {
                    if (currentAttempt < maxAttempts) {
                        currentAttempt++;
                        handler.postDelayed(this, 1000); // Riprova dopo 1 secondo
                    } else {
                        currentAttempt = 0; // Resetta il contatore
                        handler.postDelayed(this, 300000); // 5 minuti in millisecondi
                    }
                }
            } else {
                handler.postDelayed(this, 300000); // 5 minuti in millisecondi
            }
        }
    };



    @Override
    public void onCreate() {
        super.onCreate();
        locationTracker = new LocationTracker(this);
        databaseHelper = new DatabaseHelper(this);
        handler.post(saveLocationTask);
        startForeground(NOTIFICATION_ID, getMyActivityNotification(""));
    }



    private Notification getMyActivityNotification(String text){
        String channelId = "location_service_channel_id";
        String channelName = "Location Service Channel";

        // Crea il canale di notifica per Android 8.0 e versioni successive
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        // Crea e configura un oggetto Notification.Builder per la notifica
        Notification.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, channelId)
                    .setContentTitle("Localizzazione in Esecuzione")
                    .setContentText(text)
                    .setSmallIcon(android.R.drawable.ic_menu_mylocation) // Sostituisci con la tua icona
                    .setOngoing(true);
        }

        // Per Android 7.1 e versioni precedenti, usa il costruttore senza channelId
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this)
                    .setContentTitle("Localizzazione in Esecuzione")
                    .setContentText(text)
                    .setSmallIcon(android.R.drawable.ic_menu_mylocation) // Sostituisci con la tua icona
                    .setOngoing(true);
        }

        return builder.build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(saveLocationTask);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
