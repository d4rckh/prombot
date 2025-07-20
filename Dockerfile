# Stage 1: Build the JAR using Gradle and JDK 17
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /home/gradle/project

COPY gradlew .
COPY gradle gradle
RUN chmod +x ./gradlew

# To cache the gradle jars
RUN ./gradlew --no-daemon --no-configuration-cache

COPY settings.gradle.kts .
COPY gradle.properties .
COPY app ./app

# Build the JAR
RUN ./gradlew :app:clean :app:build --no-daemon --no-configuration-cache

# Stage 2: Run using lightweight JRE base image
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /home/gradle/project/app/build/libs/app-all.jar ./prombot.jar

# Run the application
ENTRYPOINT ["java", "-jar", "prombot.jar"]
