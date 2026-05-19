FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /build

# Copy pom.xml and download dependencies (layer caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build the application
COPY . .
RUN mvn clean package -DskipTests

# Final stage
FROM eclipse-temurin:25-jdk-jammy

WORKDIR /app

# Copy built JAR from builder
COPY --from=builder /build/target/SportsTournamentManager-*.jar app.jar

# Expose port
EXPOSE 8080

# Set Java options (optional, can be adjusted)

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

