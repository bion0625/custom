FROM openjdk:17-jdk-slim AS build
WORKDIR /workspace
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY src ./src
RUN chmod +x gradlew && ./gradlew bootJar

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /workspace/build/libs/*.jar app.jar
COPY docker-entrypoint.sh /app/entrypoint.sh
RUN chmod +x /app/entrypoint.sh
ENV DB_HOST=db \
    DB_PORT=5433 \
    DB_USER=postgres \
    DB_PASSWORD=postgres \
    DB_NAME=true_self
ENTRYPOINT ["/app/entrypoint.sh"]
