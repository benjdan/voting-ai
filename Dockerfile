FROM maven:3.9.5-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/target/voting-ai-1.0.0.jar app.jar

EXPOSE 5000

ENV SPRING_PROFILES_ACTIVE=production
ENV SERVER_PORT=5000

ENTRYPOINT ["java", "-jar", "app.jar"]
