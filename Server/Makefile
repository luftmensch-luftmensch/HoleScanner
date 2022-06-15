CC = gcc # Compilatore da usare -> Controllare la versione di gcc che Azure offre!

#CFLAGS = -Wall -Werror -O2 -g -fstack-clash-protection -fstack-protector-all

CFLAGS = -O2 -Wall -Werror -g # Per una lista più ampia https://developers.redhat.com/blog/2018/03/21/compiler-and-linker-flags-gcc

CFLAGS_WITH_THREADS = -pthread $(CFLAGS) # Flag necessaria per i sorgenti che fanno uso dei thread (L'opzione va esplicitata)

LDLIBS = -lm # Flag da usare per il linker

OBJECTFILES =

DATABASE = HoleScanner.db

LOAD_LIBRARY =  -lsqlite3

TARGET-Server = server # nome dell'eseguibile prodotto per il server

TARGET-Request-Handler = request-handler

TARGET-Tollerance-Threshold = tollerance-threshold # nome dell'eseguibile prodotto per il server che gestisce la porta

TARGET-Client = client # nome dell'eseguibile prodotto per il server

SRC-Server = Server.c Database/SQLite/database.c Utils/utils.c

SRC-Request-Handler = Request-Handler.c Database/SQLite/database.c Utils/utils.c

SRC-Tollerance-Threshold = Tollerance-Threshold.c Utils/utils.c # File sorgente che ha il compito di inviare la porta a cui il client Android deve collegarsi

SRC-Client = Client.c # Nome del file sorgente del Client

all: $(TARGET-Server) $(TARGET-Request-Handler) $(TARGET-Tollerance-Threshold) $(TARGET-Client) 
	@printf "\e[32m%b\e[0m" "\n\tCreazione del file completata\n"


# Compiliamo il sorgente Server.c con i parametri sopra definiti
$(TARGET-Server) : $(OBJECTFILES)
	@printf "\e[32m%b\e[0m" "Compilazione del Server con i seguenti parametri:\n\n"
	$(CC) $(CFLAGS_WITH_THREADS) -o $(TARGET-Server) $(OBJECTFILES) $(LDLIBS) $(SRC-Server) $(LOAD_LIBRARY)

$(TARGET-Request-Handler) : $(OBJECTFILES)
	@printf "\e[32m%b\e[0m" "Compilazione del Server (selection) con i seguenti parametri:\n\n"
	$(CC) $(CFLAGS_WITH_THREADS) -o $(TARGET-Request-Handler) $(OBJECTFILES) $(LDLIBS)  $(SRC-Request-Handler) $(LOAD_LIBRARY)

$(TARGET-Tollerance-Threshold) : $(OBJECTFILES)
	@printf "\e[32m%b\e[0m" "Compilazione del Server (tollerance) con i seguenti parametri:\n\n"
	$(CC) $(CFLAGS_WITH_THREADS) -o $(TARGET-Tollerance-Threshold) $(OBJECTFILES) $(LDLIBS) $(SRC-Tollerance-Threshold)

# Compiliamo il sorgente Client.c con i parametri sopra definiti
$(TARGET-Client) : $(OBJECTFILES)
	@printf "\e[32m%b\e[0m" "\nCompilazione del Client con i seguenti parametri:\n\n"
	$(CC) $(CFLAGS) -o $(TARGET-Client) $(OBJECTFILES) $(LDLIBS) $(SRC-Client)


.PHONY: depend clean # Controlliamo che non sia presente realmente un file chiamato clean

clean:
	rm -f $(OBJECTFILES) $(TARGET-Server) $(TARGET-Request-Handler) $(TARGET-Client) $(TARGET-Tollerance-Threshold)
	@printf "\e[32m%b\e[0m" "Eliminazione file completata\n"

valcheck:
	@printf "\e[32m%b\e[0m" "Check dell'eseguibile con valgrind\n"
	valgrind --tool=memcheck --leak-check=yes --track-origins=yes ./${TARGET-Server}
