FROM openjdk:8-alpine

COPY target/wewe.jar /wewe/app.jar

CMD ["java", "-jar", "/wewe/app.jar"]
