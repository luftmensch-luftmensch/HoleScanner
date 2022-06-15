/*

 Scritto da Valentino Bocchetti e Mario Gabriele Carofano
 Copyright (c) 2022. All rights reserved.

*/
package com.holescanner.view.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.holescanner.R;
import com.holescanner.view.fragment.ReceiverFragment;
import com.holescanner.view.fragment.SenderFragment;

public class HomePageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        BottomNavigationView MenuInferiore = findViewById(R.id.MenuInferiore);

        MenuItem SenderButton = MenuInferiore.getMenu().findItem(R.id.SenderButton);
        MenuItem ReceiverButton = MenuInferiore.getMenu().findItem(R.id.ReceiverButton);

        SenderButton.setOnMenuItemClickListener(menuItem -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.FragmentContainer, new SenderFragment())
                    .commit();
            return false;
        });

        ReceiverButton.setOnMenuItemClickListener(menuItem -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.FragmentContainer, new ReceiverFragment())
                    .commit();
            return false;
        });

    }
}