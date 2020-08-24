# ES文档

官方测试数据：https://www.elastic.co/guide/en/kibana/7.9/getting-started.html#get-data-in



## ES简介

分布式数据搜索分析引擎，近乎实时的搜索分析，支持结构非结构话各种数据(ES不要求文档的格式一致，但是要求同名字段类型必须一致)。

数据输入：数据存储在序列化的`JSON`文档，多节点分片存储。到排索引。启用动态映射后，`Elasticsearch`自动检测并向索引添加新字段，也可以显式定义映射来完全控制字段的存储和索引方式。

数据输出：可以通过`ES Rest Client`或命令行命令检索数据，支持结构化数据查询、全文查询和它们的复合查询。除了搜索单个术语外，您还可以执行短语搜索，相似性搜索和前缀搜索，并获得自动完成建议。

+ `Query DSL`
+ `SQL-style queries`

可伸缩性和弹性：集群，分片，跨集群复制。

> 如何自定义映射？



## ES初体验

软件包部署

Docker镜像部署

> 注意集群部署地时候，要保证磁盘空闲空间足够（具体参考水位线设置）否则分片分配可能异常，导致集群亚健康状态。

```shell
# 通过Rest API 查看集群健康状态
GET /_cat/health?v
curl -X<VERB> '<PROTOCOL>://<HOST>:<PORT>/<PATH>?<QUERY_STRING>' -d '<BODY>'
curl -X GET "http://localhost:9201/_cat/health?v&pretty" 
```

**基本操作**: 创建/删除索引，查看索引状态，增删改查数据

```shell
# 创建索引(不存在则创建)并插入数据
# customer：索引名称 _doc: 索引类型,可自定义 1: 文档Id（不存在则插入存在则更新） pretty: 用于美化输出格式
curl -X PUT "http://localhost:9201/customer/_doc/1?pretty"	\
	-H 'Content-Type: application/json'	\
	-d '{
		"name":"Arvin Lee"		#插入的文档数据
	}'
	
    {
      "_index" : "customer",
      "_type" : "_doc",
      "_id" : "1",				#文档id（一个文档是json字符串，一个索引可以存储多个文档的“摘要”信息）
      "_version" : 1,			#索引版本号，每修改一次就+1
      "result" : "created",
      "_shards" : {
        "total" : 2,
        "successful" : 1,		# 
        "failed" : 0			# 
      },
      "_seq_no" : 0,			#更新操作序列值
      "_primary_term" : 1		#
    }
# 查看所有索引列表
curl -X GET "http://localhost:9201/_cat/indices?v"
# 删除索引
curl -XDELETE "http://localhost:9201/customer1?pretty"

# 通过文档id查看索引某个文档内容
curl -X GET "localhost:9201/customer/_doc/1?pretty"
# 插入文档数据（追加数据的方式）
curl -X POST "localhost:9201/customer/_doc?pretty" \
	-H 'Content-Type: application/json' \
	-d'{
      "name": "John Doe"
    }'
# 从二进制文件批量插入文档数据
curl -XPOST "localhost:9201/bank/_bulk?pretty&refresh"	\
	-H "Content-Type: application/json" \
	--data-binary "@accounts.json"		#acounts.json是文档名
# 删除索引中某条文档
# 通过id进行删除,注意id默认生成格式是20Byte随机字符串
curl -XDELETE "http://localhost:9201/customer/_doc/H--sFnQBA1zWdlqBSRSp?pretty"
# 通过匹配筛选删除
curl -X POST "localhost:9201/customer/_delete_by_query?pretty" -H 'Content-Type: application/json' -d'
{
  "query": { 
    "match": {
      "name": "John Doe"
    }
  }
}
'
```

**数据搜索**

```shell
# bank是索引
curl -X GET "localhost:9201/bank/_search?pretty" -H 'Content-Type: application/json' -d'
    {
      "query": { "match_all": {} },
      "sort": [
        { "account_number": "asc" }
      ]
    }
    '

    {
      "took" : 63,				#搜索时间ms
      "timed_out" : false,		#搜索是否超时
      "_shards" : {				#搜索的分片范围
        "total" : 5,
        "successful" : 5,
        "skipped" : 0,
        "failed" : 0
      },
      "hits" : {				#搜索结果数据
        "total" : {
            "value": 1000,
            "relation": "eq"
        },
        "max_score" : null,		#最相似文档的相似度score
        "hits" : [ {			#匹配的数据列表，默认显示10条
          "_index" : "bank",
          "_type" : "_doc",
          "_id" : "0",
          "sort": [0],
          "_score" : null,
          "_source" : {"account_number":0,"balance":16623,"firstname":"Bradshaw","lastname":"Mckenzie","age":29,"gender":"F","address":"244 Columbus Place","employer":"Euron","email":"bradshawmckenzie@euron.com","city":"Hobucken","state":"CO"}
        }, {
          "_index" : "bank",
          "_type" : "_doc",
          "_id" : "1",
          "sort": [1],
          "_score" : null,
          "_source" : {"account_number":1,"balance":39225,"firstname":"Amber","lastname":"Duke","age":32,"gender":"M","address":"880 Holmes Lane","employer":"Pyrami","email":"amberduke@pyrami.com","city":"Brogan","state":"IL"}
        }, ...
        ]
      }
    }
```

**搜索规则**：

+ **搜索**

  + 匹配

    + 单词匹配

      ```
      "query": { "match": { "address": "mill lane" } }
      ```

    + 短语匹配

      ````
      "query": { "match_phrase": { "address": "mill lane" } }
      ````

    + 必须匹配，推荐匹配，必须不匹配

      ```
      "query": {
          "bool": {
              "must": [
                  { "match": { "age": "40" } }
              ],
              "should": [
                  ...
              ],
              "must_not": [
                  { "match": { "state": "ID" } }
              ]
          }
      }
      ```

  + 条件过滤

    ```
    "query": {
        "bool": {
          "filter": {
            "range": {
              "balance": {
                "gte": 20000,
                "lte": 30000
              }
            }
          }
        }
      }
    ```

+ **排序**

  ```
  "sort": [
  	{ "account_number": "asc" }
  ]
  ```

+ **分页**

  ```
  "from": 10,
  "size": 10
  ```

+ **数据聚合**

  + 分组

    分组里面还可以加排序条件。

    ```
    "aggs": {
        "group_by_state": {
            "terms": {
    	        "field": "state.keyword"
            }
        }
    }
    ```

  + 求平均

    ```
    "aggs": {
        "average_balance": {
            "avg": {
    	        "field": "balance"
            }
        }
    }
    ```

示例：

```shell
curl -X GET "localhost:9201/bank/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "size": 0,
  "aggs": {
    "group_by_state": {
      "terms": {
        "field": "state.keyword",
        "order": {
          "average_balance": "desc"
        }
      },
      "aggs": {
        "average_balance": {
          "avg": {
            "field": "balance"
          }
        }
      }
    }
  }
}
'
```

**搜索多个index和多个type下的数据**

> ```txt
> /_search：所有索引，所有type下的所有数据都搜索出来
> /index1/_search：指定一个index，搜索其下所有type的数据
> /index1,index2/_search：同时搜索两个index下的数据
> /*1,*2/_search：按照通配符去匹配多个索引
> /index1/type1/_search：搜索一个index下指定的type的数据
> /index1/type1,type2/_search：可以搜索一个index下多个type的数据
> /index1,index2/type1,type2/_search：搜索多个index下的多个type的数据
> /_all/type1,type2/_search：_all，可以代表搜索所有index下的指定type的数据
> ```



## ES配置

[官方配置文档](https://www.elastic.co/guide/en/elasticsearch/reference/current/settings.html)

### 配置文档

+ **elasticsearch.yml**

+ **jvm.options**

  ```
  # <ES版本号>:<JVM参数>
  8-:-Xmx2g
  ```

+ **log4j2.properties**

### 配置类型

+ **JVM配置**

+ **安全与审核配置**

  [springboot为ES实体封装审计Auditing功能](https://www.cnblogs.com/lori/p/13442919.html)

  ES的日志审计功能可以用来做日志输出过滤。

+ **断路器设置**

  Elasticsearch包含多个断路器用于防止操作造成OutOfMemoryError（定义对内存使用的限制规则）。

+ **分片以及路由设置**

  初始恢复，副本分配，重新平衡或添加或删除节点时控制分片分配规则。

+ **跨集群副本设置**

  为了实现数据备份。

+ **集群组网与节点发现设置**

+ **字段数据缓存设置**

  字段数据缓存主要用于对字段进行排序或对字段计算聚合。它将所有字段值加载到内存中，以便提供基于文档的对这些值的快速访问。

  默认没有限制，可以限制为ES分配的堆内存的百分比如30%，或设置绝对值如12GB。

  ```
  indices.fielddata.cache.size
  ```

+ **HTTP设置**（http.*）

  ```yaml
  http.port	#对外服务端口
  http.host	#对外服务的域名或ip
  http.cors.allow-origin
  ```

+ **索引生命周期管理设置**

  ILM

+ **索引管理设置**

  定义索引创建规则（如不存在则创建），索引删除规则（如必须指定索引名称才能删除），远程修改索引百名单。

+ **索引恢复设置**

+ **索引缓存设置**

+ **证书设置**

+ **本地网关设置**

+ **日志设置**

+ **机器学习设置**

+ **监控设置**

+ **节点设置**

  指定主节点、数据节点、摄取节点、机器学习节点、传输节点。

  节点拓展与删除。

  集群重启与滚动重启。

  滚动升级。

  远程集群。

+ **网络设置**

  ```yaml
  network.host			#节点在集群网络中的host名
  discovery.seed_hosts	#其他加入同一集群的节点host名
  http.port
  transport.port
  network.bind_host
  network.publish_host
  ```

+ **节点查询缓存设置**

  是否启动查询缓存以及缓存大小限制。

+ **查询设置**

  最大查询子句个数和结果返回最大聚合桶数。

+ **安全设置**

+ **分片请求缓存设置**

+ **快照生命周期管理设置**

+ **传输设置**

+ **线程池设置**

+ **监视器设置**

  监控并提醒ES集群状态给用户（比如发邮件、Slack、PagerDuty）。

### X-Pack

是ES拓展插件，包括安全（用户管理、日志审计）、监控、告警、报告、机器学习等功能。默认安装，功能强大但是使用付费，刚开始有30天的体验期。

[Java客户端配置X-Pack](https://www.elastic.co/guide/en/elasticsearch/reference/current/setup-xpack-client.html)

### ES插件

[Elasticsearch Plugins and Integrations](https://www.elastic.co/guide/en/elasticsearch/plugins/7.9/index.html)



## 索引模块

+ 索引分析模块

+ 索引分片分配模块

+ 索引块模块

  限制索引读写执行权限。

+ 类型映射模块
+ 索引段整合模块
+ 相似度模块

+ 慢查询日志模块
+ 存储模块
+ Translog模块
+ 历史版本模块
+ 索引排序模块
+ 索引压缩模块

## 映射

起初，我们说"索引"和关系数据库的“库”是相似的，“类型”和“表”是对等的。

这是一个不正确的对比，导致了不正确的假设。在关系型数据库里,"表"是相互独立的,一个“表”里的列和另外一个“表”的同名列没有关系，互不影响。但在类型里字段不是这样的。

**在一个Elasticsearch索引里，所有不同类型的同名字段内部使用的是同一个lucene字段存储**。也就是说，上面例子中，user类型的user_name字段和tweet类型的user_name字段是存储在一个字段里的，两个类型里的user_name必须有一样的字段定义。

这可能导致一些问题，例如你希望同一个索引中"deleted"字段在一个类型里是存储日期值，在另外一个类型里存储布尔值。

最终新版本ES的索引的映射类型只有一个`_doc`, 且查询语句中也不推荐使用映射类型`_type`。

**查看索引的mappings信息**：

```shell
curl -XGET http://localhost:9201/customer/_mapping\?pretty
{
  "customer" : {			#索引名
    "mappings" : {
      "properties" : {		#包含的字段
        "lists" : {				#某个字段名
          "properties" : {
            "name" : {			#字段里面的字段
              "type" : "text",	#字段类型
              "fields" : {
                "keyword" : {			#	
                  "type" : "keyword",	#
                  "ignore_above" : 256	#
                }
              }
            }
          }
        },
        "name" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        },
        "tags" : {
          "type" : "text",
          "fields" : {
            "keyword" : {
              "type" : "keyword",
              "ignore_above" : 256
            }
          }
        }
      }
    }
  }
}
```

添加mappin字段映射类型

```
curl -X PUT "localhost:9201/customer/_mapping?pretty" -H 'Content-Type: application/json' -d'
{
    "properties": {
      "blob": {
        "type": "binary"
      }
    }
}
'
```

> mapping映射类型在索引建立后只能新增不能修改。



### 字段数据类型

就是前面mapping查询结果中的

```
"type" : "<字段数据类型>",
```

+ [Alias](https://www.elastic.co/guide/en/elasticsearch/reference/current/alias.html)

  为字段取别名。

  ```
  "type":"alias"
  ```

+ [Arrays](https://www.elastic.co/guide/en/elasticsearch/reference/current/array.html)

  如下，使用`[]`。并没有保留字。

  ```shell
  curl -X POST "localhost:9201/customer/_doc?pretty" -H 'Content-Type: application/json' -d'
  {
  	"tags": ["魔兽争霸人物", "人族"],
  	"lists":[{
    		"name":"卡迪文"
    	},{
    		"name":"阿尔萨斯"
    	}]
  }          
  ' 
  ```

+ [Binary](https://www.elastic.co/guide/en/elasticsearch/reference/current/binary.html)

  Base64编码字符串

  ```
  "type":"binary"
  ```

  先添加Binary映射类型。

  ```shell
  curl -X PUT "localhost:9201/customer?pretty" -H 'Content-Type: application/json' -d'
  {
    "mappings": {
      "properties": {
        "blob": {
          "type": "binary"
        }
      }
    }
  }
  '
  ```

  插入文档。

  ```shell
  curl -X POST "localhost:9201/customer/_doc?pretty" -H 'Content-Type: application/json' -d'
  {
  	"name": "密码"，
      "passwd":"U29tZSBiaW5hcnkgYmxvYg=="
  }'
  ```

+ [Boolean](https://www.elastic.co/guide/en/elasticsearch/reference/current/boolean.html)

  ```
  "type": "boolean"
  ```

+ [Date](https://www.elastic.co/guide/en/elasticsearch/reference/current/date.html)

  ```
  "type": "date"
  ```

  ```
  curl -X PUT "localhost:9200/my-index-000001?pretty" -H 'Content-Type: application/json' -d'
  {
    "mappings": {
      "properties": {
        "date": {
          "type":   "date",
          "format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"
        }
      }
    }
  }
  '
  ```

+ [Date nanoseconds](https://www.elastic.co/guide/en/elasticsearch/reference/current/date_nanos.html)
+ [Dense vector](https://www.elastic.co/guide/en/elasticsearch/reference/current/dense-vector.html)
+ [Histogram](https://www.elastic.co/guide/en/elasticsearch/reference/current/histogram.html)
+ [Flattened](https://www.elastic.co/guide/en/elasticsearch/reference/current/flattened.html)
+ [Geo-point](https://www.elastic.co/guide/en/elasticsearch/reference/current/geo-point.html)
+ [Geo-shape](https://www.elastic.co/guide/en/elasticsearch/reference/current/geo-shape.html)
+ [IP](https://www.elastic.co/guide/en/elasticsearch/reference/current/ip.html)
+ [Join](https://www.elastic.co/guide/en/elasticsearch/reference/current/parent-join.html)
+ [Keyword](https://www.elastic.co/guide/en/elasticsearch/reference/current/keyword.html)
+ [Nested](https://www.elastic.co/guide/en/elasticsearch/reference/current/nested.html)
+ [Numeric](https://www.elastic.co/guide/en/elasticsearch/reference/current/number.html)
+ [Object](https://www.elastic.co/guide/en/elasticsearch/reference/current/object.html)
+ [Percolator](https://www.elastic.co/guide/en/elasticsearch/reference/current/percolator.html)
+ [Point](https://www.elastic.co/guide/en/elasticsearch/reference/current/point.html)
+ [Range](https://www.elastic.co/guide/en/elasticsearch/reference/current/range.html)
+ [Rank feature](https://www.elastic.co/guide/en/elasticsearch/reference/current/rank-feature.html)
+ [Rank features](https://www.elastic.co/guide/en/elasticsearch/reference/current/rank-features.html)
+ [Search-as-you-type](https://www.elastic.co/guide/en/elasticsearch/reference/current/search-as-you-type.html)
+ [Sparse vector](https://www.elastic.co/guide/en/elasticsearch/reference/current/sparse-vector.html)
+ [Text](https://www.elastic.co/guide/en/elasticsearch/reference/current/text.html)
+ [Token count](https://www.elastic.co/guide/en/elasticsearch/reference/current/token-count.html)
+ [Shape](https://www.elastic.co/guide/en/elasticsearch/reference/current/shape.html)
+ [Constant keyword](https://www.elastic.co/guide/en/elasticsearch/reference/current/constant-keyword.html)
+ [Wildcard](https://www.elastic.co/guide/en/elasticsearch/reference/current/wildcard.html)

### 元数据字段

- [_field_names](https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-field-names-field.html)

  用在查询、聚合以及脚本中 —— 用于查找指定字段的值非空的文档是否存在。

  ```
  curl -XGET localhost:9201/customer/_search\?pretty -H "Content-Type:application/json" -d '{ 
          "query": {
          "terms": {"_field_names": ["name"]}
      }
  }'
  {
    "took" : 0,
    "timed_out" : false,
    "_shards" : {
      "total" : 1,
      "successful" : 1,
      "skipped" : 0,
      "failed" : 0
    },
    "hits" : {
      "total" : {
        "value" : 0,
        "relation" : "eq"
      },
      "max_score" : null,
      "hits" : [ ]
    }
  }
  ```

- [_ignored](https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-ignored-field.html)

- [_id](https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-id-field.html)

  文档在索引中的唯一标识id。

- [_index](https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-index-field.html)

  标注文档所属的索引。

- [_meta](https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-meta-field.html)

- [_routing](https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-routing-field.html)

  用于将文档路由到指定的分片上. 通过如下公式将文档路由到特定的分片:

  ```
  shard_num = hash(_routing) % num_primary_shards
  ```

  如果不指定`_routing`的值, 默认使用文档的`_id`字段. 如果存在父文档则使用其`_parent`的编号.

- [_source](https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-source-field.html)

  文档原始JSON数据内容。

- [_type](https://www.elastic.co/guide/en/elasticsearch/reference/current/mapping-type-field.html)

### 映射参数

### 动态映射



## 文本分析

## 索引模板

## 数据流

## 摄取节点

## 搜索数据

## 查询领域特定语言

## 聚合

## 事件查询语言（EQL）

## SQL访问

## 脚本

## 索引生命周期管理

## 自动缩放

## 集群监控

## 索引冻结

## 数据汇总＆转换

## HA集群

## 快照与恢复

## 集群安全

## 集群报警和索引事件

## 命令行工具

## 注意事项

## 术语表

## REST APIs

