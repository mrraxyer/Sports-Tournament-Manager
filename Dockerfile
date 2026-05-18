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

# Set environment variables with defaults
ENV DB_HOST=postgres \
    DB_PORT=5432 \
    DB_NAME=deportivos \
    DB_USERNAME=postgres \
    DB_PASSWORD=root \
    MASTER_USERNAME=admin \
    MASTER_EMAIL=admin@sportstournament.com \
    MASTER_PASSWORD=admin123 \
    JAVA_OPTS="-Xmx512m -Xms256m"

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

