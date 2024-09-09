FROM openjdk:17-jdk-alpine

WORKDIR /app

COPY target/microlabo-0.0.1-SNAPSHOT.jar app.jar

ENV DB_HOST=db
ENV DB_NAME=medilabo
ENV DB_USER=root
ENV DB_PASSWORD=root

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]