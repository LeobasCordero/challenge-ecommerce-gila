# Stage 1: Build stage
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app

# 1. Copy pom.xml first
COPY pom.xml .

# 2. Pre-fetch dependencies using cache mount
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline -B

# 3. Copy config and source code afterwards
COPY config ./config
COPY src ./src

# 4. Compile and package with cache mount
RUN --mount=type=cache,target=/root/.m2 mvn package -DskipTests -B

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/*.jar app.jar

# JVM optimized config
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Use NON root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Expose port
EXPOSE 8080

# Run the jar file with support for JVM startup optimizations
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
