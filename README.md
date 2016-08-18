# tjj-metrics-sdk
天机镜客户端

探针类轻量级的统计客户端，支持异步静默http发送监控数据，可通过开发适配器支持Hbase、elasticsearch、influxdb等数据库。

主要包含两种接口：
1. metric轻量级统计
meter（速率）， counter（计数），histogram（直方图），timer（时间分布），guage（绝对值）。

2. reporter接口
可以将统计数据格式化发送到后端，以便成图；

应用场景：
基础运维监控所需的agent；
应用级别监控（APM）；
作为采集大型分布式系统的发数客户端；
