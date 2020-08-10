# kafka命令



## 1. 安装kafka
brew install kafka

## 2.安装位置
/usr/local/Cellar/kafka

## 3.配置文件
/usr/local/etc/kafka/


## 4. 修改Kafka服务配置文件
修改配置文件 /usr/local/etc/kafka/server.properties

解除注释: listeners=PLAINTEXT://localhost:9092


## 5. 启动Kafka服务和关闭
kafka-server-start /usr/local/etc/kafka/server.properties &
nohup kafka-server-start /usr/local/etc/kafka/server.properties &
关闭
kafka-server-stop


## 6. 新建topic
kafka-topics --create --zookeeper 47.98.213.18:2181 --replication-factor 1 --partitions 1 --topic topic_a

查看已有主题list

kafka-topics --list --zookeeper 47.98.213.18:2181

修改分区数

kafka-topics --zookeeper 47.98.213.18:2181 --alter --topic topic_a  --partitions 2

## 7. 创建生产者
kafka-console-producer --broker-list localhost:9092 --topic topic_a


## 8. 创建消费者
kafka-console-consumer --bootstrap-server localhost:9092 --topic topic_a --from-beginning
kafka-console-consumer --bootstrap-server localhost:9092 -topic topic_a
