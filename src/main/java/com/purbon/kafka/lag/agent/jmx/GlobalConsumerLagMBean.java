package com.purbon.kafka.lag.agent.jmx;

import java.util.List;
import java.util.Map;

public interface GlobalConsumerLagMBean {

  Map<String, List<Map<String, Object>>> getConsumerLagByPartition();
}
