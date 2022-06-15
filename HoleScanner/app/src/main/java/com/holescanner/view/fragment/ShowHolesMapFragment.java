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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.holescanner.BuildConfig;
import com.holescanner.R;
import com.holescanner.utils.constants.Constants;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class ShowHolesMapFragment extends Fragment {
    private static final String ShowHolesMapFragment_TAG = "ShowHolesMapFragment ";

    private Context context;

    private AppBarLayout TopAppBar;
    private MaterialToolbar TopToolBar;

    // Utilizzato per contenere le coordinate delle buche ottenute dal Server
    private ArrayList<GeoPoint> listaGeopoint;

    // Gestione della mappa
    private MapView mappa;

    // Fine Location (da utilizzare nei setting della mappa invece del Geopoint (che per il momento setta la mappa sulle coordinate dell'Uni
    // Per un esempio dare un occhiata qui -> https://github.com/lexteo13/android-fused-location-provider-example/blob/master/app/src/main/java/com/ideeastudios/example/location/fused/MainActivity.java
    private FusedLocationProviderClient fusedLocationProviderClient;

    public ShowHolesMapFragment(ArrayList<GeoPoint> listaGeopoint) {
        this.listaGeopoint = listaGeopoint;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_show_holes_map, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);


	    /*
	        OSMDroid richiede la definizione di uno user agent in modo da poter utilizzare le mappe online
	        Dai log infatti viene specificato -> `OsmDroid: Please configure a relevant user agent; current value is: osmdroid`
	        Per risolvere questo problema setto lo user agent (soluzione presa -> https://github.com/k3b/APhotoManager/commit/af0975d51f68a1508fc0ee4a0e7b99fd8a82f60d)
	    */
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);

        getLocation();

        setUI(view);
    }

    private void setUI(View view){
        TopAppBar = requireActivity().findViewById(R.id.AppBarLayout);
        TopToolBar = (MaterialToolbar) TopAppBar.getChildAt(0);

        TopToolBar.setTitle(R.string.VisualizzaBucheMappa);
        TopToolBar.getMenu().clear();
        TopToolBar.setNavigationIcon(R.drawable.arrow_back_24dp);

        requireActivity().findViewById(R.id.FloatingActionButton).setVisibility(View.GONE);
        requireActivity().findViewById(R.id.RicercaTextInput_appBar).setVisibility(View.GONE);

        mappa = view.findViewById(R.id.mapview);
        mappa.setTileSource(TileSourceFactory.MAPNIK);
        mappa.setClickable(true);
        mappa.getController().setZoom(18.0);
        mappa.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.ALWAYS);
        mappa.setMultiTouchControls(true);
        TopToolBar.setNavigationOnClickListener(click -> requireActivity().getSupportFragmentManager()
                .popBackStackImmediate());
    }

    private void getLocation(){
        int checkFineLocation = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        int checkCoarseLocation = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);

        if((checkFineLocation == Constants.PERMISSION_GRANTED) && (checkCoarseLocation == Constants.PERMISSION_GRANTED)){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    loadMap(location.getLatitude(), location.getLongitude());
                } else {
                    Log.d(ShowHolesMapFragment_TAG, "Impossibile ottenere la posizione");
                    Toasty.info(context,
                            "Non è stato possibile ottenere la tua posizione\n" +
                                    "Controlla di avere abilitato i permessi e il GPS",
                            Toast.LENGTH_LONG, true).show();
                }
            });
        }

    }

    private void loadMap(Double latitudine, Double longitudine){
        mappa.getController().setCenter(new GeoPoint(latitudine, longitudine));
        new Thread(() -> {
            for (GeoPoint g: listaGeopoint){
                Log.d(ShowHolesMapFragment_TAG, "Lista geopoint " + listaGeopoint);
                Marker marker = new Marker(mappa);
                marker.setPosition(g);
                marker.setTitle(getResources().getString(R.string.Latitudine) + g.getLatitude() + "\n" +
                        getResources().getString(R.string.Longitudine) + g.getLongitude());
                mappa.getOverlays().add(marker);
            }
        }).start();
    }
}