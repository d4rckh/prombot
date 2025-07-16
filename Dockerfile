# Stage 1: Build native image using GraalVM
FROM ghcr.io/graalvm/graalvm-ce:ol8-java17-22.3.3 AS builder

WORKDIR /home/gradle/project

RUN gu install native-image

COPY gradlew .
COPY gradle gradle
COPY settings.gradle.kts .
COPY gradle.properties .

COPY app ./app

RUN chmod +x ./gradlew

RUN ./gradlew :app:clean :app:nativeCompile --no-daemon --no-configuration-cache

FROM ubuntu:22.04

# RUN apk add --no-cache libstdc++ libgcc

WORKDIR /app

COPY --from=builder /home/gradle/project/app/build/native/nativeCompile/prombot ./prombot

# Make sure it's executable
RUN chmod +x ./prombot

ENTRYPOINT ["./prombot"]
