#!/usr/bin/env bash

docker pull kibana:7.5.1

#docker run -i --link es-single:elasticsearch \
#    --name kibana-single \
#    -p 5601:5601 kibana:7.5.1

#容器中kibana.yml默认配置
#server.name: kibana
#server.host: "0"
#elasticsearch.hosts: [ "http://elasticsearch:9200" ]
#xpack.monitoring.ui.container.elasticsearch.enabled: true
#docker run -di --link es-single:elasticsearch \
#    --name kibana-single \
#    -v /home/lee/docker/kibana/config:/usr/share/kibana/config \
#    -p 5601:5601 kibana:7.5.1

docker run -di --network=host \
    --name kibana-single \
    -v /home/lee/docker/kibana/config:/usr/share/kibana/config \
    kibana:7.5.1
