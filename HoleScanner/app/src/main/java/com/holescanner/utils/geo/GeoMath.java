/*

 Scritto da Valentino Bocchetti e Mario Gabriele Carofano
 Copyright (c) 2022. All rights reserved.

*/
package com.holescanner.utils.geo;

import org.osmdroid.util.GeoPoint;

// https://stackoverflow.com/questions/3695224/sqlite-getting-nearest-locations-with-latitude-and-longitude
public class GeoMath {
    /**
     * Calcola il punto finale da una data sorgente in un dato intervallo (metri)
     * e portamento (gradi). Questo metodo utilizza semplici equazioni geometriche per
     * calcola il punto finale.
     *
     * @param point Punto di origine
     * @param range Range in metri
     * @param bearing Rilevamento in gradi
     * @return End-point dal punto dato dato il range e il bearing
     */
    public static GeoPoint calculateDerivedPosition(GeoPoint point, double range, double bearing){
        double EarthRadius = 6371000; // m

        double initialLatitude = Math.toRadians(point.getLatitude());
        double initialLongitude = Math.toRadians(point.getLongitude());

        double angularDistance = range / EarthRadius;
        double trueCourse = Math.toRadians(bearing);


        double finalLatitude = Math.asin( Math.sin(initialLatitude) * Math.cos(angularDistance) + Math.cos(initialLatitude) * Math.sin(angularDistance) * Math.cos(trueCourse));

        double dfinalLongitude = Math.atan2( Math.sin(trueCourse) * Math.sin(angularDistance) * Math.cos(initialLatitude), Math.cos(angularDistance) - Math.sin(initialLatitude) * Math.sin(finalLatitude));

        double finalLongitude = ((initialLongitude + dfinalLongitude + Math.PI) % (Math.PI * 2)) - Math.PI;

        finalLatitude = Math.toDegrees(finalLatitude);
        finalLongitude = Math.toDegrees(finalLongitude);

        return new GeoPoint(finalLatitude, finalLongitude);
    }
}