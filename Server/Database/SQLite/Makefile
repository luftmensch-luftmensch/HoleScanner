CC = gcc # Compilatore da usare -> Controllare la versione di gcc che Azure offre!

#CFLAGS = -Wall -Werror -O2 -g -fstack-clash-protection -fstack-protector-all

CFLAGS = -O2 -Wall -Werror # Per una lista più ampia https://developers.redhat.com/blog/2018/03/21/compiler-and-linker-flags-gcc

LDLIBS = -lm # Flag da usare per il linker

OBJECTFILES =

DATABASE = HoleScanner.db

HOST = $(shell hostname)

LOAD_LIBRARY = -lsqlite3

TARGET-DATABASE = run # nome dell'eseguibile prodotto per il testing del database

SRC-Database = database.c

all: $(TARGET-DATABASE)
	@printf "\e[32m%b\e[0m" "\n\tCreazione del file completata\n"

# Compiliamo il sorgente Server.c con i parametri sopra definiti
$(TARGET-DATABASE) : $(OBJECTFILESS)
	@printf "\e[32m%b\e[0m" "Compilazione del Server con i seguenti parametri:\n\n"
	$(CC) $(CFLAGS) -o $(TARGET-DATABASE) $(OBJECTFILES) $(LDLIBS) $(SRC-Database) $(LOAD_LIBRARY)

.PHONY: depend clean # Controlliamo che non sia presente realmente un file chiamato clean

clean:
	rm -f $(OBJECTFILES) $(TARGET-DATABASE) $(DATABASE)
	@printf "\e[32m%b\e[0m" "Eliminazione file completata\n"

valcheck:
	@printf "\e[32m%b\e[0m" "Check dell'eseguibile con valgrind\n"
	valgrind --tool=memcheck --leak-check=yes --track-origins=yes ./${TARGET-DATABASE}
