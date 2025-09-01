FROM openjdk:8-jdk-alpine as builder

ADD ./gateway/target/gateway-*.jar /app.jar

RUN sh -c 'touch /app.jar'
EXPOSE 30010

ENTRYPOINT ["java", "-jar","/app.jar"]