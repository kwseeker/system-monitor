#!/usr/bin/env bash

#使用方法:
#将elk-cluster目录复制到本地存放docker容器的目录下,比如放在/home/lee/docker/下面(注意.env文件也要确保复制过去);
#进入/home/lee/docker/elk-cluster，执行 ./deploy.sh 。

#set -x

function prepare() {
    echo ">>>>>>> elk docker cluster prepare ..."
    # 1 镜像检查
    # logstash elasticsearch elasticsearch-head kibana
    image_keys=()
    image_values=()
    path_keys=()
    path_values=()
    while read line
    do
        if [[ ${line} == *"IMAGE"* ]]; then
            array=(${line//=/ })        #空格替换=
            image_keys[${#image_keys[@]}]=${array[0]}
            image_values[${#image_values[@]}]=${array[1]}
        fi
        if [[ ${line} == *"PATH"* ]]; then
            array=(${line//=/ })        #空格替换=
            path_keys[${#path_keys[@]}]=${array[0]}
            path_values[${#path_values[@]}]=${array[1]}
        fi
    done < ./.env

    #echo ${image_values[@]}
    #echo ${path_values[@]}

    # 检查镜像是否存在，不存在则拉取镜像
    for image in ${image_values[@]} ; do
        image_infos=(${image//:/ })
        image_detail=`docker images | grep ${image_infos[0]} | grep ${image_infos[1]}`
        if [[ ${image_detail} == "" ]] ; then
            echo "docker pull ${image}"
            docker pull ${image}
        fi
    done

    # 2 创建映射目录
    for dir in ${path_values[@]} ; do
        if [[ ! -d ${dir} ]]; then
            echo "mkdir -p ${dir}"
            mkdir -p ${dir}
        fi
    done
}

function container_up() {
    # 1 使用docker-compose启动docker集群
    echo ">>>>>>> elk docker cluster start ..."
    docker-compose up -d
    echo ">>>>>>> elk docker cluster start done"
}

#加载配置项到环境变量
prepare
container_up
