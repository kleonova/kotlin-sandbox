FROM openjdk:17-jre-slim
COPY build/libs/auth-service.jar /app/auth-service.jar
WORKDIR /app
CMD ["java", "-jar", "auth-service.jar"]