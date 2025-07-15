# Stage 1: Build
FROM gradle:8.2.1-jdk17 AS builder

WORKDIR /home/gradle/project

COPY gradlew .
COPY gradle gradle
COPY settings.gradle.kts .
COPY gradle.properties .

COPY app ./app

RUN chmod +x ./gradlew

RUN ./gradlew :app:clean :app:shadowJar --no-daemon

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /home/gradle/project/app/build/libs/app-all.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
