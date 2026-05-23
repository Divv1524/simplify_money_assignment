FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY payment-gateway-api/pom.xml payment-gateway-api/pom.xml
COPY payment-gateway-api/src payment-gateway-api/src
RUN mvn -f payment-gateway-api/pom.xml package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/payment-gateway-api/target/payment-gateway-api-1.0.0.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
