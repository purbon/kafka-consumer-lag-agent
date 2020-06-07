package com.purbon.kafka.lag.agent.jmx;

import com.purbon.kafka.lag.agent.AgentKafkaAdminClient;
import com.purbon.kafka.lag.agent.model.TopicPartitionGroupInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GlobalConsumerLag implements GlobalConsumerLagMBean {

  private static final Logger LOGGER = LogManager.getLogger(GlobalConsumerLag.class);

  private AgentKafkaAdminClient adminClient;
  private Thread backgroundThread;
  private Map<String, List<Map<String, Object>>> consumerLagInfo;

  public GlobalConsumerLag(AgentKafkaAdminClient adminClient) {
    this.adminClient = adminClient;
    this.backgroundThread = new Thread();
    this.consumerLagInfo = new HashMap<>();

    startBackgroundThread();
  }

  public void startBackgroundThread() {

    LOGGER.debug("start background");
    this.backgroundThread = new Thread(() -> {
      try {
        while (true) {
          updateConsumerLag();
          Thread.sleep(5000L);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    this.backgroundThread.setName("backgroundThread");
    this.backgroundThread.start();

  }

  private synchronized void updateConsumerLag() {

   LOGGER.debug("update Global ConsumerLag");
    try {
     LOGGER.debug("describeConsumerGroups.size= "+adminClient.describeConsumerGroups().size());
     adminClient
         .describeConsumerGroups()
         .forEach(new Consumer<List<TopicPartitionGroupInfo>>() {
           @Override
           public void accept(List<TopicPartitionGroupInfo> groupInfo) {
             String groupId = "";
             List<Map<String, Object>> elems = new ArrayList<>();
             for(TopicPartitionGroupInfo info : groupInfo) {
               if (groupId.isEmpty()) {
                 groupId = info.getGroupId();
               }
               Map<String, Object> map = new HashMap<>();
               map.put("topicPartition", info.getTopicPartition());
               map.put("consumerOffset", info.getConsumerOffset());
               map.put("Lag",info.getTopicLastOffset()-info.getConsumerOffset());
               elems.add(map);
             }
             consumerLagInfo.put(groupId, elems);
           }
         });

    } catch (Exception ex) {
      LOGGER.error(ex);
      consumerLagInfo.clear();
    }

  }

  @Override
  public Map<String,  List<Map<String, Object>>> getConsumerLagByPartition() {
    return consumerLagInfo;
  }
}
