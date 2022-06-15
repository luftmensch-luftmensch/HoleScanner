/*

  _____                            _     _    _                 _ _           
 |  __ \                          | |   | |  | |               | | |          
 | |__) |___  __ _ _   _  ___  ___| |_  | |__| | __ _ _ __   __| | | ___ _ __ 
 |  _  // _ \/ _` | | | |/ _ \/ __| __| |  __  |/ _` | '_ \ / _` | |/ _ \ '__|
 | | \ \  __/ (_| | |_| |  __/\__ \ |_  | |  | | (_| | | | | (_| | |  __/ |   
 |_|  \_\___|\__, |\__,_|\___||___/\__| |_|  |_|\__,_|_| |_|\__,_|_|\___|_|   
                | |                                                           
                |_|                                                           

 Ha il compito di gestire l'invio dei dati che matchano la richiesta effettuata precedentemente dal Client collegato

 Autori:

	Valentino Bocchetti (matr. N86003405)

*/

// Header file
#include <stdio.h> // Input & Output 
#include <string.h> // Manipolazione di array di caratteri (in C non offre un tipo stringa built-in!)
#include <stdlib.h> // Standard Library ( Informazioni sulle funzioni di allocazione/deallocazione di memoria )

#include <fcntl.h> // File control options

#include <sys/socket.h> // Internet Protocol family 
#include <sys/types.h>
#include <netinet/in.h> // Internet Address family 

#include <arpa/inet.h> // Definizione di operazioni Internet
#include <netdb.h> // Operazioni network dabatase

#include <unistd.h> // Per utilizzare close (implicit declaration not valid in C99)

#include <pthread.h> // Contiene dichiarazioni di funzione e mapping per le interfacce di threading (definisce una serie di costanti utilizzate da tali funzioni)

#include "Database/SQLite/database.h" // Header personale che contiene tutte le firme dei metodi ausiliari per la gestione del database
#include "Utils/utils.h" // Header personale che contiene le funzione di logging (backtrace) e di controllo dello stato di varie operazioni delle socket
#include "Utils/welcome_messages.h" // Header personale che contiene delle stringhe costanti per la stampa

// Definizione variabili globali

#define BUFSIZE 4096 // Una dimensione più che sufficiente per il tipo di dati che vengono ricevuti
#define PORT 9001

#define SOCKET_ERROR (-1) // Controllo del valore di ritorno delle funzioni per le socket che in caso di errore ritornano lo stesso valore
#define SERVER_BACKLOG 1000 // Definiamo una variabile che verrà utilizzata per definire il numero di client totali che possono collegarsi


// Definizione di abbreviazioni che servono a rendere più leggibile e compatto il codice
typedef struct sockaddr_in SA_IN;
typedef struct sockaddr SA;


// Definizione prototipi delle funzioni ausiliarie

// Gestione della connessione dei client
void* gestisciConnessione(void* p_clientSocket);

int main(int argc, char *argv[]) {
  int serverSocket, clientSocket, addressLen;
  //int opt = 1;
  SA_IN serverAddr, clientAddr;

  // Messaggio di informazione
  printf(ANSI_COLOR_BMAGENTA "%s\n" ANSI_COLOR_RESET , ascii_request_handler_message);

  databaseInit();
  //int addressLen = sizeof(serverAddr);

  // Alternativa al metodo utilizzato facendo uso di sockaddr_storage e socklen_t direttamente
  //int addrLen = sizeof(serverAddr);
  //struct sockaddr_storage serverStorage;
  //socklen_t addr_size;

  /*---- Accept call creates a new socket for the incoming connection ----*/
  //addr_size = sizeof(serverStorage);
  //clientSocket = accept(serverSocket, (struct sockaddr *) &serverStorage, &addr_size);

  /*
    Creazione della socket. Accetta 3 argomenti:

    1. Dominio Internet;
    2. Socket stream;
    3. Protocollo di default (In questo caso usiamo TCP)

  */

  // Per brevità utilizzo la funzione ausiliaria `check` al posto di un if statement

  // `SOCKET`
  check((serverSocket = socket(AF_INET, SOCK_STREAM, 0)), "[-]Errore nella creazione della socket");

  logging("[+] Server socket inizializzata correttamente");

  // Forcefully attaching socket to the port 8080
  //if (setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR | SO_REUSEPORT, &opt, sizeof(opt))) {
  //  perror("setsockopt");
  //  exit(EXIT_FAILURE);
  //}

  // Azzeramento della regione di memoria (alternativa a memset). NB Non deve ritornare un valore
  bzero(&serverAddr, sizeof(serverAddr));
  
  // Configurazione delle impostazioni della struct dell' indirizzo del server

  /*
    NB: 
    AF_INET = Address Format, Internet = IP Addresses

    PF_INET = Packet Format, Internet = IP, TCP/IP or UDP/IP

  */
  
  serverAddr.sin_family = AF_INET;

  /* Set port number, using htons function to use proper byte order */
  // Settiamo il numero di porta (definito in precedenza). Facciamo uso della funzione htons  per la conversione di una porta a una "network byte order"

  serverAddr.sin_port = htons(PORT);

  /*
    Per il momento andiamo a settare l'indirizzo IP a localhost (127.0.0.1), per ovvie ragioni va cambiato.

    Andremo a settarla successivamente a:
    serverAddr.sin_addr.s_addr = htonl(INADDR_ANY);
    (Di solito, il server sceglie solo la porta. Come indirizzo IP, sceglie INADDR_ANY, così accetta connessioni dirette a qualunque indirizzo, infatti uno stesso host può avere più indirizzi IP)
    

  */
  serverAddr.sin_addr.s_addr = htonl(INADDR_ANY);
  //serverAddr.sin_addr.s_addr = inet_addr("127.0.0.1");

  //memset(serverAddr.sin_zero, '\0', sizeof serverAddr.sin_zero);   // Alternativa a bzero

  // Assegnazione dell'indirizzo locale al socket

  /*
    NB:

    La bind di INADDR_ANY non genera un indirizzo IP random (mappa la socket a tutte le interfacce disponibili (che su un server per ovvi motivi è una cosa fortemete voluta), e non solo localhost)

  */
  
  // `BIND` (NB: Restituisce un nuovo descrittore in caso di successo, -1 altrimenti)
  check((bind(serverSocket, (SA *)&serverAddr, sizeof(serverAddr))), "[-]Errore sulla bind");
  logging("[+] Binding inizializzato correttamente");

  // `LISTEN`
  // (NB: Restituisce un nuovo descrittore in caso di successo, -1 altrimenti. Il secondo argomento specifica quante connessioni possono essere in attesa di essere accettate.)
  check((listen(serverSocket, SERVER_BACKLOG)), "[-]Errore sulla listen");
  info_logging(PORT, SERVER_BACKLOG);

  // Fondamentale chiudere la connessione prima di terminare il programma
  //close(serverSocket);


  // Loop infinito che permette al server di continuare a servire connessioni una volta terminata la precente
  for(;;){
    printf("[+] In attesa di connessioni...\n");

    addressLen = sizeof(SA_IN);

    // `ACCEPT` (NB: Restituisce un nuovo descrittore in caso di successo, -1 altrimenti)
    check((clientSocket = accept(serverSocket, (struct sockaddr *)&clientAddr, (socklen_t *)&addressLen )), "[-]Errore sulla accept");

    client_logging((char*)inet_ntoa((struct in_addr)clientAddr.sin_addr));

    // Definizione della thread pool necessaria alla gestione simultanea di X connessioni (andando a evitare situazioni di dead-lock e race condition)
    pthread_t thread_pool;

    int *pClient = malloc(sizeof(int));

    *pClient = clientSocket;

    // int pthread_create(pthread_t *tid, const pthread_attr_t *attr, void *(*start_func) (void *), void arg);
    if (pthread_create(&thread_pool, NULL, gestisciConnessione, pClient) < 0){
      perror("[-] Errore nella creazione di un thread\n");
    }
  }
  return 0;
}

// Visto che la pthred_create richiede un void pointer castiamo successivamente il pointer ad int (per risolvere il warning in compilazione)
void * gestisciConnessione(void* p_clientSocket){
  int clientSocket = *(int*)p_clientSocket;

  free(p_clientSocket); // Una volta definito il `clientSocket` non necessitiamo più della variabile `p_clientSocket`

  char buffer[BUFSIZE];
  size_t bytes_letti;
  int dimensioneMessaggio=0; // Definire un valore per inizializzare la variabile!
  

  // Variabili necessarie al trim del buffer facendo uso del carattere `;` come end
  char *find;
  size_t index;

  // Lettura del messaggio ricevuto da parte del client
  while((bytes_letti = read(clientSocket, buffer+dimensioneMessaggio, sizeof(buffer)-dimensioneMessaggio-1 )) > 0){
    dimensioneMessaggio += bytes_letti;
    if(dimensioneMessaggio > BUFSIZE-1 || buffer[dimensioneMessaggio-1] == '\n') break;
  }

  check(bytes_letti, "[-] Errore sulla recv");

  printf("\tRichiesta: %s\n", buffer);

  // La funzione `strchr` ricerca la prima occorrenza del  carattere `;` (unsigned char) nel buffer `buffer`
  find = strchr(buffer, ';');

  // Mi calcolo l'indice su cui la funzione `strchr` ha trovato la prima occorrenza
  index = (size_t)(find - buffer);

  //printf("INDEX: %lu", index);


  // Mi creo un contenitore sufficiente a contenere la query che il client ha inviato
  char query[index + 1];

  // Copio la parte che mi interessa del buffer all'interno del contenitore query secondo l'indice `index` calcolato precedentemente
  strncpy(query, buffer, index);
  query[index] = '\0';

  // TODO: Cambiare il tipo di ritorno della funzione
  sendDataAfterSelection(query, clientSocket);
  
  bzero(buffer, sizeof(buffer));
  bzero(query, sizeof(query));

  fflush(stdout);

  // Infine chiudiamo la connessione
  close(clientSocket);
  logging("[+] Connessione terminata");
  return NULL; // la funzione è `void`
}
