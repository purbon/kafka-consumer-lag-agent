package com.purbon.kafka.lag.agent;

import com.purbon.kafka.lag.agent.model.TopicPartitionGroupInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AgentKafkaAdminClient {

  private static final Logger LOGGER = LogManager.getLogger(AgentKafkaAdminClient.class);
  private final Properties props;

  private KafkaConsumer<String, String> consumer;
  private AdminClient adminClient;

  public AgentKafkaAdminClient(Properties props) {
    this.props = props;
    this.adminClient = KafkaAdminClient.create(props);
    this.consumer = new KafkaConsumer<>(props);
  }

  public Collection<ConsumerGroupListing> listConsumerGroups() {

    Collection<ConsumerGroupListing> consumerGroups = new ArrayList<>();

    try {
      consumerGroups = adminClient
          .listConsumerGroups()
          .all()
          .get();
    } catch (Exception e) {
      LOGGER.error(e);
    }

    return consumerGroups;
  }
  public List<List<TopicPartitionGroupInfo>> describeConsumerGroups() throws IOException {

    Collection<ConsumerGroupListing> consumerGroups = new ArrayList<>();

    try {
      consumerGroups = adminClient
          .listConsumerGroups()
          .all()
          .get();
    } catch (Exception e) {
      LOGGER.error(e);
    }
    LOGGER.debug("number of Consumer Groups: "+consumerGroups.size());
    List<String> groupIds = consumerGroups
        .stream()
        .map(consumerGroupListing -> consumerGroupListing.groupId())
        .collect(Collectors.toList());

    Map<String, ConsumerGroupDescription> groupsInfo = getConsumerGroupInfo(groupIds);

    return consumerGroups
        .stream()
        .map(consumerGroupListing -> {

          List<TopicPartitionGroupInfo> listOfTopics = new ArrayList<>();

          try {
            Map<TopicPartition, OffsetAndMetadata> offsetsMetadata = listConsumerGroupOffsets(consumerGroupListing.groupId());

            Map<TopicPartition, Long> offsetsForPartition = findLastPartitionOffset(offsetsMetadata.keySet());

            LOGGER.debug("offsets.size= "+offsetsMetadata.size());

            offsetsMetadata.forEach((topicPartition, offsetAndMetadata) -> {

              String topicPartitionLine =  topicPartition.topic()+"-"+topicPartition.partition();
              TopicPartitionGroupInfo info = new TopicPartitionGroupInfo(
                  consumerGroupListing.groupId(),
                  topicPartitionLine,
                  offsetsForPartition.getOrDefault(topicPartition, 0L),
                  offsetAndMetadata.offset()
                  );

              listOfTopics.add(info);
            });
          } catch (Exception ex) {
            LOGGER.error(ex);
          }
          LOGGER.debug("GroupId="+consumerGroupListing.groupId()+" Partitions="+listOfTopics.size());
          return listOfTopics;
        })
        .collect(Collectors.toList());
  }

  public Map<String, Long> describeConsumerGroupLag(String groupId) {
    Map<String, Long> listOfTopics = new HashMap<>();

    try {
      Map<TopicPartition, OffsetAndMetadata> offsetsMetadata = listConsumerGroupOffsets(groupId);
      Map<TopicPartition, Long> offsetsForPartition = findLastPartitionOffset(offsetsMetadata.keySet());

      LOGGER.debug("offsets.size= "+offsetsMetadata.size());

      offsetsMetadata.forEach((topicPartition, offsetAndMetadata) -> {
        Long lag = offsetsForPartition.getOrDefault(topicPartition, 0L) - offsetAndMetadata.offset();
        if (listOfTopics.get(topicPartition.topic()) == null) {
          listOfTopics.put(topicPartition.topic(), 0L);
        }
        Long updatedLag = listOfTopics.get(topicPartition.topic()) + lag;
        listOfTopics.put(topicPartition.topic(), updatedLag);

      });
    } catch (Exception ex) {
      LOGGER.error(ex);
    }
    LOGGER.debug("GroupId="+groupId+" Partitions="+listOfTopics.size());
    return listOfTopics;
  }

  private Map<TopicPartition, Long> findLastPartitionOffset(Set<TopicPartition> partitions) {
    Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);
    return endOffsets;
  }
  private Map<TopicPartition, OffsetAndMetadata> listConsumerGroupOffsets(String groupId)
      throws IOException {

    try {
      return adminClient
          .listConsumerGroupOffsets(groupId)
          .partitionsToOffsetAndMetadata()
          .get();
    } catch (Exception e) {
      throw new IOException(e);
    }
  }

  private Map<String, ConsumerGroupDescription> getConsumerGroupInfo(Collection<String> groupIds) throws IOException {

    Map<String, ConsumerGroupDescription> consumerGroupInfo;

    try {
      consumerGroupInfo = adminClient
          .describeConsumerGroups(groupIds)
          .all()
          .get();
    } catch (Exception e) {
      throw new IOException(e);
    }

    return consumerGroupInfo;

  }



}
