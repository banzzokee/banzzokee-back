FROM openjdk:17-alpine

COPY ./build/libs/banzzokee-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app.jar"]