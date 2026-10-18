#!/usr/bin/env bash

# Define o classpath (igual ao teu exemplo)
# Assume que o build-all.sh já correu e criou a pasta target/dependency
# Usa ; para separar os caminhos no classpath (Windows) ou : para Unix.
export BASE_CP="aisafe.app.bootstrap/target/aisafe.app.bootstrap-1.0-SNAPSHOT.jar:aisafe.app.bootstrap/target/dependency/*"

echo "▶️ Starting Bootstrap Console..."

# Chama a classe Main (usando : como no exemplo para Unix)
java -cp "$BASE_CP" aiSafe.app.bootstrap.AisafeBootstrap