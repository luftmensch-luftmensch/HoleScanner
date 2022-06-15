/*

 Scritto da Valentino Bocchetti e Mario Gabriele Carofano
 Copyright (c) 2022. All rights reserved.

*/
package com.holescanner.utils.persistence;

import androidx.annotation.NonNull;
public class LocalUser {

    private int id;
    private String username;

    // Costruttori
    public LocalUser() { }
    public LocalUser(int id, String username) { this.id = id; this.username = username;  }

    // Getter
    public int getId() { return id; }
    public String getUsername() { return username; }

    // Setter
    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }

    // Per il logging
    @NonNull
    @Override
    public String toString() {
        return "LocalUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
}