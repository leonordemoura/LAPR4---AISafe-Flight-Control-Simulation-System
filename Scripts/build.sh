#!/usr/bin/env bash
set -e

# Vai para a raiz do projeto (pasta pai de Scripts/)
cd "$(dirname "$0")/.."

echo "🔨 Building project and generating reports..."

mvn $1 clean install dependency:copy-dependencies surefire-report:report-only -Daggregate=true checkstyle:checkstyle-aggregate -DlinkXRef=false -DskipTests

echo "✅ Build completed!"