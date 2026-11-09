# --- STAGE 1: Build the application using Maven ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the pom.xml first to leverage Docker cache for dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# --- STAGE 2: Run the application with a lightweight JRE image ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the generated jar file from the previous stage
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080 (standard Spring Boot port)
EXPOSE 9090

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]