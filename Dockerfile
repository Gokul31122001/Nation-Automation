# ====== BUILD STAGE ======
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# ====== RUNTIME STAGE ======
# (Render doesn’t allow comments inline with instructions)
FROM selenium/standalone-chrome:latest

USER root
WORKDIR /app

# Copy built jar from previous stage
COPY --from=build /app/target/Nation-0.0.1-SNAPSHOT.jar app.jar

# Set environment variables for headless Chrome
ENV DISPLAY=:99
ENV JAVA_OPTS="-Dwebdriver.chrome.driver=/usr/bin/chromedriver"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
