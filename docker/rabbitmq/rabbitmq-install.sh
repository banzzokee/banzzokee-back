#!/bin/sh

docker stop rabbitmq-banzzokee
docker rm rabbitmq-banzzokee
docker pull rabbitmq:management-alpine
docker run -d --name rabbitmq-banzzokee -p 5672:5672 -p 15672:15672 -p 61613:61613 --restart=unless-stopped rabbitmq:management-alpine
docker exec -it rabbitmq-banzzokee rabbitmq-plugins enable rabbitmq_stomp