FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml ./pom.xml
COPY payment-service/pom.xml payment-service/pom.xml

RUN mvn -q -B -DskipTests -f payment-service/pom.xml dependency:go-offline

COPY payment-service/src payment-service/src

RUN mvn -q -B -DskipTests -f payment-service/pom.xml package

FROM amazoncorretto:21-alpine-jdk
WORKDIR /app
COPY --from=build /build/payment-service/target/payment-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
