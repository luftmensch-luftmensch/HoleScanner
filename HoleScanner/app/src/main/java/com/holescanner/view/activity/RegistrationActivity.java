/*

 Scritto da Valentino Bocchetti e Mario Gabriele Carofano
 Copyright (c) 2022. All rights reserved.

*/
package com.holescanner.view.activity;

// Componenti
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.button.MaterialButton;
import com.holescanner.R;
import com.holescanner.utils.persistence.LocalUser;
import com.holescanner.utils.persistence.LocalUserDbManager;

import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import es.dmoral.toasty.Toasty;

public class RegistrationActivity extends AppCompatActivity {
    private static final String RegistrationActivity_TAG = "RegistrationActivity";

    private ImageView LogoImageView;

    private MaterialButton ConfermaRegistrazioneButton;
    private EditText UsernameEditText;

    private LocalUserDbManager localUserDbManager;
    private LocalUser localUser;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        setUI();
    }

    private void setUI(){
        LogoImageView = findViewById(R.id.LogoImageView_login);
        ConfermaRegistrazioneButton = findViewById(R.id.ConfermaButton_login);

        // Utilizziamo EditText invece di TextInputLayout per la gestione dei dati inseriti dall'utente
        // -> evitiamo in questo modo di dover gestire dei NullPointerException
        UsernameEditText = findViewById(R.id.UsernameEditText_registration);

        LogoImageView.setOnClickListener(view -> {
            // TODO: Cambiare la stringa in modo che punti al sito di HoleScanner (ancora da deployare)
            String url = "https://natour2022.netlify.app/";
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        });

        ConfermaRegistrazioneButton.setOnClickListener(view -> {
            username = UsernameEditText.getText().toString();
            if(controlloCampi(username)){
                Log.d(RegistrationActivity_TAG, "Username dell'utente: " + username);
                Log.d(RegistrationActivity_TAG, "Passaggio alla HomePage");

                localUser = new LocalUser(1, username);
                setLocalUser(localUser);

                Toasty.success(getApplicationContext(), "Benvenuto " + username + "!", Toasty.LENGTH_SHORT, true).show();
                Intent intent = new Intent(RegistrationActivity.this, HomePageActivity.class);
                view.getContext().startActivity(intent);
            }

        });
    }

    // Metodo per la gestione dei campi inseriti dall'utente
    private boolean controlloCampi(String username){
        if((username.isEmpty())) {
            Toasty.warning(this, "Attenzione! È necessario settare uno username per continuare", Toasty.LENGTH_SHORT, true).show();
            return false;
        }

        if(username.length() > 8){
            Toasty.warning(this, "Attenzione! Lo username deve avere una lunghezza massima di 8 caratteri", Toasty.LENGTH_SHORT, true).show();
            return false;
        }

        if(!username.matches("^[a-zA-Z0-9]+$")){
            Toasty.warning(this, "Attenzione! Lo username deve contenere solo caratteri alfanumerici", Toasty.LENGTH_SHORT, true).show();
            return false;
        }
        return true;
    }

    private void setLocalUser(LocalUser localUser){
        localUserDbManager = new LocalUserDbManager(this.getApplicationContext());
        localUserDbManager.openW();

        Log.d(RegistrationActivity_TAG, "Inserimento dell'utente nel database locale");
        localUserDbManager.insertDatiUtente(localUser);
        Log.d(RegistrationActivity_TAG, "Inserimento dell'utente nel database locale completato");
        localUserDbManager.closeDB();
        Log.d(RegistrationActivity_TAG, "Chiusura del database database locale");
    }
}