/*

 Scritto da Valentino Bocchetti e Mario Gabriele Carofano
 Copyright (c) 2022. All rights reserved.

*/
package com.holescanner.view.fragment;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.google.android.material.button.MaterialButton;
import com.holescanner.BuildConfig;
import com.holescanner.R;
import com.holescanner.utils.constants.Constants;
import com.holescanner.utils.geo.GeoMath;
import com.holescanner.utils.handler.ConnectionReceiverHandler;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

// Questo fragment ha lo scopo di gestire la ricezione dei dati inviati dal Server in seguito ad
// una richiesta dell'utente
public class ReceiverFragment extends Fragment {
    public static final String ReceiverFragment_TAG = "ReceiverFragment";

    private Context context;

    private AppBarLayout TopAppBar;
    private MaterialToolbar TopToolBar;

    private ArrayList<GeoPoint> listaBuche;

    // Fine Location (da utilizzare nei setting della mappa invece del Geopoint (che per il momento setta la mappa sulle coordinate dell'Uni
    // Per un esempio dare un occhiata qui -> https://github.com/lexteo13/android-fused-location-provider-example/blob/master/app/src/main/java/com/ideeastudios/example/location/fused/MainActivity.java
    private FusedLocationProviderClient fusedLocationProviderClient;

    //private LocationManager locationManager;
    private ActivityResultLauncher<String> activityResultLauncherPermission;

    // Drop down menu per la scelta del raggio d'azione
    private AutoCompleteTextView dropDownTextView;
    private ArrayAdapter<String> dropDownAdapter;
    private static final String[] listaDistanze = {"1000 m", "2000 m", "5000 m"};
    private String selection = "";
    private MaterialButton AvviaRicercaButton;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_receiver, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUI();

	    /*
	        OSMDroid richiede la definizione di uno user agent in modo da poter utilizzare le mappe online
	        Dai log infatti viene specificato -> `OsmDroid: Please configure a relevant user agent; current value is: osmdroid`
	        Per risolvere questo problema setto lo user agent (soluzione presa -> https://github.com/k3b/APhotoManager/commit/af0975d51f68a1508fc0ee4a0e7b99fd8a82f60d)
	    */
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }

    private void setUI() {
        TopAppBar = requireActivity().findViewById(R.id.AppBarLayout);
        TopToolBar = (MaterialToolbar) TopAppBar.getChildAt(0);

        TopToolBar.setTitle(R.string.RicercaButton);
        TopToolBar.getMenu().clear();
        TopToolBar.setNavigationIcon(null);
        TopToolBar.inflateMenu(R.menu.homepage_topappbar_menu);

        // Gestione Drop down menu
        dropDownTextView = requireActivity().findViewById(R.id.ReceiverSelection);
        dropDownAdapter = new ArrayAdapter<>(requireActivity(), R.layout.drop_down_menu_options, listaDistanze);
        dropDownTextView.setAdapter(dropDownAdapter);
        AvviaRicercaButton = requireActivity().findViewById(R.id.AvviaRicercaButton);

        // Drop Down menu per la selezione delle distanze
        dropDownTextView.setOnItemClickListener((parent, view, position, id) -> selection = listaDistanze[position]);

        // Pulsante di conferma in seguito alla selezione effettuata dall'utente del dropdown menu
        AvviaRicercaButton.setOnClickListener(view ->{
            if (selection.isEmpty()){
                Log.d(ReceiverFragment_TAG, "Nessuna distanza selezionata");
                Toasty.info(context, "Nessuna distanza selezionata\nSelezionane una per avviare la ricerca", Toast.LENGTH_LONG, true).show();
            } else {
                Log.d(ReceiverFragment_TAG, "Il valore selezionato è " + selection);
                checkLocationPermission();
            }
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

    // Calcolo della coordinate che matchano la richiesta in base al radius
    private void generateQuery(GeoPoint centerPosition, Double radius) {
        GeoPoint p1 = GeoMath.calculateDerivedPosition(centerPosition, 1 * radius, 0);
        GeoPoint p2 = GeoMath.calculateDerivedPosition(centerPosition, 1 * radius, 90);
        GeoPoint p3 = GeoMath.calculateDerivedPosition(centerPosition, 1 * radius, 180);
        GeoPoint p4 = GeoMath.calculateDerivedPosition(centerPosition, 1 * radius, 270);
        String query = "SELECT * FROM " + Constants.TABLE_NAME + " WHERE " +
                "LATITUDINE > " + p3.getLatitude() + " AND " +
                "LATITUDINE < " + p1.getLatitude() + " AND " +
                "LONGITUDINE < " + p2.getLongitude() + " AND " +
                "LONGITUDINE > " + p4.getLongitude() + ";";


        //String query = "SELECT * FROM HoleScanner;"; // Da utilizzare come testing

        Log.d(ReceiverFragment_TAG, "QUERY " + query);
        retrievalPotholes(query);
    }

    // Funzione per il retrevial (tramite thread) delle buche presenti sul database che matchano la richiesta effettuata al server
    private void retrievalPotholes(String query) {
        Thread getPotHoles = new Thread(() -> {
            Log.d(ReceiverFragment_TAG, "Inizializzazione richiesta buche presenti sul Database");
            listaBuche = ConnectionReceiverHandler.getPotHolesWithRanges(query);
            Log.d(ReceiverFragment_TAG, "Buche: " + listaBuche);

            if (listaBuche.size() == 0) {
                Log.d(ReceiverFragment_TAG, "Non sono state trovate buche nella zona intorno al dispositivo");
                // Per mostrare un messaggio grafico di avviso è necessario utilizzare su un Thread UI
                requireActivity().runOnUiThread(() ->
                        Toasty.info(context, "Non sono state trovate buche nella zona intorno a te",
                                Toast.LENGTH_LONG, true).show());
            } else {
                passaggioAShowHolesMap(listaBuche);
            }
        });
        getPotHoles.setPriority(10);
        getPotHoles.start();
    }


    // Gestione della posizione corrente
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(context, "android.permission.ACCESS_FINE_LOCATION") == Constants.PERMISSION_DENIED) {
            Log.d(ReceiverFragment_TAG, "Permesso " + "android.permission.ACCESS_FINE_LOCATION" + " negato");
            activityResultLauncherPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        } else {
            // In caso in cui i permessi siano stati già forniti, utilizziamo il GPS per calcolare la posizione dell'utente
            getLocation();
        }
    }

    private void getLocation() {
        int checkFineLocation = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        int checkCoarseLocation = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);

        if ((checkFineLocation == Constants.PERMISSION_GRANTED) && (checkCoarseLocation == Constants.PERMISSION_GRANTED)) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    Log.d(ReceiverFragment_TAG, "Posizione: " + location.getLatitude() + " " + location.getLongitude());
                    generateQuery(new GeoPoint(location.getLatitude(), location.getLongitude()), Double.parseDouble(selection.substring(0,4)));
                } else {
                    Log.d(ReceiverFragment_TAG, "Non è stato possibile ottenere la posizione");
                    Toasty.info(context, "Non è stato possibile ottenere la tua posizione\n" + "Controlla di avere abilitato i permessi e il GPS",
                            Toast.LENGTH_SHORT, true).show();
                }
            });
        }
    }

    // Passaggio alla vista delle buche su mappa
    private void passaggioAShowHolesMap(ArrayList<GeoPoint> result) {
        Log.d(ReceiverFragment_TAG, "Lista Geopoint: " + result + " Size: " + result.size());

        Log.d(ReceiverFragment_TAG, "Passaggio alla mappa interattiva delle buche nelle vicinanze");
        ShowHolesMapFragment showHolesMapFragment = new ShowHolesMapFragment(result);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.FragmentContainer, showHolesMapFragment, "ShowHolesMapFragment")
                .addToBackStack(ReceiverFragment_TAG)
                .commit();

    }
}