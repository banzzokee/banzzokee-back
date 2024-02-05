#!/bin/sh

docker pull rabbitmq:management-alpine
docker run -d --name rabbitmq-banzzokee -p 5672:5672 -p 15672:15672 --restart=unless-stopped rabbitmq:management-alpine