# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the project and create the Fat JAR
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the server JAR from the build stage
# Note: The output JAR name depends on your pom.xml version/artifactId
COPY --from=build /app/target/chess-1.0-SNAPSHOT-server.jar app.jar

# Expose the chess server port
EXPOSE 12345

# Run the server with memory limits for free tier
CMD ["java", "-Xmx400M", "-jar", "app.jar"]
