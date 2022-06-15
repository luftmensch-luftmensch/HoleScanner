/*

 Scritto da Valentino Bocchetti e Mario Gabriele Carofano
 Copyright (c) 2022. All rights reserved.

*/
package com.holescanner.utils.constants;

public class Constants {
    public static final String SERVER_ADDR = "";
    public static final String TABLE_NAME = "HoleScanner";

    // Costanti per la gestione dei permessi dell'applicazione
    public static final int PERMISSION_GRANTED = 0;
    public static final int PERMISSION_DENIED = -1;

    public static int selectPort(ElencoEndPoint endPoint){
        switch (endPoint){
            case SEND_DATA_PORT:
                return 9000;
            case RETRIEVAL_DATA_WITH_PARAMETERS_PORT:
                return 9001;
            case RETRIEVAL_TOLLERANCE:
                return 9002;
            default:
                return 0;
        }
    }
}
