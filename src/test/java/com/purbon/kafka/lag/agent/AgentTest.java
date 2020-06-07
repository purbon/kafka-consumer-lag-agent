package com.purbon.kafka.lag.agent;

import java.io.File;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.DockerComposeContainer;

public class AgentTest {

  @ClassRule
  public static DockerComposeContainer environment =
      new DockerComposeContainer(new File("docker/docker-compose-test.yml"))
          .withLocalCompose(true);

  @Test
  public void testFlow() {

    Assert.assertEquals(true, true);
  }
}
