# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jdk AS runtime

# Set working directory
WORKDIR /app

# Copy the JAR from build stage
COPY --from=build /app/target/virtual-clothing-store-0.0.1-SNAPSHOT.jar .

# Expose port 8080
EXPOSE 8080

# Run the application with docker profile
CMD ["java", "-jar", "-Dspring.profiles.active=docker", "virtual-clothing-store-0.0.1-SNAPSHOT.jar"]