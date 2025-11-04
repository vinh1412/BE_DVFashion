# Use Gradle or Maven builder (ở đây bạn đang dùng Gradle, nên đổi maven → gradle)
FROM gradle:8.5-jdk17 AS build

WORKDIR /app
COPY . .

RUN ./gradlew clean build -x test

# Runtime stage
FROM eclipse-temurin:17-jdk
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]