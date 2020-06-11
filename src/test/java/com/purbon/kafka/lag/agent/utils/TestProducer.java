package com.purbon.kafka.lag.agent.utils;

import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.Serdes;

public class TestProducer {

  public void produceMessagesToKafka(Properties config, int numberOfMessages, String topic)
      throws ExecutionException, InterruptedException {

    KafkaProducer<String, String> producer = new KafkaProducer<>(config);
    Future<RecordMetadata> response = null;

    for(int i=0; i < numberOfMessages; i++) {
      ProducerRecord<String, String> record = new ProducerRecord<>(topic, "message");
      response = producer.send(record);
    }
    if (response != null) {
      response.get();
    }
    System.out.println(numberOfMessages+" produced.");
  }

  public Properties config(String server) {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
    props.put(ProducerConfig.ACKS_CONFIG, "all");
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, Serdes.String().serializer().getClass());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, Serdes.String().serializer().getClass());
    return props;
  }


}
