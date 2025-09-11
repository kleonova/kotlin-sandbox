FROM openjdk:17-jre-slim
COPY build/libs/order-service.jar /app/order-service.jar
WORKDIR /app
CMD ["java", "-jar", "order-service.jar"]