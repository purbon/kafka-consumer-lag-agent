package com.purbon.kafka.lag.agent;

import java.io.File;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;

public class AgentTest {

  public static DockerComposeContainer environment =
      new DockerComposeContainer(new File(System.getProperty("user.dir")+"/docker/docker-compose-test.yml"))
          .withExposedService("kafka", 9092)
          .withLocalCompose(true);

  @Test
  public void testFlow() {

    System.out.println(environment.getServiceHost("kafka", 9092));
    Assert.assertEquals(true, true);
  }
}
