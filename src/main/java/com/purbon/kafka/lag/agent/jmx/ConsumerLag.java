package com.purbon.kafka.lag.agent.jmx;

import com.purbon.kafka.lag.agent.AgentKafkaAdminClient;
import com.purbon.kafka.lag.agent.model.TopicPartitionGroupInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConsumerLag implements ConsumerLagMBean {

  private static final Logger LOGGER = LogManager.getLogger(ConsumerLag.class);
  private final String groupId;

  private AgentKafkaAdminClient adminClient;
  private Thread backgroundThread;
  private Map<String, Long> consumerLagInfo;

  public ConsumerLag(AgentKafkaAdminClient adminClient, String groupId) {
    this.adminClient = adminClient;
    this.backgroundThread = new Thread();
    this.consumerLagInfo = new HashMap<>();
    this.groupId = groupId;

    startBackgroundThread();
  }

  public void startBackgroundThread() {

    LOGGER.debug("start background");
    this.backgroundThread = new Thread(() -> {
      try {
        while (true) {
          updateConsumerLag();
          Thread.sleep(3000L);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    this.backgroundThread.setName("GroupId backgroundThread");
    this.backgroundThread.start();

  }

  private synchronized void updateConsumerLag() {

   LOGGER.debug("updateConsumerLag for group "+groupId);
   consumerLagInfo.clear();
    try {
     adminClient
         .describeConsumerGroupLag(groupId)
         .forEach((key, value) -> consumerLagInfo.put(key, value));

    } catch (Exception ex) {
      ex.printStackTrace();
      LOGGER.error(ex);
      consumerLagInfo.clear();
    }

  }

  @Override
  public Map<String, Long> getConsumerLag() {
    return consumerLagInfo;
  }
}
