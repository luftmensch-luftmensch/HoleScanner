/*

 Scritto da Valentino Bocchetti e Mario Gabriele Carofano
 Copyright (c) 2022. All rights reserved.

*/
package com.holescanner.utils.handler;

import android.util.Log;

import com.holescanner.utils.constants.Constants;
import com.holescanner.utils.constants.ElencoEndPoint;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

public class ConnectionTolleranceHandler {
    public static final String ConnectionTolleranceHandler_TAG = "ConnectionTolleranceHandler ";
    public double getTolerance(){

        /*
            Settiamo un valore di default per la tolleranza
            Il valore di ritorno verrà poi confrontato in seguito per controllare che sia stato possibile ottenere una soglia del server
            Si preferisce definirne uno in modo da poter continuare a utilizzare il programma nonostante non sia stato possibile farne il retrieval dal Server
            Utilizziamo una Stringa in quanto le connessioni client/server via socket sono costituite da uno scambio di stringhe
            Il valore scelto è leggermente diverso da quello definito sul Server, in modo da rendere validi i rilevamenti ottenuti
        */
        String tollerance = "15.000000";
        try{
            Log.d(ConnectionTolleranceHandler_TAG, "Creazione dell'indirizzo server per il retrieval della tolleranza");
            InetAddress serverAddr = InetAddress.getByName(Constants.SERVER_ADDR); // Come test "127.0.0.1"

            // Un timeout di circa 30 secondi è più che sufficiente in base ai nostri test (locali e non)
            if(serverAddr.isReachable(9000)){
                Log.d(ConnectionTolleranceHandler_TAG, "Il server è raggiungibile! Inizializzo la socket");

                Log.d(ConnectionTolleranceHandler_TAG, "Creazione socket per il retrieval della tolleranza");

                Socket socket = new Socket(serverAddr, Constants.selectPort(ElencoEndPoint.RETRIEVAL_TOLLERANCE));

                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                Log.d(ConnectionTolleranceHandler_TAG, "Retrieval della tolleranza dal Server");
                tollerance = reader.readLine();
                tollerance = tollerance.replace("\u0000", "");

                Log.d(ConnectionTolleranceHandler_TAG, "Tolleranza: " + tollerance);

                Log.d(ConnectionTolleranceHandler_TAG, "Chiusura della socket");
                socket.close();
        } else {
                Log.d(ConnectionTolleranceHandler_TAG, "Il server non è raggiungibile! Utilizzo il valore di tolleranza di default");
            }
        }catch (IOException e){
            Log.d(ConnectionTolleranceHandler_TAG, e.getLocalizedMessage());
        }
        return Double.parseDouble(tollerance);
    }
}