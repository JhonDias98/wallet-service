FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /build

COPY pom.xml ./pom.xml
COPY user-service/pom.xml user-service/pom.xml

RUN mvn -q -B -DskipTests -f user-service/pom.xml dependency:go-offline

COPY user-service/src user-service/src

RUN mvn -q -B -DskipTests -f user-service/pom.xml package

FROM amazoncorretto:21-alpine-jdk
WORKDIR /app
COPY --from=build /build/user-service/target/user-service-1.0.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
