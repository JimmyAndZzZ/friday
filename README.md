# friday

### 主要特性

- 微服务网关，可自由接入Dubbo、http、springcloud(nacos)、rpc和grpc
    - 支持客户端进行rest接口调用、sdk调用和interface调用，只需要引入framework jar包
    - 支持接口负载均衡，权限验证、路由选择、限流、熔断、重试和超时等操作
    - 支持消息订阅、发送，保证ack
    - 支持接口收费，以及接口同步和异步两种请求方式

----  

- 日志中心，通过字节码asm，对程序植入探针
    - 支持日志链路收集，保证日志在rabbitmq、http、kafka、dubbo过程中保证连续性
    - 参考skywalking的写法，对控件有良好的扩展性
    - 通过运行日志收集，可形成服务血缘图，并生成实时运行血缘图

----

- 分布式调度定时器
    - 提供注解和手动方式进行定时器的注册和删除，

----

- 分布式事务
    - 提供lcn、tcc两种事务方式，txc待完成

----

### 配置文件

接入前需要创建friday.properties

| 参数名                         | 是否必填 | 模块        | 示例                                               | 描述                                                                                                                                     |
|-----------------------------|------|-----------|--------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------|
| TRANSACTION_POINT_ROOT_PATH | 否    | framework | /tmp/transaction/                                | 事务文件保存路径                                                                                                                               |
| APPLICATION_NAME            | 是    | all       | test                                             | 应用名                                                                                                                                    |
| COLLECTOR_PATH              | 是    | agent     | com.jimmy.friday.demo.controller                 | 日志收集路径<br/>日志只会收集该配置路径下                                                                                                                |
| LOG_PUSH_LEVEL              | 否    | agent     | test                                             | 日志推送等级<br/>(只收集对应等级的日志，可逗号分隔)                                                                                                          |
| LOG_ALL_PUSH                | 否    | agent     | true                                             | 日志是否全推送(不建议)                                                                                                                           |
| LOG_COLLECTOR_POINT         | 否    | agent     |                                                  | 配置日志收集方法<br/>例如：<br/> LOG_COLLECTOR_POINT.com.jimmy.friday.demo.controller.AgentController=ggg,getSimpleQueueMessage<br/>也可以通过@Trace注解 |
| QPS_COLLECTOR_POINT         | 否    | agent     | 与上述配置同理                                          | 监控某个方法的qps情况                                                                                                                           |
| IGNORE_COLLECTOR_PATH       | 否    | agent     | com.jimmy.friday.demo.controller                 | 忽略收集路径，由于字节码会对类有操作，有时候会导致程序报错<br/>例如，mybatis-plus会根据entity字段名进行查询时会有问题                                                                 |
| IGNORE_COLLECTOR_CLASS      | 否    | agent     | com.jimmy.friday.demo.controller.AgentController | 类忽略，全路径，逗号分隔                                                                                                                           |
| ADDRESS                     | 是    | agent     | 127.0.0.1                                        | 本地应用ip                                                                                                                                 |
| BATCH_SIZE                  | 是    | agent     | 10                                               | 日志批量推送条数                                                                                                                               |
| COLLECTOR_SERVER            | 是    | all       | 127.0.0.1:33233                                  | 服务端地址                                                                                                                                  |
| VERSION                     | 否    | framework | release-1.0.0                                    | 当前服务版本号                                                                                                                                |
| APP_ID                      | 是    | framework | test                                             | 客户端请求appId                                                                                                                             |
| WEIGHT                      | 否    | framework | 1                                                | 当前服务权重                                                                                                                                 |
| OFFSET_PATH                 | 否    | framework | /data/gateway/offset/                            | 推送消息offset文件路径                                                                                                                         |