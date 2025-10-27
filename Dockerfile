# Use Maven image to build the JAR
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Run the app using a lightweight JDK image
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/Nation-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
