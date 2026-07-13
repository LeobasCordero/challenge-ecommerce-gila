# Stage 1: Build stage
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml, config rulesets, and source code
COPY pom.xml .
COPY config ./config
COPY src ./src

# Compile and package using BuildKit cache mount to accelerate builds and avoid downloading dependencies repeatedly
RUN --mount=type=cache,target=/root/.m2 mvn package -DskipTests -B

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the jar file with support for JVM startup optimizations
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
