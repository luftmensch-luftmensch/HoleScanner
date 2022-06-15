/*
  Questo file sorgente presenta tutte le funzioni legacy non più utilizzate ma mantenute come reference e le funzioni ausiliare necessarie ad esse

*/
#include <stdio.h>
#include <stdlib.h> // Necessario per la macro EXIT_FAILURE
#include <string.h>

#include "./headers/sqlite3.h" // Workaround per sqlite3.h (da sostituire in produzione con #`#include <sqlite3.h>`)

#include "database.h"

#define DATABASE_NAME "HoleScanner.db"

const char* data = "Chiamata alla funzione di callback";

static int callback(void *data, int argc, char **argv, char **azColName); // Funzione ausialiaria di callback per l'esecuzione di query con SQLite
void oldInsertData(); // Funzione legacy per l'inserimento dei dati all'interno del database (Mantenuta come reference)
void oldSelectData(); // Funzione legacy per la selezione dei dati (Mantenuta come reference)

static int callback(void *unused, int count, char **data, char **columns) {
    int idx;

    printf("Sono presenti %d colonne(s)\n", count);

    for (idx = 0; idx < count; idx++) {
        printf("Il dato nella colonna \"%s\" è: %s\n", columns[idx], data[idx]);
    }

    printf("\n");

    return 0;
}


// Per una lista completa dei possibili `status code` -> https://sqlite.org/rescode.html
int checkDBstatus(int expr, const char *msg) {
  if (expr == SQLITE_ERROR || expr == SQLITE_BUSY) {
    perror(msg);
    exit(EXIT_FAILURE);
  }
  return expr;
}

void oldDatabaseInit(){
   sqlite3 *database;
   char *errorMessage = 0;
   int sqlDbHandler;

   // Definizione della tabella che conterrà i dati ricevuti dal client
   const char *table = "CREATE TABLE IF NOT EXISTS HoleScanner(NickName VARCHAR(25) NOT NULL, Latitudine DOUBLE NOT NULL, Longitudine DOUBLE NOT NULL);";

   // Apertura e controllo dello stato database
   checkDBstatus((sqlDbHandler = sqlite3_open_v2(DATABASE_NAME, &database, SQLITE_OPEN_READWRITE | SQLITE_OPEN_CREATE| SQLITE_OPEN_NOMUTEX, NULL)), "[-] Errore nell'apertura del database pre-init");
   printf("[+] Apertura del database avvenuta con successo\n");


   // Creazione della tabella che conterrà i record ricevuti dai client
   checkDBstatus((sqlDbHandler = sqlite3_exec(database, table, callback, 0 , &errorMessage)), "[-]Errore nella creazione della tabella");
   printf("[+] Creazione della tabella avvenuta con successo\n");

   // Chiusura del database
   sqlite3_close(database);
   printf("[+] Chiusura del database avvenuta con successo in seguito alla creazione della tabella\n");
}

void oldInsertData() {

  sqlite3 *database;
  int sqlDbHandler;

   // Definizione dell' operazione di inserimento da eseguire sulla tabella
   const char *insertion = "INSERT INTO HoleScanner(NickName, Latitudine, Longitudine) VALUES ('Valentino', 20000.00, 20000.00)";

   // Apertura e controllo dello stato database
   checkDBstatus((sqlDbHandler = sqlite3_open_v2(DATABASE_NAME, &database,
					       SQLITE_OPEN_READWRITE | SQLITE_OPEN_CREATE | SQLITE_OPEN_NOMUTEX, NULL)), "[-] Errore nell'apertura del database pre-inserimento dati\n");
   printf("[+] Apertura del database avvenuta con successo\n");

   // Inserimento di valori nel database
   //sqlDbHandler = sqlite3_exec(database, insertion, callback, 0, &errorMessage);
   checkDBstatus((sqlDbHandler = sqlite3_exec(database, insertion, callback, NULL, NULL)), "[-]Errore nell'inserimento dei dati nella tabella");
   printf("[+] Inserimento nella tabella avvenuto con successo\n");

   // Chiusura del database
   sqlite3_close(database);
   printf("[+] Chiusura del database avvenuta con successo in seguito all' inserimento dei dati nella tabella\n");
}

// Selezione dei dati presenti nella tabella - Stampa su stdout (Da eliminare in seguito)
void oldSelectData(){

   sqlite3 *database;
   char *errorMessage = 0;
   int sqlDbHandler;
   const char *tableSelection = "SELECT * FROM HoleScanner";


   // Apertura del database
   // A differenza delle altree operazioni la SELECT ci permette di aprire il database in `READONLY` mode (non dobbiamo modificare infatti i record)
   checkDBstatus((sqlDbHandler = sqlite3_open_v2(DATABASE_NAME, &database, SQLITE_OPEN_READONLY | SQLITE_OPEN_NOMUTEX, NULL)), "[-] Errore nell'apertura del database pre-selezione dati\n");
   printf("[+] Apertura del database avvenuta con successo\n");

   // Esecuzione della SELECT
   printf("[+] Esecuzione della SELECT nella tabella\n");
   checkDBstatus((sqlDbHandler = sqlite3_exec(database, tableSelection, callback, (void*)data, &errorMessage)), "[-]Errore nella selezione dei dati nella tabella\n");


   // Chiusura del database
   sqlite3_close(database);
   printf("[+] Chiusura del database in seguito alla selezione dei dati avvenuta con successo\n");
}
