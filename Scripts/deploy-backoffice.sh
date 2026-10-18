#!/usr/bin/env bash
set -e

# Define a pasta de distribuição
DEPLOY_DIR="./libs/backoffice"

echo "🚀 Preparing deployment package in $DEPLOY_DIR..."

# Limpa versões anteriores e cria a estrutura
rm -rf "$DEPLOY_DIR"
mkdir -p "$DEPLOY_DIR/libs"

# 1. Copia o JAR do módulo (o teu código)
cp aisafe.app.backoffice.console/target/aisafe.app.backoffice.console-1.0-SNAPSHOT.jar "$DEPLOY_DIR/backoffice-app.jar"

# 2. Copia todas as dependências externas (H2, ANTLR, etc.)
cp aisafe.app.backoffice.console/target/dependency/* "$DEPLOY_DIR/libs/"

echo "✅ Deploy package created successfully!"
echo "📂 Location: $DEPLOY_DIR"