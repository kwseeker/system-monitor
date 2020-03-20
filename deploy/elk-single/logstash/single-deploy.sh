#!/usr/bin/env bash

#docker run --rm -it --name logstash-tmp logstash:7.5.1
#docker cp logstash-tmp:/usr/share/logstash/config ./

#-p 5044:5044 \
#-p 9600:9600 \
#-p 9011-9014:9011-9014 \

docker run -di \
    --name lgs-single \
    --network=host \
    -v /home/lee/docker/logstash/config:/usr/share/logstash/config \
    -v /home/lee/docker/logstash/pipeline:/usr/share/logstash/pipeline \
    -e xpack.monitoring.elasticsearch.hosts=http://localhost:9200 \
    logstash:7.5.1
