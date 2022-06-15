/*

 Scritto da Valentino Bocchetti e Mario Gabriele Carofano
 Copyright (c) 2022. All rights reserved.

*/
package com.holescanner.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.holescanner.R;
import com.holescanner.utils.persistence.LocalUserDbManager;

public class SplashScreenActivity extends  AppCompatActivity{
    private Handler handler;
    private Runnable runnable;

    // Db Manager
    private LocalUserDbManager localUserDbManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        handler = new Handler();
        runnable = () -> {
            /*
                1. Controllare che i permessi siano effettivamente attivi
                2. Controllare che ci sia la rete (e che ovviamente funzioni)
                    a. Nel caso in cui ci sia si passa al punto 3;
                    b. In caso di assenza della rete si fa visualizzare su schermo un popup di errore in cui si avvisa
                       l'utente che l'app non possa funzionare senza la rete.
                 3. Controllare che l'utente sia loggato
                    a. Se si, si passa alla homepage;
                    b. Altrimenti si passa alla Registrazione
            */
            boolean status = userStatus();

            Intent intent;
            if(status){
                /*
                 Nel caso in cui sia TRUE (il DB è vuoto) rimandiamo all'activity di registrazione,
                 in cui l'utente potrà definire un suo username
                 (che verrà poi utilizzato come campo all'interno della tabella delle segnalazioni delle buche)
                 */
                intent = new Intent(SplashScreenActivity.this, RegistrationActivity.class);
            } else {
                // In caso contrario invece alla Home page
                intent = new Intent(SplashScreenActivity.this, HomePageActivity.class);
            }
            startActivity(intent);
            finish();
        };
        handler.postDelayed(runnable, 1000);
    }

    private boolean userStatus(){
        localUserDbManager = new LocalUserDbManager(getApplicationContext());
        localUserDbManager.openR();
        boolean isEmpty = localUserDbManager.isEmpty();
        localUserDbManager.closeDB();
        Log.d("Stato del Database: ", String.valueOf(isEmpty));
        return isEmpty;
    }
}