FROM docker.io/library/gradle:8.8-jdk21-alpine AS builder
WORKDIR /workspace
COPY common-module ./common-module
COPY order-service/build.gradle.kts order-service/settings.gradle.kts ./order-service/
WORKDIR /workspace/order-service
RUN gradle dependencies --no-daemon --quiet
COPY order-service/src ./src
RUN gradle bootJar --no-daemon -x test

FROM docker.io/library/eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/order-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
