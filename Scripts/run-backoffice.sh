#!/usr/bin/env bash

# Define o classpath (igual ao teu exemplo)
# Assume que o build-all.sh já correu e criou a pasta target/dependency
# Usa ; para separar os caminhos no classpath (Windows) ou : para Unix.
export BASE_CP="aisafe.app.backoffice.console/target/aisafe.app.backoffice.console-1.0-SNAPSHOT.jar:aisafe.app.backoffice.console/target/dependency/*"

echo "▶️ Starting Backoffice Console..."

# Chama a classe Main (usando : como no exemplo para Unix)
java -cp "$BASE_CP" aiSafe.Main