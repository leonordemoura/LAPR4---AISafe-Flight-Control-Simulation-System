CC      = gcc
CFLAGS  = -Wall -Wextra -Iinclude
LDFLAGS =

ifeq ($(OS),Windows_NT)
    LDFLAGS += -lws2_32
    EXT = .exe
else
    EXT =
endif

# Diretórios fonte
SRC_COMMON = src/common
SRC_SERVER = src/server/logging_server

# Fontes comuns aos clientes
COMMON_SRC = $(SRC_COMMON)/protocol.c $(SRC_COMMON)/util.c

# Targets dos clientes
WEATHER_TARGET      = weather_client$(EXT)
PILOT_TARGET        = pilot_client$(EXT)
COLLABORATOR_TARGET = atcc_client$(EXT)
LOGGING_TARGET      = logging_server$(EXT)

# Fontes dos clientes
WEATHER_SRC      = src/client/weather/weather_client.c
PILOT_SRC        = src/client/pilot/pilot_client.c
COLLABORATOR_SRC = src/client/atcc/atcc_client.c

# Fontes do logging server
LOGGING_SRCS = \
    $(SRC_SERVER)/main_server.c \
    $(SRC_SERVER)/udp_receiver.c \
    $(SRC_SERVER)/http_handler.c \
    $(SRC_SERVER)/repository.c

all: $(WEATHER_TARGET) $(PILOT_TARGET) $(COLLABORATOR_TARGET) $(LOGGING_TARGET)

$(WEATHER_TARGET): $(COMMON_SRC) $(WEATHER_SRC)
	$(CC) $(CFLAGS) -o $@ $(COMMON_SRC) $(WEATHER_SRC) $(LDFLAGS)

$(PILOT_TARGET): $(COMMON_SRC) $(PILOT_SRC)
	$(CC) $(CFLAGS) -o $@ $(COMMON_SRC) $(PILOT_SRC) $(LDFLAGS)

$(COLLABORATOR_TARGET): $(COMMON_SRC) $(COLLABORATOR_SRC)
	$(CC) $(CFLAGS) -o $@ $(COMMON_SRC) $(COLLABORATOR_SRC) $(LDFLAGS)

$(LOGGING_TARGET): $(LOGGING_SRCS)
	$(CC) $(CFLAGS) -pthread -o $@ $(LOGGING_SRCS) -pthread $(LDFLAGS)

clean:
	rm -f $(WEATHER_TARGET) weather_client weather_client.exe \
	      $(PILOT_TARGET) pilot_client pilot_client.exe \
	      $(COLLABORATOR_TARGET) atcc_client atcc_client.exe \
	      $(LOGGING_TARGET) logging_server logging_server.exe

.PHONY: all clean