#!/usr/bin/env bash

PWD=`pwd`
topic=$1
network=docker_default
echo "Reading messages to topic $1"

docker run  --tty --interactive --rm \
            --network $network \
           confluentinc/cp-kafkacat \
           kafkacat -b kafka:9092 \
              -C \
              -f '\nKey (%K bytes): %k\t\nValue (%S bytes): %s\n\Partition: %p\tOffset: %o\n--\n' \
              -G 2mygroup $topic
