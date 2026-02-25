# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build

# build argument selects module to compile (default order-service)
ARG MODULE=order-service

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
# copy module poms to leverage caching
COPY order-service/pom.xml order-service/pom.xml
COPY discovery-server/pom.xml discovery-server/pom.xml
COPY config-server/pom.xml config-server/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY catalog-service/pom.xml catalog-service/pom.xml

RUN mvn dependency:go-offline -B

# Copy source trees for all modules
COPY order-service/src order-service/src
COPY discovery-server/src discovery-server/src
COPY config-server/src config-server/src
COPY api-gateway/src api-gateway/src
COPY catalog-service/src catalog-service/src

# Build the selected module and its dependencies
RUN mvn clean package -pl ${MODULE} -am -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jdk AS runtime

# repeat build arg so runtime stage knows which module was built
ARG MODULE=order-service

# Set working directory
WORKDIR /app

# Copy the JAR from build stage
COPY --from=build /app/${MODULE}/target/${MODULE}-0.0.1-SNAPSHOT.jar ./app.jar


# Expose port 8080
EXPOSE 8080

# Run the application (profiles may be supplied via environment)
CMD ["java", "-jar", "app.jar"]