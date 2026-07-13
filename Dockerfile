# Stage 1: Build stage
FROM maven:3.8.8-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml, config rulesets, and source code
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src
COPY config ./config

# Compile and package
RUN mvn package -DskipTests -B

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
