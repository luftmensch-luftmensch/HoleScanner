#ifndef Utils_Hole_Scanner
#define Utils_Hole_Scanner

#define SOCKET_ERROR (-1)
void logging(char* message);
void client_logging(char* message);
void info_logging(int PORT, int MAX_CONNECTION);

/*
  Gestore degli errori delle funzioni `socket`, `bind`, `listen`, `accept` e `read`
  (Sfruttiamo il fatto che tutti questi metodi abbiano come valore di ritorno in caso di errore `-1`).

  Per brevità utilizziamo questa funzione con lo scopo di evitare di scrivere
  molteplici `if statement` che controllino il `return value`
*/
int check(int expr, const char *msg);

#endif

#ifndef Color_Hole_Scanner
#define Color_Hole_Scanner

  #define ANSI_COLOR_RED "\x1b[31m"
  #define ANSI_COLOR_GREEN "\x1b[32m"
  #define ANSI_COLOR_YELLOW "\x1b[33m"
  #define ANSI_COLOR_BLUE "\x1b[34m"
  #define ANSI_COLOR_MAGENTA "\x1b[35m"
  #define ANSI_COLOR_CYAN "\x1b[36m"
  #define ANSI_COLOR_BRED "\x1b[91m"
  #define ANSI_COLOR_BGREEN "\x1b[92m"
  #define ANSI_COLOR_BYELLOW "\x1b[93m"
  #define ANSI_COLOR_BBLUE "\x1b[94m"
  #define ANSI_COLOR_BMAGENTA "\x1b[95m"
  #define ANSI_COLOR_BCYAN "\x1b[96m"
  #define ANSI_COLOR_RESET "\x1b[0m"

#endif

#ifndef TIME_FORMAT
  #define TIME_FORMAT "(%02d:%02d:%02d, %02d-%02d-%d)" 

  #define TIME_FORMAT_ARGS(tm) tm.tm_hour, tm.tm_min, tm.tm_sec, tm.tm_mday, tm.tm_mon + 1, 1900 + tm.tm_year // [Giorno - Mese - Anno] (ora:minuti:secondi)
#endif

