package com.purbon.kafka.lag.agent.model;

import java.io.Serializable;

public class ConsumerLagInfo implements Serializable {

  private final String groupId;
  private final Long offset;
  private final Long lag;
  private final String topicPartition;

  public ConsumerLagInfo(String groupId, String topicPartition, Long offset, Long lag) {
    this.topicPartition = topicPartition;
    this.groupId = groupId;
    this.offset = offset;
    this.lag = lag;
  }

  public Long getLag() {
    return lag;
  }

  public Long getOffset() {
    return offset;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getTopicPartition() {
    return topicPartition;
  }

}
