FROM openjdk:17-jre-slim
COPY build/libs/notification-service.jar /app/notification-service.jar
WORKDIR /app
CMD ["java", "-jar", "notification-service.jar"]