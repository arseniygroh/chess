# Use a lightweight JDK 21 image
FROM openjdk:21-slim

# Copy the server JAR from the build folder
COPY target/chess-1.0-SNAPSHOT-server.jar app.jar

# Expose the chess server port
EXPOSE 12345

# Run the server
CMD ["java", "-Xmx512M", "-jar", "app.jar"]
