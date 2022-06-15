/*

 Scritto da Valentino Bocchetti e Mario Gabriele Carofano
 Copyright (c) 2022. All rights reserved.

*/
package com.holescanner.view.fragment;

import android.Manifest;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.holescanner.R;
import com.holescanner.utils.constants.Constants;
import com.holescanner.utils.handler.ConnectionSenderHandler;
import com.holescanner.utils.handler.ConnectionTolleranceHandler;
import com.holescanner.utils.persistence.LocalUser;
import com.holescanner.utils.persistence.LocalUserDbManager;

import com.holescanner.model.Potholes;

import es.dmoral.toasty.Toasty;

// Questo fragment ha lo scopo di gestire l'invio dei dati al Server in seguito alla ricezione di una buca
public class SenderFragment extends Fragment {
    public static final String SenderFragment_TAG = "SenderFragment";

    private Context context;
    private AppBarLayout TopAppBar;
    private MaterialToolbar TopToolBar;

    private BottomNavigationView MenuInferiore;
    private MaterialButton AvviaCalibrazioneButton, AvviaRegistrazioneButton, InterrompiRegistrazioneButton;

    private Chronometer cronometro;

    private TextView InformazioniTextView;

    private MenuItem logoutButton;

    // Gestione utente locale
    private LocalUserDbManager localUserDbManager;
    private LocalUser localUser;

    // Sensoristica
    private SensorManager sensorManager;
    private Sensor accellerometer;

    // Listener sui sensori per la calibrazione
    private SensorEventListener accellerometerEventListener, accellerometerCalibrationEventListener;

    private float[] calibrationValues = new float[3];

    // Il valore di tolleranza deve essere molto alto, onde evitare falsi positivi (ad es 17)
    private Double tollerance = 0.000000d;

    // Fine Location (da utilizzare nei setting della mappa invece del Geopoint (che per il momento setta la mappa sulle coordinate dell'Uni
    // Per un esempio dare un occhiata qui -> https://github.com/lexteo13/android-fused-location-provider-example/blob/master/app/src/main/java/com/ideeastudios/example/location/fused/MainActivity.java
    private FusedLocationProviderClient fusedLocationProviderClient;

    private ActivityResultLauncher<String> activityResultLauncherPermission;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sender, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        setUI();

        // Alla creazione del fragment richiediamo il valore di tolleranza settato dal server
        retrievalTolerance();
    }

    private void setUI() {
        TopAppBar = requireActivity().findViewById(R.id.AppBarLayout);
        TopToolBar = (MaterialToolbar) TopAppBar.getChildAt(0);

        TopToolBar.setTitle(R.string.HomeButton);
        TopToolBar.getMenu().clear();
        TopToolBar.setNavigationIcon(null);
        TopToolBar.inflateMenu(R.menu.homepage_topappbar_menu);
        logoutButton = TopToolBar.getMenu().findItem(R.id.LogoutButton);

        MenuInferiore = requireActivity().findViewById(R.id.MenuInferiore);
        MenuInferiore.getMenu().getItem(0).setChecked(true);

        requireActivity().findViewById(R.id.FloatingActionButton).setVisibility(View.GONE);
        requireActivity().findViewById(R.id.RicercaTextInput_appBar).setVisibility(View.GONE);

        // Definizione visibilità per i pulsanti di Calibrazione e Avvio Registrazione
        AvviaCalibrazioneButton = requireActivity().findViewById(R.id.AvviaCalibrazioneButton);
        AvviaRegistrazioneButton = requireActivity().findViewById(R.id.AvviaRegistrazioneButton);
        InterrompiRegistrazioneButton = requireActivity().findViewById(R.id.InterrompiRegistrazioneButton);

        AvviaRegistrazioneButton.setVisibility(View.GONE);
        InterrompiRegistrazioneButton.setVisibility(View.GONE);

        // Definizione visibilità per il cronometro (visualizzato solo durante la registrazione)
        cronometro = requireActivity().findViewById(R.id.Cronometro);
        cronometro.setVisibility(View.GONE);

        // Definizione del testo di informazione per l'utente durante le varie fasi
        InformazioniTextView = requireActivity().findViewById(R.id.SenderTextView);

        localUser = getDatiutente();

        // Sensoristica
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        accellerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);


        // Gestione avvio calibrazione sensore
        AvviaCalibrazioneButton.setOnClickListener(view -> {
            Toasty.success(context, "Inizializzazione calibrazione...", Toast.LENGTH_SHORT, true).show();

            initializeCalibration();
            Log.d(SenderFragment_TAG, calibrationValues[0] + " - " + calibrationValues[1] + " - " + calibrationValues[2]);
            setComponentsStatus("Calibrazione");
        });

        // Gestione avvio registrazione buche
        AvviaRegistrazioneButton.setOnClickListener(view ->{
            Toasty.success(context, "Registrazione avviata con successo!", Toast.LENGTH_SHORT, true).show();
            unregisterCalibration();
            checkLocationPermission();
            setComponentsStatus("Avvio registrazione");

            cronometro.setBase(SystemClock.elapsedRealtime());
            cronometro.start();
        });

        // Gestione interruzione registrazione buche
        InterrompiRegistrazioneButton.setOnClickListener(view ->{
            Toasty.success(context, "Interruzione registrazione avvenuta con successo!", Toast.LENGTH_SHORT, true).show();
            unregisterAccelerationSensor();
            cronometro.stop();
            cronometro.setVisibility(View.INVISIBLE);
            setComponentsStatus("Interruzione registrazione");
        });

        // Gestione logout
        logoutButton.setOnMenuItemClickListener(click -> {
            logout();
            return false;
        });

        // Gestione richiesta dei permessi
        activityResultLauncherPermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(), permesso ->{
            if (permesso) {
                getLocation();
            } else {
                Toasty.error(context, "Per poter accedere a questa funzione, HoleScanner ha bisogno della tua posizione", Toast.LENGTH_SHORT, true).show();
            }
        });

    }



    // Funzioni di gestione della posizione corrente
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(context, "android.permission.ACCESS_FINE_LOCATION") == Constants.PERMISSION_DENIED) {
            Log.d(SenderFragment_TAG, "Permesso " + "android.permission.ACCESS_FINE_LOCATION" + " negato");
            activityResultLauncherPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            // In caso in cui i permessi siano stati già forniti, utilizziamo il GPS per calcolare la posizione dell'utente
            accellerometerInitializer();
        }
    }
    private void getLocation(){
        int checkFineLocation = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        int checkCoarseLocation = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);

        if((checkFineLocation == Constants.PERMISSION_GRANTED) && (checkCoarseLocation == Constants.PERMISSION_GRANTED)){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    Log.d(SenderFragment_TAG, "Latitudine: " + location.getLatitude() +  " Longitudine: " + location.getLongitude());
                    sendDataToServer(localUser.getUsername(), location.getLatitude(), location.getLongitude());
                } else {
                    Toasty.info(context, "Non è stato possibile ottenere la tua posizione\n" + "Controlla di avere abilitato i permessi e il GPS",
                            Toast.LENGTH_SHORT, true).show();
                }
            });
        }
    }

    // Retrieval dati utente dal Database locale
    private LocalUser getDatiutente(){
        LocalUserDbManager localUserDbManager = new LocalUserDbManager(this.getContext());
        localUserDbManager.openR();
        LocalUser localUser = localUserDbManager.fetchDataUser();
        localUserDbManager.closeDB();
        return localUser;
    }

    private void logout(){
        localUserDbManager = new LocalUserDbManager(context);
        localUserDbManager.openW();
        localUser = localUserDbManager.fetchDataUser();
        localUserDbManager.delete(1);
        localUserDbManager.closeDB();

        requireActivity().onBackPressed();
    }

    // Invio dati al server
    private void sendDataToServer(String nickName, double Latitudine, double Longitudine){
        Log.d(SenderFragment_TAG, "Inizializzazione componenti da inviare al server");
        Potholes potholes = new Potholes(nickName, Latitudine, Longitudine);
        Log.d(SenderFragment_TAG, "Dati inviati al server: " + potholes);

        new Thread(new ConnectionSenderHandler(potholes.toQuery())).start();
    }

    // Funzioni relative ai sensori
    private void accellerometerInitializer(){
        accellerometerEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float yValue = event.values[1];
                //float xValue = event.values[0];
                //float zValue = event.values[2];

                // NB: Il THRESHOLD deve essere molto ampio
                if(Math.abs(yValue) > tollerance){
                    Log.d(SenderFragment_TAG, "THRESHOLD superato " + yValue);
                    Toasty.info(context, "Buca rilevata!\n Invio dei dati in corso...",
                            Toast.LENGTH_SHORT, true).show();
                    getLocation();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) { }
        };
        sensorManager.registerListener(accellerometerEventListener, accellerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void unregisterAccelerationSensor(){
        sensorManager.unregisterListener(accellerometerEventListener, accellerometer);
    }

    // Funzioni relative alla calibrazione del sensore
    private void initializeCalibration(){
        accellerometerCalibrationEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                calibrationValues[0] = event.values[0];
                calibrationValues[1] = event.values[1];
                calibrationValues[2] = event.values[2];
                Log.d(SenderFragment_TAG,"Valore dei sensori:" + event.values[0] + " " + event.values[1] + " " + event.values[2]);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) { }
        };

        // Controllo che il sensore sia valido
        if (accellerometer == null) {
            Log.d(SenderFragment_TAG, "Il sensore non è stato rilevato!");
            Toasty.error(context, "Attenzione!\nImpossibile rilevare il sensore", Toast.LENGTH_SHORT, true).show();
            return;
        }
        sensorManager.registerListener(accellerometerCalibrationEventListener, accellerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    // Una volta terminata la calibrazione ne eliminiamo la registrazione
    private void unregisterCalibration(){
        sensorManager.unregisterListener(accellerometerCalibrationEventListener, accellerometer);
    }

    // Funzioni per la gestione della visibilità degli elementi
    private void setComponentsStatus(String stato){
        switch (stato){
            case "Calibrazione":
                AvviaCalibrazioneButton.setVisibility(View.INVISIBLE);
                AvviaRegistrazioneButton.setVisibility(View.VISIBLE);
                InformazioniTextView.setText("Per iniziare la registrazione clicca\nil pulsante sottostante");
                break;
            case "Avvio registrazione":
                InformazioniTextView.setText("Per interrompere la registrazione\nclicca il pulsante sottostante");
                cronometro.setVisibility(View.VISIBLE);
                InterrompiRegistrazioneButton.setVisibility(View.VISIBLE);
                break;
            case "Interruzione registrazione":
                InterrompiRegistrazioneButton.setVisibility(View.INVISIBLE);
                AvviaRegistrazioneButton.setVisibility(View.INVISIBLE);
                AvviaCalibrazioneButton.setVisibility(View.VISIBLE);
                InformazioniTextView.setText("Per iniziare la calibrazione clicca\nil pulsante sottostante");
                break;
        }
    }

    // Funzione per ottenere il valore di tolleranza come parametro di confronto
    private void retrievalTolerance(){
        Thread getter = new Thread(() -> {
            Log.d(SenderFragment_TAG, "Inizializzazione per la richiesta della tolleranza al server");
            ConnectionTolleranceHandler connectionTolleranceHandler = new ConnectionTolleranceHandler();
            tollerance = connectionTolleranceHandler.getTolerance();
            Log.d(SenderFragment_TAG, "Tolleranza " + tollerance);

            if (tollerance == 0.000000){
                requireActivity().runOnUiThread(() ->
                        Toasty.info(context, "Non è stato possibile ricavare il valore di\ntolleranza. Verrà utilizzato quello di default",
                                Toast.LENGTH_LONG, true).show());
            }
        });
        getter.setPriority(10);
        getter.start();
    }
}