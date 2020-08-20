package top.kwseeker.monitor.elasticsearch;

import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.ActiveShardCount;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

/**
 * ES　客户端操作测试
 *
 * For more : https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html
 * ES官方文档是见过众多框架文档写的最详细的了，每个接口的demo案例都有
 */
public class ESClientTest {

    private final static String indexName = "cli-test";
    private final static int shardsNum = 5;
    private final static int replicasNum = 2;

    private RestHighLevelClient client;

    @Before
    public void init() {
        client = new RestHighLevelClient(RestClient.builder(
                new HttpHost("localhost", 9201, "http"),
                new HttpHost("localhost", 9202, "http"),
                new HttpHost("localhost", 9203, "http")
        ));
    }

    /**
     * 创建索引
     * https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-create-index.html
     */
    @Test
    public void testCreateIndex() {
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        //分片与副本配置
        request.settings(Settings.builder()
                .put("index.number_of_shards", shardsNum)
                .put("index.number_of_replicas", replicasNum)
        );
        //mapping配置
        request.mapping("{" +
                "\"properties\":{" +
                    "\"newsId\":{" +
                        "\"type\":\"text\"," +
                        "\"index\":false" +
                    "}," +
                    "\"newsTitle\":{" +
                        "\"type\":\"text\"," +
                        "\"index\":true" +
                        //"\"analyzer\":\"ik_smart\"" +
                    "}," +
                    "\"newsContent\":{" +
                        "\"type\":\"text\"," +
                        "\"index\":true" +
                        //"\"analyzer\":\"ik_smart\"" +
                    "},\"readCount\":{" +
                        "\"type\":\"integer\"," +
                        "\"index\":true" +
                    "}" +
                "}" +
            "}", XContentType.JSON);
        //可选参数配置
        request.setTimeout(TimeValue.timeValueMillis(2));
        request.setMasterTimeout(TimeValue.timeValueMinutes(1));
        request.waitForActiveShards(ActiveShardCount.from(replicasNum));

        try {
            //同步执行
            CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
            if(createIndexResponse.isAcknowledged()) {
                System.out.println("所有节点都已确认请求: " + createIndexResponse.index());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除索引
     */
    @Test
    public void testDeleteIndex() {
        DeleteIndexRequest request = new DeleteIndexRequest(indexName);
        try {
            AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
            if(response.isAcknowledged()) {
                System.out.println("所有节点都已确认请求");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 异步的方式创建索引
     */
    @Test
    public void testAsyncCreateIndex() throws InterruptedException {
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        //分片与副本配置
        request.settings(Settings.builder()
                .put("index.number_of_shards", shardsNum)
                .put("index.number_of_replicas", replicasNum)
        );
        //mapping配置
        request.mapping("{" +
                "\"properties\":{" +
                "\"newsId\":{" +
                "\"type\":\"text\"," +
                "\"index\":false" +
                "}," +
                "\"newsTitle\":{" +
                "\"type\":\"text\"," +
                "\"index\":true" +
                //"\"analyzer\":\"ik_smart\"" +
                "}," +
                "\"newsContent\":{" +
                "\"type\":\"text\"," +
                "\"index\":true" +
                //"\"analyzer\":\"ik_smart\"" +
                "},\"readCount\":{" +
                "\"type\":\"integer\"," +
                "\"index\":true" +
                "}" +
                "}" +
                "}", XContentType.JSON);
        //可选参数配置
        request.setTimeout(TimeValue.timeValueMillis(2));
        request.setMasterTimeout(TimeValue.timeValueMinutes(1));
        request.waitForActiveShards(ActiveShardCount.from(replicasNum));

        CountDownLatch countDownLatch = new CountDownLatch(1);
        ActionListener<CreateIndexResponse> listener =
                new ActionListener<CreateIndexResponse>() {
                    @Override
                    public void onResponse(CreateIndexResponse createIndexResponse) {
                        System.out.println("success: " + createIndexResponse.index());
                        countDownLatch.countDown();
                    }
                    @Override
                    public void onFailure(Exception e) {
                        System.out.println("failed: " + e.getMessage());
                        countDownLatch.countDown();
                    }
                };
        //异步执行
        client.indices().createAsync(request, RequestOptions.DEFAULT, listener);
        countDownLatch.await();
    }

    /**
     * 单条插入文档
     */
    @Test
    public void testCreateDocument() {
        IndexRequest request = new IndexRequest(indexName);

        News news = new News();
        news.setId("0001");
        news.setNewsId(UUID.randomUUID().toString());
        news.setNewsTitle("有时感觉这世界真不是真的");
        news.setNewsContent("有种不断轮回一次又一次重复悲惨人生的赶脚");
        news.setReadCount(1234);

        request.id(UUID.randomUUID().toString())
                .source(JSON.toJSONString(news), XContentType.JSON);
        try {
            IndexResponse response = client.index(request, RequestOptions.DEFAULT);
            System.out.println("文档插入ID: " + response.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 批量插入文档
     */

    /**
     * 删除文档
     */

    /**
     * 根据关键字查询
     */

    /**
     *　多词条匹配
     */

    /**
     * 布尔查询
     */

    /**
     * 布尔范围查询
     */
}
