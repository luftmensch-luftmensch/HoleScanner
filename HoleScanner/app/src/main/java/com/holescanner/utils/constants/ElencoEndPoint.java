/*

 Scritto da Valentino Bocchetti e Mario Gabriele Carofano
 Copyright (c) 2022. All rights reserved.

*/
package com.holescanner.utils.constants;

public enum ElencoEndPoint {
    SEND_DATA_PORT, // Rotta per l'invio dei dati al server
    RETRIEVAL_DATA_WITH_PARAMETERS_PORT, // Rotta per la richiesta di tutti i dati salvati sul db che matchano una specifica richiesta da parte del client
    RETRIEVAL_TOLLERANCE // Rotta per la richiesta del valore di tolleranza da usare per il calcolo delle buche
}