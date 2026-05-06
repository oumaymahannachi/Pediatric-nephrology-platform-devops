FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY eureka-server/pom.xml ./eureka-server/pom.xml
COPY config-server/pom.xml ./config-server/pom.xml
COPY api-gateway/pom.xml ./api-gateway/pom.xml
COPY auth-service/pom.xml ./auth-service/pom.xml
COPY treatment-monitoring-service/pom.xml ./treatment-monitoring-service/pom.xml
COPY prescription-service/pom.xml ./prescription-service/pom.xml
COPY treatment-service/pom.xml ./treatment-service/pom.xml
COPY lab-results-service/pom.xml ./lab-results-service/pom.xml
COPY messaging-service/pom.xml ./messaging-service/pom.xml
COPY dossiermedical-service/pom.xml ./dossiermedical-service/pom.xml
COPY config-server/src ./config-server/src
RUN mvn clean package -DskipTests -q -pl config-server -am

FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/config-server/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
