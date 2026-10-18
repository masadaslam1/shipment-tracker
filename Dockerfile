# Multi-stage build: compile with Maven, run on a slim JRE
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY . .
RUN chmod +x mvnw && ./mvnw -B package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
