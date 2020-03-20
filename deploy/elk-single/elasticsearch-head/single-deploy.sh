#!/usr/bin/env bash

#docker pull mobz/elasticsearch-head:5-alpine

docker run -di --name es-head-single --network=host 474197200/elasticsearch-head:1.0
