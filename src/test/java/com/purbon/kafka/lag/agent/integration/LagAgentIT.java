package com.purbon.kafka.lag.agent.integration;

import static org.hamcrest.MatcherAssert.assertThat;

import com.purbon.kafka.lag.agent.JMXClient;
import com.purbon.kafka.lag.agent.jmx.GlobalConsumerLagMBean;
import java.io.IOException;
import javax.management.MalformedObjectNameException;
import org.junit.Test;

public class LagAgentIT extends BaseIntegrationTest {

  @Test
  public void agentLoads() throws IOException, MalformedObjectNameException, InterruptedException {

    Process app = buildAndStartInternalTestAppProcess();

    // Wait for application to start
    app.getInputStream().read();

    JMXClient client = new JMXClient();
    client.connect("", 9999);

    GlobalConsumerLagMBean bean = (GlobalConsumerLagMBean)client.query("com.purbon.kafka:type=Lag,name=ConsumerLag", GlobalConsumerLagMBean.class);
    assertThat("There should be no consumers", bean.getConsumerLagByPartition().size() == 0);

    stopInternalApp(app);
  }

}
