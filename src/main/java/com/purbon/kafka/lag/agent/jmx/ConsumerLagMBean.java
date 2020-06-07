package com.purbon.kafka.lag.agent.jmx;

import java.util.List;
import java.util.Map;

public interface ConsumerLagMBean {

  Map<String, Long> getConsumerLag();
}
