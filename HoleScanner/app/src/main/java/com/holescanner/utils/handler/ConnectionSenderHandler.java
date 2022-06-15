/*

 Scritto da Valentino Bocchetti e Mario Gabriele Carofano
 Copyright (c) 2022. All rights reserved.

*/
package com.holescanner.utils.handler;

import android.util.Log;

import com.holescanner.utils.constants.Constants;
import com.holescanner.utils.constants.ElencoEndPoint;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class ConnectionSenderHandler implements Runnable {
    private static final String ConnectionSenderHandler_TAG = "ConnectionHandler";
    private String message;

    @Override
    public void run(){
        try{
            Log.d(ConnectionSenderHandler_TAG, "Creazione dell'indirizzo server per l'invio dei dati al Server");
            InetAddress serverAddr = InetAddress.getByName(Constants.SERVER_ADDR);

            // Un timeout di circa 30 secondi è più che sufficiente in base ai nostri test (locali e non)
            if(serverAddr.isReachable(3000)) {
                Log.d(ConnectionSenderHandler_TAG, "Il server è raggiungibile! Inizializzo la socket");

                Log.d(ConnectionSenderHandler_TAG, "Creazione socket per l'invio dei dati al Server");
                Socket socket = new Socket(serverAddr, Constants.selectPort(ElencoEndPoint.SEND_DATA_PORT));

                Log.d(ConnectionSenderHandler_TAG, "Invio dei dati al Server");
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                out.println(message);

                Log.d(ConnectionSenderHandler_TAG, "Chiusura del buffer utilizzato per l'invio dei dati al Server");
                out.close();

                Log.d(ConnectionSenderHandler_TAG, "Chiusura della socket");
                socket.close();
            } else {
                Log.d(ConnectionSenderHandler_TAG, "Il server è raggiungibile! Non è stato possibile inviare i dati");
            }

        }catch (IOException io){
            Log.d(ConnectionSenderHandler_TAG, "IOException: " + io.getLocalizedMessage());

        }

    }

    public ConnectionSenderHandler(String message) { this.message = message; }
}