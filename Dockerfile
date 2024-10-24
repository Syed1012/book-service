#official java runtime
FROM openjdk:17-jdk-alpine

#Working directory inside the container
WORKDIR /app

#Copy of maven built spring boot jar to the container
COPY target/book-service-0.0.1-SNAPSHOT.jar /app/book-service.jar

#Application port
EXPOSE 8080

#Command to run the jar file
ENTRYPOINT ["java", "-jar", "/app/book_service.jar"]