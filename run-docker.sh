#!/bin/bash

if [ "$1" == "dev" ]; then
  echo "Starting the application in development mode with H2 database..."
  docker-compose -f docker-compose-dev.yml -p wallet-service-dev up -d
  
  echo "Application is starting at http://localhost:8080"
  echo "OpenAPI is available at http://localhost:8080/swagger-ui/index.html"
  echo "H2 Console will be available at http://localhost:8080/h2-console"
  echo "H2 Console settings:"
  echo "  JDBC URL: jdbc:h2:mem:walletdb"
  echo "  Username: sa"
  echo "  Password: password"
  
elif [ "$1" == "prod" ]; then
  echo "Starting the application in production mode with PostgreSQL..."
  docker-compose up -d

  echo "Application is starting at http://localhost:8080"
  echo "  Host: postgres"
  echo "  Port: 5432"
  echo "  Database: walletdb"
  echo "  Username: postgres"
  echo "  Password: postgres"
  
else
  echo "Please specify a profile: dev or prod"
  echo "Usage: ./run-docker.sh [dev|prod]"
  exit 1
fi

