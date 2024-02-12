FROM openjdk:17-alpine

COPY ./build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod", "app.jar"]