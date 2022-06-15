/*

 Scritto da Valentino Bocchetti e Mario Gabriele Carofano
 Copyright (c) 2022. All rights reserved.

*/
package com.holescanner.utils.persistence;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class LocalUserDbManager {
    private LocalUserDbHelper localUserDbHelper;
    private Context context;
    private SQLiteDatabase sqLiteDatabase;

    public LocalUserDbManager(Context context){
        this.context = context;
    }

    // Funzioni ausiliarie per la gestione del db

    // Settaggio e lettura dei dati in write mode
    public void openW() throws SQLException{
        localUserDbHelper = new LocalUserDbHelper(context);
        sqLiteDatabase = localUserDbHelper.getWritableDatabase();
    }


    // Lettura delle informazioni da un datavase
    public void openR() throws SQLException {
        localUserDbHelper = new LocalUserDbHelper(context);
        sqLiteDatabase = localUserDbHelper.getReadableDatabase();
    }

    public void closeDB(){
        sqLiteDatabase.close();
    }

    public void insertDatiUtente(LocalUser user){
        ContentValues contentValues = new ContentValues();
        contentValues.put(LocalUserDbHelper._ID, user.getId());
        contentValues.put(LocalUserDbHelper.USERNAME, user.getUsername());
        sqLiteDatabase.insert(LocalUserDbHelper.TABLE_NAME, null, contentValues);
    }

    public LocalUser fetchDataUser(){
        String[] proiezione = {
                LocalUserDbHelper._ID,
                LocalUserDbHelper.USERNAME,
        };

        Cursor cursor = sqLiteDatabase.query(LocalUserDbHelper.TABLE_NAME, proiezione, null,
                null, null, null,
                null, null );
        LocalUser localUser = new LocalUser();
        while(cursor.moveToNext()){
            localUser.setId(cursor.getInt(cursor.getColumnIndexOrThrow(LocalUserDbHelper._ID)));
            localUser.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(LocalUserDbHelper.USERNAME)));
        }
        cursor.close(); // This `Cursor` should be freed up after use with `#close()`
        return localUser;
    }

    public void delete(long id){
        sqLiteDatabase.delete(LocalUserDbHelper.TABLE_NAME, LocalUserDbHelper._ID + "=" + id, null);
    }

    public boolean isEmpty(){
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + LocalUserDbHelper.TABLE_NAME, null);
        if((cursor != null) && (cursor.getCount() > 0 )){
            cursor.close();
            return false;
        }
        return true;
    }
}