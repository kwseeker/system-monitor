#!/usr/bin/env bash

#Dockerfile：https://github.com/docker-library/elasticsearch/blob/c2f5a0165f9a3b6e071dfd413ea16fcca8522ff1/7/Dockerfile
#DockerHub：https://hub.docker.com/_/elasticsearch?tab=description
#ES docker 镜像使用官方文档：https://www.elastic.co/guide/en/elasticsearch/reference/7.5/docker.html

docker pull elasticsearch:7.5.1

docker run -di -p 9200:9200 -p 9300:9300 \
    --name=es-single \
    -e "discovery.type=single-node" \
    -v /home/lee/docker/elasticsearch/plugins:/usr/share/elasticsearch/plugins \
    elasticsearch:7.5.1
