#!/bin/sh

# elasticsearch 컨테이너 이미지 pull 및 elasticsearch 실행
docker pull docker.elastic.co/elasticsearch/elasticsearch:8.11.3
docker run -d --name elasticsearch-banzzokee -p 9200:9200 -p 9300:9300 -it -m 1GB -e "discovery.type=single-node" docker.elastic.co/elasticsearch/elasticsearch:8.11.3

# elasticsearch 내 nori분석기 설치
docker exec -it elasticsearch-banzzokee bin/elasticsearch-plugin install analysis-nori

# nori 분석기 적용을 위한 elasticsearch 재시작
docker restart elasticsearch-banzzokee

# username, password 설정(username : banzzokee, password : funnycode)
docker exec -it --user root elasticsearch-banzzokee /usr/share/elasticsearch/bin/elasticsearch-users useradd banzzokee -p funnycode -r superuser

# kibana 설치
docker pull docker.elastic.co/kibana/kibana:8.11.3
docker run -d --name kibana-banzzokee --link elasticsearch-banzzokee:elasticsearch-banzzokee -p 5601:5601 docker.elastic.co/kibana/kibana:8.11.3

# fingerprint 확인
echo "wait for 40seconds"
sleep 40
docker logs elasticsearch-banzzokee

# 키바나 인증코드 확인
docker exec -it kibana-banzzokee bin/kibana-verification-code