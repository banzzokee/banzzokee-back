#!/bin/sh

# elasticsearch 컨테이너 이미지 pull 및 elasticsearch 실행
docker pull docker.elastic.co/elasticsearch/elasticsearch:8.11.3
docker run -d --name elasticsearch-banzzokee -p 9200:9200 -p 9300:9300 --env-file ./es.env docker.elastic.co/elasticsearch/elasticsearch:8.11.3

# elasticsearch 내 nori분석기 설치
docker exec -it elasticsearch-banzzokee bin/elasticsearch-plugin install analysis-nori

# nori 분석기 적용을 위한 elasticsearch 재시작
docker restart elasticsearch-banzzokee

# username, password 설정(username : banzzokee, password : funnycode)
docker exec -it --user root elasticsearch-banzzokee /usr/share/elasticsearch/bin/elasticsearch-users useradd banzzokee -p funnycode -r superuser