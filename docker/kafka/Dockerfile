FROM centos:centos7
MAINTAINER pere.urbon@gmail.com
ENV container docker

# 1. Adding Confluent repository
RUN rpm --import https://packages.confluent.io/rpm/5.5/archive.key
COPY confluent.repo /etc/yum.repos.d/confluent.repo
RUN yum clean all

# 2. Install zookeeper and kafka
RUN yum install -y java-1.8.0-openjdk-devel
RUN yum install -y wget
#RUN yum install -y java-1.8.0-openjdk-devel
RUN yum install -y confluent-platform-2.12
#schema-registry package is required to run kafka-avro-console-producer
RUN wget https://github.com/jiaqi/jmxterm/releases/download/v1.0.1/jmxterm-1.0.1-uber.jar

# 3. Configure Kafka 
COPY server.properties /etc/kafka/server.properties
COPY consumer.properties /etc/kafka/consumer.properties

# 4. Configure a JMX agent

COPY lag-exporter-agent.jar /etc/kafka/lag-exporter-agent.jar

EXPOSE 9092

CMD kafka-server-start /etc/kafka/server.properties
