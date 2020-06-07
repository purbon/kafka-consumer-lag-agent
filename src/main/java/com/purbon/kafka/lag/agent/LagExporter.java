package com.purbon.kafka.lag.agent;

import com.purbon.kafka.lag.agent.jmx.GlobalConsumerLag;
import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import javax.management.ObjectName;

public class LagExporter {

  private final AgentKafkaAdminClient adminClient;
  private final ConsumerLagController consumerLagController;

  public LagExporter(AgentKafkaAdminClient adminClient) {
    this.adminClient = adminClient;
    this.consumerLagController = new ConsumerLagController(adminClient);
  }

  public void registerConsumerLagBean() {

    GlobalConsumerLag consumeLag = new GlobalConsumerLag(adminClient);
    MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
    try {
      String objectNameString = "com.purbon.kafka:type=Lag,name=ConsumerLag";
      ObjectName objectName = new ObjectName(objectNameString);
      platformMBeanServer.registerMBean(consumeLag, objectName);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}
