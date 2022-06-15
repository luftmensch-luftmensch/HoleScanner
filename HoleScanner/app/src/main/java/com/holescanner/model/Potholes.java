/*

 Scritto da Valentino Bocchetti e Mario Gabriele Carofano
 Copyright (c) 2022. All rights reserved.

*/
package com.holescanner.model;

import androidx.annotation.NonNull;

import com.holescanner.utils.constants.Constants;

public class Potholes {
    private String nickname;
    private Double longitudine, latitudine;

    // Costruttore (utilizziamo quello con tutti gli argomenti per brevità -> costruiamo l'oggetto direttamente)
    public Potholes(String nickname, Double longitudine, Double latitudine) {
        this.nickname = nickname;
        this.longitudine = longitudine;
        this.latitudine = latitudine;
    }

    @NonNull
    @Override
    public String toString() {
        return "Potholes{" +
                "nickname='" + nickname + '\'' +
                ", longitudine=" + longitudine +
                ", latitudine=" + latitudine +
                '}';
    }

    public String toQuery(){
        return "INSERT INTO " + Constants.TABLE_NAME + " VALUES " +
                "('" + nickname + "', "
                + latitudine +
                ", " + longitudine + ");";
    }
}