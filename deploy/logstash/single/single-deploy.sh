#!/usr/bin/env bash

#docker run --rm -it --name logstash-tmp logstash:7.5.1
#docker cp logstash-tmp:/usr/share/logstash/config ./

docker run -di \
    --name logstash \
    -p 5044:5044 \
    -p 9600:9600 \
    -p 9011-9014:9011-9014 \
    -v /home/lee/docker/logstash/config:/usr/share/logstash/config \
    -e xpack.monitoring.elasticsearch.hosts=http://192.168.100.206:9200 \
    #默认是不是加载的pipelines目录下面的logstash.conf
    -f /usr/share/logstash/config/logstash.conf
    logstash:7.5.1
