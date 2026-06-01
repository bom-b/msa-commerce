FROM docker.io/library/gradle:8.8-jdk21-alpine AS builder
WORKDIR /workspace
COPY common-module ./common-module
COPY payment-service/build.gradle.kts payment-service/settings.gradle.kts ./payment-service/
WORKDIR /workspace/payment-service
RUN gradle dependencies --no-daemon --quiet
COPY payment-service/src ./src
RUN gradle bootJar --no-daemon -x test

FROM docker.io/library/eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/payment-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
