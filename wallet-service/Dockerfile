FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml ./pom.xml
COPY wallet-service/pom.xml wallet-service/pom.xml

RUN mvn -q -B -DskipTests -f wallet-service/pom.xml dependency:go-offline

COPY wallet-service/src wallet-service/src

RUN mvn -q -B -DskipTests -f wallet-service/pom.xml package

FROM amazoncorretto:21-alpine-jdk
WORKDIR /app
COPY --from=build /build/wallet-service/target/wallet-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
