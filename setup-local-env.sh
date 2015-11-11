#!/bin/bash

# do we have a local docker?
if [ -z ${DOCKER_HOST+x} ]; then
    export CASS_IP=$(docker inspect -f '{{ .NetworkSettings.IPAddress }}' cassandra)
    export ZK_IP=$(docker inspect -f '{{ .NetworkSettings.IPAddress }}' zookeeper)
else
    export DOCKER_IP=$(echo $DOCKER_HOST | awk -F '/' '{print $3}' | awk -F ':' '{print $1}')
    export CASS_IP=${DOCKER_IP}
    export ZK_IP=${DOCKER_IP}
fi

# create kafka topic
kafka-topics.sh --zookeeper ${ZK_IP}:2181 --create --replication-factor 1 --partition 4 --topic logs
sleep 15

# import cassandra schema
cqlsh ${CASS_IP} 9042 -f rtla-cassandra/schema/schema.cql
sleep 30

# prepare data for simulation
docker build -t="bochen/rtla-simulation-data-container" local-env/rtla-simulation-data-container
docker run --name rtla-simulation-data-container bochen/rtla-simulation-data-container /bin/true
sleep 15

# start simulator
docker run --name rtla-simulator-1 --volumes-from rtla-simulation-data-container --link kafka -e LOGDIR_NUM=1 -e DELAY=500 -e LOOPS=1 -d bochen/rtla-simulator:1.0.0
