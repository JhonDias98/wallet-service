FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml ./pom.xml
COPY notification-service/pom.xml notification-service/pom.xml

RUN mvn -q -B -DskipTests -f notification-service/pom.xml dependency:go-offline

COPY notification-service/src notification-service/src

RUN mvn -q -B -DskipTests -f notification-service/pom.xml package

FROM amazoncorretto:21-alpine-jdk
WORKDIR /app
COPY --from=build /build/notification-service/target/notification-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
