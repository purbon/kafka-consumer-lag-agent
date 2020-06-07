#!/usr/bin/env bash

PWD=`pwd`
topic=$1
network=docker_default

echo "listing topics"

docker run --network $network \
           confluentinc/cp-kafkacat \
           kafkacat -b kafka:9092 \
                    -L
