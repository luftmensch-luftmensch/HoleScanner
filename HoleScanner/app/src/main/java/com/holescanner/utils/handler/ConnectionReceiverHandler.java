/*

 Scritto da Valentino Bocchetti e Mario Gabriele Carofano
 Copyright (c) 2022. All rights reserved.

*/
package com.holescanner.utils.handler;

import android.util.Log;

import com.holescanner.utils.constants.Constants;
import com.holescanner.utils.constants.ElencoEndPoint;

import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ConnectionReceiverHandler {
    private static final String ConnectionReceiverHandler_TAG = "ConnectionHandler";

    public static ArrayList<GeoPoint> getPotHolesWithRanges(String query){
        ArrayList<GeoPoint> result  = new ArrayList<>();
        try{

            // Invio della query al server
            Log.d(ConnectionReceiverHandler_TAG, "Creazione dell'indirizzo server per il retrieval dei dati presenti sul Server");
            InetAddress serverAddr = InetAddress.getByName(Constants.SERVER_ADDR);

            // Un timeout di circa 30 secondi è più che sufficiente in base ai nostri test (locali e non)
            if(serverAddr.isReachable(2000)){
                Log.d(ConnectionReceiverHandler_TAG, "Il server è raggiungibile! Inizializzo la socket");
                Log.d(ConnectionReceiverHandler_TAG, "Creazione socket per il retrieval dei dati presenti sul Server");
                Socket socket = new Socket(serverAddr, Constants.selectPort(ElencoEndPoint.RETRIEVAL_DATA_WITH_PARAMETERS_PORT));

                // Alternativa 1
                Log.d(ConnectionReceiverHandler_TAG, "Creazione e invio della query al server");
                PrintWriter out = new PrintWriter(new BufferedWriter( new OutputStreamWriter(socket.getOutputStream())), true);
                out.println(query);
                //out.close();

                Log.d(ConnectionReceiverHandler_TAG, "Chiusura del writer non più necessario");

                // Alternativa 2 (diretta con socket)
                // socket.getOutputStream().write(query.getBytes());


                // Lettura dei dati in seguito all'invio della query
                Log.d(ConnectionReceiverHandler_TAG, "Inizializzazione lettura dei dati che matchano la query precedentemente inviata al server");
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));


                String responseBuffer;
                while (true){
                    responseBuffer = reader.readLine();
                    responseBuffer = responseBuffer.replace("\u0000", "");
                    Log.d("Dati ricevuti", responseBuffer);

                    // Controlliamo che il dato ricevuto non sia vuoto o che corrisponda a "Terminato" (keyword utilizzata per avere la conferma che i dati siano terminati)
                    if (responseBuffer.isEmpty() || responseBuffer.equals("Terminato")){
                        Log.d(ConnectionReceiverHandler_TAG, "Terminati i dati da ricevere");
                        break;
                    }
                    String[] fields = responseBuffer.split(";");
                    Log.d(ConnectionReceiverHandler_TAG, "Aggiunta dei seguenti campi ottenuti " + Arrays.toString(fields) + " alla lista di Potholes");
                    result.add(new GeoPoint(Double.parseDouble(fields[1]), Double.parseDouble(fields[2])));
                }
                Log.d(ConnectionReceiverHandler_TAG, "Chiusura della socket");
                socket.close();
            } else {
                Log.d(ConnectionReceiverHandler_TAG, "Il server non è raggiungibile! Impossibile fare il retriaval dei dati");
            }
        }catch (IOException e){
            Log.d(ConnectionReceiverHandler_TAG, e.getLocalizedMessage());
        }
        return result;
    }
}