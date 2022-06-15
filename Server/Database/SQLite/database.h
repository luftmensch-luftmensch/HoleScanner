#ifndef Hole_Scanner_Database
#define Hole_Scanner_Database

int checkDBstatus(int expr, const char *msg); // Funzione ausiliaria che ha il compito di controllare lo stato delle operazioni effettuate sul database
void databaseInit(); // Funzione di inizializzazione del database (Creazione del database e tabella nel caso in cui non fosse già stata creata)
void insertData(char *insertion); // Funzione di inserimento dei dati all'interno del database
void selectData(); // Funzione di selezione dei dati all'interno del database

void selectDataWithRange(char *query); // Funzione di selezione dei dati all'interno del database secondo un range prefissato
void sendDataAfterSelection(char *query, int clientSocket); // Funzione di selezione dei dati all'interno del database secondo un range prefissato ed invio dei dati ottenuti al Client che ha effettuato la richiesta
#endif
