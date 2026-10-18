CC = gcc

CFLAGS = -Wall -Wextra -Iinclude -I/opt/homebrew/include

LIBS = -L/opt/homebrew/lib -lcjson -lm

# =========================================================
# APP
# =========================================================

SRC = \
src/main.c \
src/simulation.c \
src/flight.c \
src/validator.c \
src/json_loader.c \
src/config_loader.c \
src/reportGeneration.c

OBJ = $(SRC:.c=.o)

all: app

app: $(OBJ)
	$(CC) $(CFLAGS) -o app $(OBJ) $(LIBS)

# =========================================================
# COMMON TEST SOURCES
# =========================================================

TEST_SRC = \
src/simulation.c \
src/flight.c \
src/validator.c \
src/json_loader.c \
src/config_loader.c \
src/reportGeneration.c \
tests/test_us105.c \
tests/test_validation.c  \
tests/test_us106.c \
tests/test_us107.c \
tests/test_us108.c \
tests/test_us109.c

# =========================================================
# TEST RUNNER
# =========================================================

TEST_MAIN = tests/main.c

# =========================================================
# TEST EXECUTABLES
# =========================================================

TEST_BIN = test_runner

TEST_ALL_SRC = \
$(TEST_MAIN) \
$(TEST_SRC)

$(TEST_BIN): $(TEST_ALL_SRC)
	$(CC) $(CFLAGS) $(TEST_ALL_SRC) -o $(TEST_BIN) $(LIBS)

tests: $(TEST_BIN)
	./$(TEST_BIN) -pthread -lrt -lm

# =========================================================
# CLEAN
# =========================================================

clean:
	rm -f src/*.o log/*.txt app tests/*.o tests/.*txt test_runner test.log