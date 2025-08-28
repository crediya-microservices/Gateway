FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY applications/app-service/build/libs/Gateway.jar /app/Gateway.jar

ENTRYPOINT ["java", "-jar", "/app/Gateway.jar"]
