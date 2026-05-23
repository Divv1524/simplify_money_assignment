FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY pom.xml .
COPY simplify-money-api/pom.xml simplify-money-api/pom.xml
COPY simplify-money-api/src simplify-money-api/src
RUN mvn -f simplify-money-api/pom.xml package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /workspace/simplify-money-api/target/simplify-money-api-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
