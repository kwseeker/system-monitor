# ES操作

更多参考：

[ES REST API](https://www.elastic.co/guide/en/elasticsearch/reference/7.6/rest-apis.html)

## 基本操作

+ **创建索引**

  shop为索引名;  
  body参数可选: mappings store 表示是否提取概要,文档额外单独存储;   mappings index 表示是否要基于此字段建索引。

  ```
  curl --location --request PUT 'http://localhost:9201/shop' \
  --header 'Content-Type: application/json' \
  --data-raw '{
      "settings" : {
          "number_of_shards":3,
          "number_of_replicas" : 1,
          "refresh_interval":"30s"
      },
      "mappings":{
          "properties":{
              "shopid":{
                  "type":"text",
                  "store":false,
                  "index":true
              },
              "shopname":{
                  "type":"text",
                  "store":false,
                  "index":true
              },
              "shopdesc":{
                  "type":"text",
                  "store":false,
                  "index":true
              }
          }	
      }
  }'
  ```

+ **删除索引**

  ```
  curl --location --request DELETE 'http://localhost:9201/shop'
  ```

+ **向索引添加文档**
  
  + 单条插入

    _doc后可以手动指定文档id,如果不手动指定的话会默认创建一个唯一ID。

    ```
    curl --location --request POST 'http://localhost:9201/shop/_doc' \
    --header 'Content-Type: application/json' \
    --data-raw '{
      "shopid":"10001",
      "shopname":"茶味轩",
      "shopdesc":"饮茶，品味生活"
    }'
    ```
  + 批量插入
    ```
    curl --location --request POST 'http://localhost:9201/shop/_bulk' \
    --header 'Content-Type: application/json' \
    --data-raw '
    { "create" : { "_index" : "shop"} }
    { "shopid":"10011","shopname":"东北饺子馆" ,"shopdesc":"专业地道东北饺子" }
    { "create" : { "_index" : "shop"} }
    { "shopid":"10012","shopname":"全聚德烤鸭南山店" ,"shopdesc":"正宗北京烤鸭" }
    { "create" : { "_index" : "shop", "_id":"sdhuajgosduuersdg"} }
    { "shopid":"10013","shopname":"河南绘面馆" ,"shopdesc":"包你吃饱" }
    '
    ```

+ 删除文档

  ```
  curl --location --request DELETE 'http://localhost:9201/shop/_doc/1'
  ```

+ 查询文档

  + 查询索引库中所有文档

    ```
    curl --location --request GET 'http://localhost:9201/shop/_search' \
    --header 'Content-Type: application/json' \
    --data-raw '{
      "size":100,
      "query":{
        "match_all" : {}
      }
    }'
    ```

  + 通过文档id查询文档

    ```
    curl --location --request GET 'http://localhost:9201/shop/_doc/WWDb_HAB2Q1MND4rs5PE'
    ```

  + 根据关键词查询文档

    ```
    curl --location --request GET 'http://localhost:9201/shop/_doc/_search' \
    --header 'Content-Type: application/json' \
    --data-raw '{
      "query":{
        "term":{
          "shopdesc":"茶"
        }
      }
    }'
    ```

  + queryString查询

    将输入参数进行分词后去索引库进行检索。  
    如下是将“口味”进行分词，然后分别去检索“口”和“味”，最后将结果合并。

    ```
    curl --location --request GET 'http://localhost:9201/shop/_doc/_search' \
    --header 'Content-Type: application/json' \
    --data-raw '{
      "query":{
        "query_string":{
          "default_field":"shopname",
          "query":"口味"
        }
      }
    }'
    ```

+ 修改文档

  ```
  curl --location --request POST 'http://localhost:9201/shop/_doc/WWDb_HAB2Q1MND4rs5PE' \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "shopid":"10001",
    "shopname":"茶味轩",
    "shopdesc":"品茶，品味生活"
  }'
  ```


