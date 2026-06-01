FROM docker.io/library/gradle:8.8-jdk21-alpine AS builder
WORKDIR /workspace
COPY common-module ./common-module
COPY stock-service/build.gradle.kts stock-service/settings.gradle.kts ./stock-service/
WORKDIR /workspace/stock-service
RUN gradle dependencies --no-daemon --quiet
COPY stock-service/src ./src
RUN gradle bootJar --no-daemon -x test

FROM docker.io/library/eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/stock-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
