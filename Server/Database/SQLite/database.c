/*
  _____        _        _                    
 |  __ \      | |      | |                   
 | |  | | __ _| |_ __ _| |__   __ _ ___  ___ 
 | |  | |/ _` | __/ _` | '_ \ / _` / __|/ _ \
 | |__| | (_| | || (_| | |_) | (_| \__ \  __/
 |_____/ \__,_|\__\__,_|_.__/ \__,_|___/\___|
                                             
                                             
  Autori:

	Valentino Bocchetti (matr. N86003405)

	Guida introduttiva: https://isolution.pro/it/t/sqlite/sqlite-c-cpp/sqlite-c-c
*/

/*

  SQLite routines:

  `sqlite3_open_v2` è una routine apre una connessione a un file di database SQLite e restituisce un oggetto di connessione al database che deve essere utilizzato da altre routine SQLite.

  `sqlite3_exec` fornisce un modo semplice e veloce per eseguire i comandi SQL forniti dall'argomento sql che può essere costituito da più di un comando SQL 

  Questa routine tuttavia andrebbe evitata (è un wrapper alle chiamate presentate di seguito) in favore della sequenza di routines:
  1. sqlite3_prepare_v2() -> necessario per eseguire uno SQL statement
  2. sqlite3_step() -> Evaluate the statement
  3. sqlite3_column_*() -> Ritorna informazioni su una singola colonna della riga corrente del risultato della query
  4. sqlite3_finalize() -> Ha il compito di eliminare il prepared statement non più necessario

*/

#include <unistd.h>
#include <stdio.h>
#include <stdlib.h> // Necessario per la macro EXIT_FAILURE
#include <string.h>

#include <sys/socket.h> // Internet Protocol family 

#include <sqlite3.h>

#include "database.h"

#include "../../Utils/utils.h"


#define DATABASE_NAME "HoleScanner.db"

#define BUF_MAX_SIZE 26 // Nickname;latitudine;longitudine\n

// Per una lista completa dei possibili `status code` -> https://sqlite.org/rescode.html
int checkDBstatus(int expr, const char *msg) {
  if (expr == SQLITE_ERROR || expr == SQLITE_BUSY) {
    perror(msg);
    exit(EXIT_FAILURE);
  }
  return expr;
}

void infoDatabaseOperation(char *message){
  printf(ANSI_COLOR_YELLOW "%s" ANSI_COLOR_RESET, message);
}

void databaseInit(){
   sqlite3 *database;
   int sqlDbHandler;

   sqlite3_stmt *sqlPreparedStatement;

   // Definizione della tabella che conterrà i dati ricevuti dal client
   const char *table = "CREATE TABLE IF NOT EXISTS HoleScanner(NickName VARCHAR(25) NOT NULL, Latitudine DOUBLE NOT NULL, Longitudine DOUBLE NOT NULL);";

   // Apertura e controllo dello stato database
   checkDBstatus((sqlDbHandler = sqlite3_open_v2(DATABASE_NAME, &database, SQLITE_OPEN_READWRITE | SQLITE_OPEN_CREATE| SQLITE_OPEN_NOMUTEX, NULL)), "[-] Errore nell'apertura del database pre-init");
   //printf("[+] Apertura del database avvenuta con successo\n");
   infoDatabaseOperation("[+] Apertura del database avvenuta con successo\n");

   // Preparazione dello statement pre-inserimento
   checkDBstatus((sqlDbHandler = sqlite3_prepare_v2(database, table, strlen(table), &sqlPreparedStatement, NULL)),
					    "[-] Errore nella costruzione del prepared statement pre-creazione tabella\n");
   //printf("[+] Preparazione dello statement pre-creazione tabella avvenuto con successo\n");

   infoDatabaseOperation("[+] Preparazione dello statement pre-creazione tabella avvenuto con successo\n");

   checkDBstatus(sqlite3_step(sqlPreparedStatement), "[-] Errore nell'esecuzione dello statement per la creazione della tabella\n");

   //printf("[+] Operazione di creazione della tabella avvenuta con successo\n");
   infoDatabaseOperation("[+] Operazione di creazione della tabella avvenuta con successo\n");

   checkDBstatus((sqlDbHandler = sqlite3_finalize(sqlPreparedStatement)), "[-] Errore nella distruzione del prepared statement post-creazione tabella\n");


   // Chiusura del database
   sqlite3_close(database);
   //printf("[+] Chiusura del database avvenuta con successo in seguito alla creazione della tabella\n");
   infoDatabaseOperation("[+] Chiusura del database avvenuta con successo in seguito alla creazione della tabella\n");
}

void insertData(char *insertion){

  sqlite3 *database;
  int sqlDbHandler;
  sqlite3_stmt *sqlPreparedStatement;

   // Definizione dell' operazione di inserimento da eseguire sulla tabella
   //const char *insertion = "INSERT INTO HoleScanner(NickName, Latitudine, Longitudine) VALUES ('Valentino', 20000.00, 45000.00)";

   // Apertura e controllo dello stato database
   checkDBstatus((sqlDbHandler = sqlite3_open_v2(DATABASE_NAME, &database,
					       SQLITE_OPEN_READWRITE | SQLITE_OPEN_CREATE | SQLITE_OPEN_NOMUTEX, NULL)), "[-] Errore nell'apertura del database pre-inserimento dati\n");
   
   //printf("[+] Apertura del database avvenuta con successo\n");
   infoDatabaseOperation("[+] Apertura del database avvenuta con successo\n");

   // Preparazione dello statement pre-inserimento
   checkDBstatus((sqlDbHandler = sqlite3_prepare_v2(database, insertion, strlen(insertion), &sqlPreparedStatement, NULL)),
					    "[-] Errore nella costruzione del prepared statement pre-inserimento dati\n");

   //printf("[+] Preparazione dello statement pre-inserimento avvenuto con successo\n");
   infoDatabaseOperation("[+] Preparazione dello statement pre-inserimento avvenuto con successo\n");

   // Esecuzione dello statement
   checkDBstatus(sqlite3_step(sqlPreparedStatement), "[-] Errore nell'esecuzione dello statement per l'inserimento dei dati\n");

   //printf("[+] Operazione di inserimento avvenuta con successo\n");
   infoDatabaseOperation("[+] Operazione di inserimento avvenuta con successo\n");

   // Eliminazione del prepared statement non più necessario
   checkDBstatus((sqlDbHandler = sqlite3_finalize(sqlPreparedStatement)), "[-] Errore nella distruzione del prepared statement post-inserimento dati\n");

   // Chiusura del database
   sqlite3_close(database);

   //printf("[+] Chiusura del database in seguito alla selezione dei dati avvenuta con successo\n");
   infoDatabaseOperation("[+] Chiusura del database in seguito alla selezione dei dati avvenuta con successo\n");
}

void selectData(){
  char *nickname, *latitudine, *longitudine; // parametri utilizzati per il salvataggio dei dati delle varie colonne della Tabella

  sqlite3 *database;
  int sqlDbHandler;

  sqlite3_stmt *sqlPreparedStatement;

  const char *tableSelection = "SELECT * FROM HoleScanner";

  // Apertura del database
  // A differenza delle altree operazioni la SELECT ci permette di aprire il database in `READONLY` mode (non dobbiamo modificare infatti i record)
  checkDBstatus((sqlDbHandler = sqlite3_open_v2(DATABASE_NAME, &database, SQLITE_OPEN_READONLY | SQLITE_OPEN_NOMUTEX, NULL)), "[-] Errore nell'apertura del database pre-selezione dati\n");
  printf("[+] Apertura del database per la selezione dei dati avvenuta con successo\n");

  // Esecuzione della SELECT
  printf("[+] Esecuzione della SELECT nella tabella\n");
  
  checkDBstatus((sqlDbHandler = sqlite3_prepare_v2(database, tableSelection, strlen(tableSelection), &sqlPreparedStatement, NULL)),
							"[-] Errore nella costruzione del prepared statement pre-selezione dati\n");
  

  // Stampa dei campi contenuti nella tabella (atm su stdout, utilizzato per controllare)
  while (sqlite3_step(sqlPreparedStatement) == SQLITE_ROW) {
    nickname = (char *)sqlite3_column_text(sqlPreparedStatement, 0);

    // NB: È possibile anche associarlo a un double
    latitudine = (char *)sqlite3_column_text(sqlPreparedStatement, 1);
    longitudine = (char *)sqlite3_column_text(sqlPreparedStatement, 2);

    printf("Nickname: %s\tLatitudine: %s\tLongitudine: %s\n", nickname, latitudine, longitudine);
  }

  checkDBstatus((sqlDbHandler = sqlite3_finalize(sqlPreparedStatement)), "[-] Errore nella distruzione del prepared statement post-selezione dati\n");

  // Chiusura del database
  sqlite3_close(database);
  printf("[+] Chiusura del database in seguito alla selezione dei dati avvenuta con successo\n");
}

void selectDataWithRange(char *query){
  char *nickname, *latitudine, *longitudine; // parametri utilizzati per il salvataggio dei dati delle varie colonne della Tabella

  sqlite3 *database;
  int sqlDbHandler;

  sqlite3_stmt *sqlPreparedStatement;

  // Apertura del database
  // A differenza delle altree operazioni la SELECT ci permette di aprire il database in `READONLY` mode (non dobbiamo modificare infatti i record)
  checkDBstatus((sqlDbHandler = sqlite3_open_v2(DATABASE_NAME, &database, SQLITE_OPEN_READONLY | SQLITE_OPEN_NOMUTEX, NULL)), "[-] Errore nell'apertura del database pre-selezione dati\n");

  //printf("[+] Apertura del database per la selezione dei dati con range avvenuta  con successo\n");
  infoDatabaseOperation("[+] Apertura del database per la selezione dei dati con range avvenuta  con successo\n");

  // Esecuzione della SELECT
  //printf("[+] Esecuzione della SELECT con parametri nella tabella\n");
  infoDatabaseOperation("[+] Esecuzione della SELECT con parametri nella tabella\n");


  checkDBstatus((sqlDbHandler = sqlite3_prepare_v2(database, query, strlen(query), &sqlPreparedStatement, NULL)),
							"[-] Errore nella costruzione del prepared statement pre-selezione con parametri dati\n");
  
  // Stampa dei campi contenuti nella tabella (atm su stdout, utilizzato per controllare)
  while (sqlite3_step(sqlPreparedStatement) == SQLITE_ROW) {
    nickname = (char *)sqlite3_column_text(sqlPreparedStatement, 0);

    // NB: È possibile anche associarlo a un double
    latitudine = (char *)sqlite3_column_text(sqlPreparedStatement, 1);
    longitudine = (char *)sqlite3_column_text(sqlPreparedStatement, 2);

    printf("Nickname: %s\tLatitudine: %s\tLongitudine: %s\n", nickname, latitudine, longitudine);
    // Strutturiamo la stringa da inviare al client nel seguente modo:
    // printf("%s;%s;%s", nickname, latitudine, longitudine);
  }

  checkDBstatus((sqlDbHandler = sqlite3_finalize(sqlPreparedStatement)), "[-] Errore nella distruzione del prepared statement post-selezione dati\n");

  // Chiusura del database
  sqlite3_close(database);

  //printf("[+] Chiusura del database in seguito alla selezione dei dati avvenuta con successo\n");
  infoDatabaseOperation("[+] Chiusura del database in seguito alla selezione dei dati avvenuta con successo\n");
}

void sendDataAfterSelection(char *query, int clientSocket){
  char *nickname, *latitudine, *longitudine; // parametri utilizzati per il salvataggio dei dati delle varie colonne della Tabella

  char buffer[50];


  sqlite3 *database;
  int sqlDbHandler;

  sqlite3_stmt *sqlPreparedStatement;

  // Apertura del database
  // A differenza delle altree operazioni la SELECT ci permette di aprire il database in `READONLY` mode (non dobbiamo modificare infatti i record)
  checkDBstatus((sqlDbHandler = sqlite3_open_v2(DATABASE_NAME, &database, SQLITE_OPEN_READONLY | SQLITE_OPEN_NOMUTEX, NULL)), "[-] Errore nell'apertura del database pre-selezione dati\n");

  //printf("[+] Apertura del database per la selezione dei dati con range avvenuta  con successo\n");
  infoDatabaseOperation("[+] Apertura del database per la selezione dei dati con range avvenuta  con successo\n");

  // Esecuzione della SELECT
  //printf("[+] Esecuzione della SELECT con parametri nella tabella\n");
  infoDatabaseOperation("[+] Esecuzione della SELECT con parametri nella tabella\n");

  checkDBstatus((sqlDbHandler = sqlite3_prepare_v2(database, query, strlen(query), &sqlPreparedStatement, NULL)),
							"[-] Errore nella costruzione del prepared statement pre-selezione con parametri dati\n");
  
  while (sqlite3_step(sqlPreparedStatement) == SQLITE_ROW){
    nickname = (char *)sqlite3_column_text(sqlPreparedStatement, 0);

    // NB: È possibile anche associarlo a un double
    latitudine = (char *)sqlite3_column_text(sqlPreparedStatement, 1);
    longitudine = (char *)sqlite3_column_text(sqlPreparedStatement, 2);

    bzero(buffer, 50);

    snprintf(buffer, 50, "%s;%s;%s;\n",nickname, latitudine,longitudine);
    //printf("SOnO QUI: %s\n", buffer);
    printf("\t[*] Dati da inviare: %s\n", buffer);

    send(clientSocket, buffer, 50, 0);
    //write(clientSocket, buffer, 50);

      //write(sockfd, &buff, sizeof(buff));
    bzero(buffer, 50);
  }

  // Inviamo infine una stringa di terminazione al client come conferma
  send(clientSocket, "Terminato\n", 10, 0);

  checkDBstatus((sqlDbHandler = sqlite3_finalize(sqlPreparedStatement)), "[-] Errore nella distruzione del prepared statement post-selezione dati\n");

  // Chiusura del database
  sqlite3_close(database);

  //printf("[+] Chiusura del database in seguito alla selezione dei dati avvenuta con successo\n");
  infoDatabaseOperation("[+] Chiusura del database in seguito alla selezione dei dati avvenuta con successo\n");
}


/*
  Test delle funzioni di sul database
int main() {
  databaseInit();
  insertData("INSERT INTO HoleScanner(NickName, Latitudine, Longitudine) VALUES ('Valentino', 20000.00, 45000.00)");
  selectData();
  selectDataWithRange("SELECT * FROM HoleScanner WHERE (Latitudine BETWEEN 20000.00 AND 30000.00) AND (Longitudine BETWEEN 40000.00 AND 50000.00);");
}
*/
