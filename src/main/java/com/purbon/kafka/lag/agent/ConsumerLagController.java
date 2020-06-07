package com.purbon.kafka.lag.agent;

import com.purbon.kafka.lag.agent.jmx.ConsumerLag;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConsumerLagController {

  private static final Logger LOGGER = LogManager.getLogger(ConsumerLagController.class);

  private AgentKafkaAdminClient adminClient;
  private Thread backgroundThread;
  private Map<String, List<Map<String, Object>>> consumerLagInfo;

  public ConsumerLagController(AgentKafkaAdminClient adminClient) {
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
          registerNewConsumerGroups();
          Thread.sleep(5000L);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    this.backgroundThread.setName("Controller-backgroundThread");
    this.backgroundThread.start();

  }

  private synchronized void registerNewConsumerGroups() {

    LOGGER.debug("update new consumer groups if necessary");
    MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();

    try {
      LOGGER.debug("describeConsumerGroups.size= "+adminClient.describeConsumerGroups().size());
      adminClient
          .listConsumerGroups()
          .forEach(new Consumer<ConsumerGroupListing>() {
            @Override
            public void accept(ConsumerGroupListing groupInfo) {
              String objectNameString = "com.purbon.kafka:type=Lag,name=ConsumerLag,groupId="+groupInfo.groupId();
              try {
                ObjectName objectName = new ObjectName(objectNameString);
                try {
                  platformMBeanServer.getObjectInstance(objectName);
                } catch (InstanceNotFoundException ex) {
                  // Instance not found, so it needs registration of new Instance
                  ConsumerLag consumerLag = new ConsumerLag(adminClient, groupInfo.groupId());
                  platformMBeanServer.registerMBean(consumerLag, objectName);
                }

              } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
                LOGGER.error(e);
              }
            }
          });

    } catch (Exception ex) {
      LOGGER.error(ex);
      consumerLagInfo.clear();
    }

  }

}
