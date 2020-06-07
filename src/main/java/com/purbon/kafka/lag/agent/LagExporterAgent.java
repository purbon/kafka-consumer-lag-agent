package com.purbon.kafka.lag.agent;

import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

public class LagExporterAgent {

  private static final Logger LOGGER = LogManager.getLogger(LagExporterAgent.class);

  public static void premain(String args, Instrumentation inst) {

    List<String> argsList = Arrays
        .asList(args.split(","))
        .stream()
        .map(s -> s.trim()).collect(Collectors.toList());

    main(argsList);
  }

  public static void agentmain(String args, Instrumentation inst) {
    premain(args, inst);
  }

  private static void main(List<String> args) {
    Configurator.initialize(null, "log4j2.properties");

    LOGGER.info("[Agent] In main method with "+args);
    registerBeans();
    startBackgroundControllerThread();

  }

  private static void registerBeans() {
    AgentKafkaAdminClient adminClient = new AgentKafkaAdminClient(props());
    LagExporter exporter = new LagExporter(adminClient);
    exporter.registerConsumerLagBean();
  }

  private static Properties props() {
    Properties props = new Properties();
    props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, Serdes.String().deserializer().getClass());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Serdes.String().deserializer().getClass());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "my.group");
    return props;
  }

  private static void startBackgroundControllerThread() {
    AgentKafkaAdminClient adminClient = new AgentKafkaAdminClient(props());
    ConsumerLagController controller = new ConsumerLagController(adminClient);
    controller.startBackgroundThread();
  }
}
