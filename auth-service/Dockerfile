FROM docker.io/library/gradle:8.8-jdk21-alpine AS builder
WORKDIR /workspace
COPY common-module ./common-module
COPY auth-service/build.gradle.kts auth-service/settings.gradle.kts ./auth-service/
WORKDIR /workspace/auth-service
RUN gradle dependencies --no-daemon --quiet
COPY auth-service/src ./src
RUN gradle bootJar --no-daemon -x test

FROM docker.io/library/eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/auth-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
