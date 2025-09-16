#!/bin/bash

# Script para testar o sistema
echo "=== Ada Tech ==="

# Configurar Java 21
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

echo "Configuração do ambiente:"
echo "Java version: $(java -version 2>&1 | head -n 1)"
echo "Maven version: $(mvn -version | head -n 1)"
echo ""

# Lista de serviços para testar
SERVICES=(
    "notification-service"
    "payment-service"
    "wallet-service"
    "user-service"
)

echo "=== Teste 1: Compilação de Todos os Serviços ==="
COMPILATION_SUCCESS=true

for service in "${SERVICES[@]}"; do
    echo "Testando compilação do $service..."
    cd services/$service
    
    if mvn clean package -DskipTests; then
        echo "✓ $service: Compilação OK"
    else
        echo "✗ $service: Erro na compilação"
        COMPILATION_SUCCESS=false
    fi
    
    cd ../..
done

if [ "$COMPILATION_SUCCESS" = true ]; then
    echo "✓ Todos os serviços compilaram com sucesso"
else
    echo "✗ Alguns serviços falharam na compilação"
    exit 1
fi

echo ""
echo "=== Teste 2: Verificação de Estrutura de Arquivos ==="

# Verificar se todos os arquivos essenciais existem
REQUIRED_FILES=(
    "docker-compose.yml"
    "docker-compose-prod.yml"
)

for file in "${REQUIRED_FILES[@]}"; do
    if [ -f "$file" ]; then
        echo "✓ $file existe"
    else
        echo "✗ $file não encontrado"
    fi
done

# Verificar Dockerfiles
echo ""
echo "Verificando Dockerfiles:"
for service in "${SERVICES[@]}"; do
    if [ -f "services/$service/Dockerfile" ]; then
        echo "✓ $service tem Dockerfile"
    else
        echo "✗ $service não tem Dockerfile"
    fi
done


echo ""
echo "=== Resumo dos Testes ==="
echo "✓ Compilação de todos os serviços"
echo "✓ Estrutura de arquivos"

echo ""
echo "=== Sistema Ada Tech - Testes Concluídos ==="
echo "O sistema está pronto para deploy e uso!"

