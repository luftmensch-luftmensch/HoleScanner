/*
  _______    _ _                                 _______ _                   _           _     _ 
 |__   __|  | | |                               |__   __| |                 | |         | |   | |
    | | ___ | | | ___ _ __ __ _ _ __   ___ ___     | |  | |__  _ __ ___  ___| |__   ___ | | __| |
    | |/ _ \| | |/ _ \ '__/ _` | '_ \ / __/ _ \    | |  | '_ \| '__/ _ \/ __| '_ \ / _ \| |/ _` |
    | | (_) | | |  __/ | | (_| | | | | (_|  __/    | |  | | | | | |  __/\__ \ | | | (_) | | (_| |
    |_|\___/|_|_|\___|_|  \__,_|_| |_|\___\___|    |_|  |_| |_|_|  \___||___/_| |_|\___/|_|\__,_|
                                                                                                 
  Ha il compito di inviare ai Client che si collegano il threshold definito per il calcolo delle buche
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

#include "Utils/utils.h" // Header personale che contiene le funzione di logging (backtrace) e di controllo dello stato di varie operazioni delle socket
#include "Utils/welcome_messages.h" // Header personale che contiene delle stringhe costanti per la stampa

// Definizione variabili globali

#define PORT 9002 // Porta sulla quale il server si collega al client per l'invio della porta

#define THRESHOLDS_PARAMETERS "17.000000\0" // Definizione dei parametri di soglia da comunicare al client

#define SOCKET_ERROR (-1) // Controllo del valore di ritorno delle funzioni
#define SERVER_BACKLOG 1000 // Definiamo una variabile che verrò utilizzata per definire il numero di client totali che possono collegarsi

typedef struct sockaddr_in SA_IN; // Per brevità all'interno delle funzioni
typedef struct sockaddr SA; // Per brevità all'interno delle funzioni

// Definizione prototipi funzioni ausiliarie

// Gestione della connessione dei client
void *invioDati(void* p_clientSocket);

int main(int argc, char *argv[]) {

  int serverSocket, clientSocket, addressLen;
  //int opt = 1;
  SA_IN serverAddr, clientAddr;
  //int addressLen = sizeof(serverAddr);

  // Messaggio di informazione
  printf(ANSI_COLOR_MAGENTA "%s\n" ANSI_COLOR_RESET, ascii_tollerance_threshold_message);

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
  //printf("[+] Server socket inizializzata correttamente\n");
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

  //memset(serverAddr.sin_zero, '\0', sizeof serverAddr.sin_zero);   // Alternativa a bzero

  // Assegnazione dell'indirizzo locale al socket

  /*
    NB:

    La bind di INADDR_ANY non genera un indirizzo IP random (mappa la socket a tutte le interfacce disponibili (che su un server per ovvi motivi è una cosa fortemete voluta), e non solo localhost)

  */
  
  // `BIND` (NB: Restituisce un nuovo descrittore in caso di successo, -1 altrimenti)
  check((bind(serverSocket, (SA *)&serverAddr, sizeof(serverAddr))), "[-]Errore sulla bind");
  //printf("[+] Binding inizializzato correttamente\n");
  logging("[+] Binding inizializzato correttamente");

  // `LISTEN`
  // (NB: Restituisce un nuovo descrittore in caso di successo, -1 altrimenti. Il secondo argomento specifica quante connessioni possono essere in attesa di essere accettate.)
  check((listen(serverSocket, SERVER_BACKLOG)), "[-]Errore sulla listen");
  
  info_logging(PORT, SERVER_BACKLOG);

  // Fondamentale chiudere la connessione prima di terminare il programma
  //close(serverSocket);


  // Loop infinito
  for(;;){
    printf("[+] In attesa di connessioni...\n");

    addressLen = sizeof(SA_IN);

    // `ACCEPT` (NB: Restituisce un nuovo descrittore in caso di successo, -1 altrimenti)
    check((clientSocket = accept(serverSocket, (struct sockaddr *)&clientAddr, (socklen_t *)&addressLen )), "[-]Errore sulla accept");
    client_logging((char*)inet_ntoa((struct in_addr)clientAddr.sin_addr));

    pthread_t thread_pool;

    int *pClient = malloc(sizeof(int));

    *pClient = clientSocket;

    // int pthread_create(pthread_t *tid, const pthread_attr_t *attr, void *(*start_func) (void *), void arg);
    pthread_create(&thread_pool, NULL, invioDati, pClient);
  }

  return 0;
}


// Funzioni ausiliaria

// Visto che la pthred_create richiede un void pointer castiamo successivamente il pointer ad int (per risolvere il warning in compilazione)
void *invioDati(void* p_clientSocket){
  int clientSocket = *(int*) p_clientSocket;

  free(p_clientSocket); // Una volta definito il `clientSocket` non necessitiamo più della variabile `p_clientSocket`

  char buffer[sizeof(THRESHOLDS_PARAMETERS)];
  strcpy(buffer, THRESHOLDS_PARAMETERS);
  send(clientSocket, buffer, sizeof(buffer), 0);
  bzero(buffer, sizeof(buffer));

  fflush(stdout); // Da cambiare!
  close(clientSocket);
  printf("[+] Connessione terminata\n");

  return NULL;
}
