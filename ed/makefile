CC=gcc
MYSQL_BASE=$(shell brew --prefix mysql)
MYSQL_INCLUDE=-I$(MYSQL_BASE)/include/mysql
MYSQL_LIB=-L$(MYSQL_BASE)/lib

CFLAGS=-fPIC -Wall -O3 $(MYSQL_INCLUDE)
LDFLAGS=-shared $(MYSQL_LIB)

all: libed.so libedth.so libedrec.so

libed.so: ed.c
	$(CC) $(CFLAGS) $(LDFLAGS) -o $@ $<

libedth.so: edth.c
	$(CC) $(CFLAGS) $(LDFLAGS) -o $@ $<

libedrec.so: edrec.c
	$(CC) $(CFLAGS) $(LDFLAGS) -o $@ $<

clean:
	-rm -f *.so *.o *~ unittest

test:
	@echo "Test will be handled by install script"