# Logstash

## 简介

### Logstash做什么

Logstash 用于收集数据，处理，并输出到指定目的地。

+ **数据收集**

  数据源支持很多种类型，如：stdin、file、TCP、Syslog、Collectd、RabbitMQ、Redis 等。

+ **数据处理**

  + Grok数据结构化

    将非结构化的数据统一成某种数据结构。

  + Date时间处理
  + GeoIP解析地址

+ **数据输出**

  如ES、Hadoop、邮箱等。

### 常用Logstash处理

#### 常用的数据采集

+ stdin (与stdout配合测试配置)

  ```shell
  # 最简单demo
  # 接收命令行输入，没有处理，以json格式直接输出到标准输出。虽然没有处理，但是Logstash还是会默认为输出添加额外字段，如：@version、host、@timestamp。
  bin/logstash -e 'input { stdin { } } output { stdout {} }'
  ```

+ Beats

  + FileBeat（文件数据采集）

  + [MetricBeat](https://www.elastic.co/guide/en/beats/metricbeat/7.9/index.html)（系统运行状态数据采集）

    MetricBeat默认提供了对Apache、MongoDB、MySQL、Nginx、Redis、Zookeeper、System的监控支持。

+ TCP

+ log4j

+ kafka

+ file

+ syslog

+ rabbitmq

+ redis

+ elasticsearch query

+ http
+ logstash dead_letter_queue

#### 常用的数据处理

+ [grok](https://www.elastic.co/guide/en/logstash/current/plugins-filters-grok.html)

   可以将非结构化的日志数据解析为结构化的、可查询的数据。这个工具非常适合syslog日志、apache和其他webserver日志、mysql日志，以及一般为人类而不是计算机编写的任何日志格式。

  Logstash默认附带120种模式（pattern），数据可以与这些模式进行匹配一旦匹配成功，Grok就可以从中提取各段数据，然后输出json格式结果。

  > 输入的数据格式是有要求的，要么匹配默认的120种模式要么，匹配自己定义的模式。

+ [geoip](https://www.elastic.co/guide/en/logstash/current/plugins-filters-geoip.html)

   解析IP增加地理位置字段。

+ date

   从字段解析日期以用作事件的Logstash时间戳。

+ dissect

   基于分隔符原理解析数据，解决grok解析时消耗过多cpu资源的问题。
   使用分隔符将非结构化事件数据提取到字段中。 解剖过滤器不使用正则表达式，速度非常快。

+ mutate

   使用最频繁的操作，可以对字段进行各种操作，比如重命名、删除、替换、更新等。

+ json

   对字段内容为json格式的数据进行解析。

+ ruby

   最灵活的插件，可以以ruby语言来随心所欲的修改Logstash Event对象

#### 常用的数据输出

+ stdout 标准输出

  输出到标准输出就是调试logstash设置用的，检验logstash是否启动正常，处理结果是否符合项目要求。

+ elasticsearch

+ email

+ exec

+ file

+ tcp

+ hdfs

+ nagios(报警)

+ statsd(监控UI)

#### 演示示例

最基本的测试logstash是否启动成功

```shell
cd /opt/logstash
sudo bin/logstash -e 'input { stdin { } } output { stdout {} }'
hello logstash
{
    "message" => "hello logstash",
    "@timestamp" => 2020-08-24T06:57:21.858Z,
    "@version" => "1",
    "host" => "lee-pc"
}
```

grok测试

```shell
sudo bin/logstash -e '
input { 
	stdin { } 
} 
filter {
  grok {
    match => [ "message", "%{LOGLEVEL:loglevel} - %{NOTSPACE:taskid} - %{NOTSPACE:logger} - %{WORD:label}( - %{INT:duration:int})?" ]
  }
}
output { 
	stdout {
		codec => json	#以json格式（键和值之间用":"连接）输出，默认是rubydebug格式（键和值之间用"=>"连接）
	} 
}'
# 输入　
INFO - 12345 - TASK_START - start
# 返回
{
	"message":"INFO - 12345 - TASK_START - start",
    "@timestamp":"2020-08－24T09:44:43.177Z",
    "taskid":"12345",
    "loglevel":"INFO",
    "label":"start",
    "logger":"TASK_START",
    "@version":"1",
	"host":"lee-pc"
}
```

其他

```ruby
input {
	# 
    beats {
    	port => "5044"
    }
    #
    file {
        path => "/var/log/http.log"
    }
}
filter {
	#
    grok {
    	# match => { "message" => "%{COMBINEDAPACHELOG}"}
    	match => { "message" => "%{IP:client} %{WORD:method} %{URIPATHPARAM:request} %{NUMBER:bytes} %{NUMBER:duration}" }
	}
	# 通过解析IP获取地理位置并添加到数据中
	geoip {
    	source => "clientip"		# GeoIp Filter 的必填字段，IP地址或者域名
    }
}
output {
	# 输出到标准输出就是调试logstash设置用的，检验结果是否符合项目要求
	stdout { 
		#编码成json格式
		codec => json
		codec => rubydebug 
		#惟一id
		id => "<my_uni_id>"
    }
    
	# 输出到ES
	elasticsearch {
        hosts => ["172.31.0.31:9200"]
        index => "logstash-%{type}-%{+YYYY.MM.dd}"
        document_type => "%{type}"
        flush_size => 20000
        idle_flush_time => 10
        sniffing => true
        template_overwrite => true
    }
}
```



## 官方文档



