# ELK测试案例

此案例模拟ELK监控web应用logback日志。

logback -> logstash-logback-encoder -> logstash -> elasticsearch -> kibana

> logstash-logback-encode: 主要是将logback日志编码为JSON格式  
> logstash:  
>
> > logstash-input-tcp: 通过tcp接收JSON数据  
> > logstash-codec-json_lins: 读取以换行符分割的JSON数据  
> > logstash-filter-grok: 将非结构化事件数据解析为字段(经过lle处理后不需要这个了)  
> > logstash-output-elasticsearch: 将处理后的字段存储到ES    
>
> elasticsearch  
> 
> > elasticsearch-http: 通过ES http协议将数据存入ES.
> > 
> 
> kibana  

## logstash-logback-encoder

[logstash-logback-encoder](https://github.com/logstash/logstash-logback-encoder)

### 提供的logback Appender\编码器和布局

其中编码器和布局可以用在Logback原有的Appender里面，Github上的官方文档都提供了使用方法．

| Format        | Protocol   | Function | LoggingEvent | AccessEvent
|---------------|------------|----------| ------------ | -----------
| Logstash JSON | Syslog/UDP | Appender | [`LogstashUdpSocketAppender`](/src/main/java/net/logstash/logback/appender/LogstashUdpSocketAppender.java) | [`LogstashAccessUdpSocketAppender`](/src/main/java/net/logstash/logback/appender/LogstashAccessUdpSocketAppender.java)
| Logstash JSON | TCP        | Appender | [`LogstashTcpSocketAppender`](/src/main/java/net/logstash/logback/appender/LogstashTcpSocketAppender.java) | [`LogstashAccessTcpSocketAppender`](/src/main/java/net/logstash/logback/appender/LogstashAccessTcpSocketAppender.java)
| any           | any        | Appender | [`LoggingEventAsyncDisruptorAppender`](/src/main/java/net/logstash/logback/appender/LoggingEventAsyncDisruptorAppender.java) | [`AccessEventAsyncDisruptorAppender`](/src/main/java/net/logstash/logback/appender/AccessEventAsyncDisruptorAppender.java)
| Logstash JSON | any        | Encoder  | [`LogstashEncoder`](/src/main/java/net/logstash/logback/encoder/LogstashEncoder.java) | [`LogstashAccessEncoder`](/src/main/java/net/logstash/logback/encoder/LogstashAccessEncoder.java)
| Logstash JSON | any        | Layout   | [`LogstashLayout`](/src/main/java/net/logstash/logback/layout/LogstashLayout.java) | [`LogstashAccessLayout`](/src/main/java/net/logstash/logback/layout/LogstashAccessLayout.java)
| General JSON  | any        | Encoder  | [`LoggingEventCompositeJsonEncoder`](/src/main/java/net/logstash/logback/encoder/LoggingEventCompositeJsonEncoder.java) | [`AccessEventCompositeJsonEncoder`](/src/main/java/net/logstash/logback/encoder/AccessEventCompositeJsonEncoder.java)
| General JSON  | any        | Layout   | [`LoggingEventCompositeJsonLayout`](/src/main/java/net/logstash/logback/layout/LoggingEventCompositeJsonLayout.java) | [`AccessEventCompositeJsonLayout`](/src/main/java/net/logstash/logback/encoder/AccessEventCompositeJsonLayout.java)

### 案例演示（LogstashTcpSocketAppender）

1) 配置logback.xml
    ```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <configuration>
      <appender name="logstash" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
        <!-- logstash服务器地址和端口,支持尝试连接到多个目标(但是只发送给一个目标),只需要继续添加destination标签,或者多个地址以","分割 -->
        <destination>localhost:8888</destination>
        <!--<destination>localhost:8889</destination>-->
        <!--<destination>localhost:8890</destination>-->
        <!-- 提供了三种连接尝试策略,此处选择的策略是首要连接(preferPrimary),还有roundRobin,random -->
        <connectionStrategy>
            <preferPrimary>
                <secondaryConnectionTTL>5 minutes</secondaryConnectionTTL>
            </preferPrimary>
        </connectionStrategy>
        <!-- 如果所有目标都连接失败,则等待30秒后重新尝试连接,默认就是30s -->
        <reconnectionDelay>30 second</reconnectionDelay>
        <!-- 处理完后的数据先放缓冲,然后从缓冲中取数据传给logstash-input,默认8192Byte -->
        <writeBufferSize>81920</writeBufferSize>
        <!-- 值为0表示写缓冲禁用 -->
        <!--<writeBufferSize>0</writeBufferSize>-->
        <!-- 写超时时间,如果写超时,会启动重新连接,如果有启用writeBuffer里面的数据会在重连后丢失,如果需要避免丢失可以关闭写缓冲 -->
        <writeTimeout>1 minute</writeTimeout>
        <!-- 保持连接,如果事件频繁则保持连接,直到3分钟内没有新的事件来到才断开 -->
        <keepAliveDuration>3 minutes</keepAliveDuration>
        <!--输出的格式,推荐使用这个-->
        <encoder class="net.logstash.logback.encoder.LogstashEncoder" />
        <!-- 此外还支持SSL连接, 使用JVM默认的keystore/truststore -->
        <!--<ssl/>-->
    </appender>

      <root level="DEBUG">
          <appender-ref ref="stash" />
      </root>
    </configuration>
    ```
    
    通过LogstashEncoder编码后的日志格式
    ```json
    {
        "@timestamp":"2020-01-20T17:11:32.933+08:00",
        "@version":"1",
        "message":"Warn日志：logId=1000",
        "logger_name":"top.kwseeker.monitor.webapplogger.controller.LogGenController",
        "thread_name":"http-nio-8080-exec-1",
        "level":"DEBUG",
        "level_value":10000
    }
    ```

    注意事项:  
    1) TCP Appender必须配置编码器;
    2) TCP Appender是异步的,永远不会阻塞logback日志线程;
    3) 如果logstash-logback-encoder缓冲区占满,新的日志数据会被丢弃;
    4) TCP Appender连接断开会自动重连,但是断开时数据会丢失.

2) 配置logstash input插件

    ```
    input {
        tcp {
            port => 4560
            codec => json_lines
        }
    }
    ```

### 案例演示（LogstashEncoder）

1) 配置logback.xml
    ```xml
    <appender name="file" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_HOME}/temp.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>${LOG_HOME}/temporary/temp-%d{yyyy-MM-dd}.log.gz</fileNamePattern>
            <maxHistory>7</maxHistory>
        </rollingPolicy>
        <!-- 这里需要使用LogstashEncoder -->
        <encoder class="net.logstash.logback.encoder.LogstashEncoder"/>
    </appender>
    ```

