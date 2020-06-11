package com.purbon.kafka.lag.agent.utils;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Serdes;

public class TestConsumer {

  public void consumerAllMessagesFromBeginning(Properties config, String topic, long time) {

    KafkaConsumer<String, String> consumer = new KafkaConsumer<>(config);
    consumer.subscribe(Collections.singleton(topic));
    Thread thread = new Thread(() -> {
      try {
        while (true) {
          ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));
          consumer.commitSync();
          System.out.println(records.count()+" consumed.");
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    thread.start();
    try {
      thread.join(time);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public Properties config(String server) {
    Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, Serdes.String().deserializer().getClass());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, Serdes.String().deserializer().getClass());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "my.group");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    return props;
  }


}
