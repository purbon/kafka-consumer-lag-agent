package com.purbon.kafka.lag.agent.model;

public class TopicPartitionGroupInfo {

  private final String groupId;
  private final Long consumerOffset;
  private final Long topicLastOffset;
  private final String topicPartition;

  public TopicPartitionGroupInfo(String groupId, String topicPartition, Long topicLastOffset, Long consumerOffset) {
    this.topicPartition = topicPartition;
    this.groupId = groupId;
    this.consumerOffset = consumerOffset;
    this.topicLastOffset = topicLastOffset;
  }

  public String getGroupId() {
    return groupId;
  }

  public Long getConsumerOffset() {
    return consumerOffset;
  }

  public Long getTopicLastOffset() {
    return topicLastOffset;
  }

  public String getTopicPartition() {
    return topicPartition;
  }
}
