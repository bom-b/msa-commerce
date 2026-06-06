FROM docker.io/library/gradle:8.8-jdk21-alpine AS builder
WORKDIR /workspace/api-gateway
COPY api-gateway/build.gradle.kts api-gateway/settings.gradle.kts ./
RUN gradle dependencies --no-daemon --quiet
COPY api-gateway/src ./src
RUN gradle bootJar --no-daemon -x test

FROM docker.io/library/eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/api-gateway/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
