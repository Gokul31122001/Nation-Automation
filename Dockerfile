# ====== BUILD STAGE ======
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# ====== RUNTIME STAGE ======
FROM selenium/standalone-chrome:latest  # ✅ Preinstalled Chrome + Driver + headless ready
USER root
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
