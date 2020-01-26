#!/usr/bin/env bash

docker pull mobz/elasticsearch-head:5-alpine

docker run -di --name es-head-single -p 9100:9100 mobz/elasticsearch-head:5-alpine
