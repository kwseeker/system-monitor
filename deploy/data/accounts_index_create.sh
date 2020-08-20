#!/usr/bin/env bash

# 注意accounts.json最后一行要回车换行，否则批量插入时会报错"The bulk request must be terminated by a newline"
curl -H "Content-Type: application/json" -XPOST "localhost:9201/bank/_bulk?pretty&refresh" --data-binary "@accounts.json"
curl "localhost:9201/_cat/indices?v"


curl -X GET "localhost:9201/bank/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": { "match_phrase": { "address": "mill lane" } }
}
'
curl -X GET "localhost:9201/bank/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": {
    "bool": {
      "must": { "match_all": {} },
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
}
'
curl -X GET "localhost:9201/bank/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "size": 0,
  "aggs": {
    "group_by_state": {
      "terms": {
        "field": "state.keyword"
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

