FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY partner-gold-api/pom.xml partner-gold-api/pom.xml
COPY partner-gold-api/src partner-gold-api/src
RUN mvn -f partner-gold-api/pom.xml package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/partner-gold-api/target/partner-gold-api-1.0.0.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
