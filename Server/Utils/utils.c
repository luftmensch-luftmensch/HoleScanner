#include <stdio.h>
#include <time.h>
#include <stdlib.h> // Necessario per la macro EXIT_FAILURE
#include "utils.h"


// Ritorna la data e tempo corrente
static inline struct tm get_time() {
  time_t t = time(NULL);
  return *localtime(&t);
}

// Funzione per il logging con l'orario
void logging(char* message){
  //time_t orario_corrente;
  //time(&orario_corrente);
  ////printf("(%.24s) : %s", ctime(&orario_corrente), message);
  //printf("%s (%.24s)\n", message, ctime(&orario_corrente));
  struct tm time = get_time();
  printf(ANSI_COLOR_GREEN "%s " ANSI_COLOR_RESET ANSI_COLOR_BLUE TIME_FORMAT ANSI_COLOR_RESET "\n", message, TIME_FORMAT_ARGS(time));
}

void info_logging(int PORT, int MAX_CONNECTION) {
  struct tm time = get_time();
  printf(ANSI_COLOR_GREEN "[+] Server Socket in ascolto sulla porta"
	 ANSI_COLOR_CYAN " %d "
	 ANSI_COLOR_GREEN "con un numero massimo di "
	 ANSI_COLOR_CYAN " %d "
	 ANSI_COLOR_GREEN "connessioni"
	 TIME_FORMAT ANSI_COLOR_RESET "\n", PORT, MAX_CONNECTION, TIME_FORMAT_ARGS(time));
}

void client_logging(char* message){
  //time_t orario_corrente;
  //time(&orario_corrente);
  //printf("[+]Connesso con il client con IP: [%s] (%.24s)\n", message, ctime(&orario_corrente));

  struct tm time = get_time();
  printf( ANSI_COLOR_CYAN "[+]Connesso con il client con IP:[" ANSI_COLOR_BGREEN "%s" ANSI_COLOR_CYAN "]" ANSI_COLOR_RESET ANSI_COLOR_BLUE TIME_FORMAT ANSI_COLOR_RESET "\n", message, TIME_FORMAT_ARGS(time));
}

int check(int expr, const char *msg) {
  if (expr == SOCKET_ERROR) {
    perror(msg);
    exit(EXIT_FAILURE);
  }
  return expr;
}
