#!/usr/bin/env bash

PWD=`pwd`
topic=$1
network=docker_default
acks=all
echo "Write messages to topic $1"

docker run --network $network \
           --volume $PWD/data/my_msgs.txt:/data/my_msgs.txt \
           confluentinc/cp-kafkacat \
           kafkacat -b kafka:9092 \
                    -t $topic \
                    -X acks=$acks \
                    -P -l /data/my_msgs.txt
