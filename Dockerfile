# ---------- Build stage ----------
FROM gradle:8.5-jdk17-alpine as builder

WORKDIR /app

# Copy only files needed for dependencies resolution first (for build caching)
COPY build.gradle settings.gradle gradle.properties ./
COPY gradle ./gradle

# Trigger Gradle to download dependencies (faster rebuilds)
RUN gradle --no-daemon dependencies

# Now copy the actual project
COPY . .

# Build the JAR in production mode (with Vaadin optimization)
RUN gradle -Pvaadin.productionMode=true clean bootJar -x test

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy jar from build container
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
