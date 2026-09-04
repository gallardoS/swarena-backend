FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw --batch-mode --no-transfer-progress dependency:go-offline

COPY src src
RUN ./mvnw --batch-mode --no-transfer-progress verify

FROM eclipse-temurin:21-jre-alpine

LABEL org.opencontainers.image.source="https://github.com/gallardoS/swarena-backend"

RUN addgroup --system swarena \
    && adduser --system --ingroup swarena swarena

WORKDIR /app
COPY --from=build /workspace/target/arena-0.0.1-SNAPSHOT.jar app.jar

USER swarena
EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=70.0", "-jar", "/app/app.jar"]
