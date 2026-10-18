CC = gcc

CFLAGS = -Wall -Wextra -Iinclude

LIBS = -lcjson -lm

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
src/safetyViolation.c \
src/util.c \
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
src/safetyViolation.c \
src/util.c \
src/reportGeneration.c \
tests/test_util.c \
tests/test_validation.c  \
tests/test_violation.c \
tests/test_simulation.c \
tests/test_flight.c \
tests/test_signals.c \
tests/test_steps.c

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
	./$(TEST_BIN)

# =========================================================
# CLEAN
# =========================================================

clean:
	rm -f src/*.o log/*.txt app tests/*.o tests/.*txt test_runner test.log