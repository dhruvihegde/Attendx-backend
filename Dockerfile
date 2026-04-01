# ── Stage 1: Build ──────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml first so Maven can cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Run ─────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built JAR from stage 1
COPY --from=build /app/target/attendx-backend-1.0.0.jar app.jar

# Expose port 8080
EXPOSE 8080

# JVM memory flags optimized for Fly.io free tier (256MB RAM)
ENV JAVA_OPTS="-Xms64m -Xmx200m -XX:+UseSerialGC -XX:MaxMetaspaceSize=80m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
