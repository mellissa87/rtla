#!/bin/bash

#echo "Building main RTLA packages..."
#mvn clean install -P shade -D dockerimg
#echo "OK! \n"
#sleep 3

#echo "Building Docker images..."
#../dockerfiles/build.sh
#echo "OK! \n"
#sleep 10

echo "Starting RTLA Docker cluster..."
docker-compose -f ../dockerfiles/docker-compose.yml -p rtla up --force-recreate -d
echo "OK! \n"
sleep 180

echo "Scaling Storm Supervisors to 2 instances..."
docker-compose -f ../dockerfiles/docker-compose.yml -p rtla scale storm-supervisor=2
echo "OK! \n"
sleep 10

echo "Updating OpsCenter cluster information..."
../dockerfiles/update-opscenter.sh
echo "\nOK! \n"
sleep 5

if [ -z ${DOCKER_HOST+x} ]; then
    export CASS_IP=$(docker inspect -f '{{ .NetworkSettings.IPAddress }}' cassandra)
    export ZK_IP=$(docker inspect -f '{{ .NetworkSettings.IPAddress }}' zookeeper)
    export NIMBUS_IP=$(docker inspect -f '{{ .NetworkSettings.IPAddress }}' storm-nimbus)
    echo "Local Docker instance found! \n"
else
    export DOCKER_IP=$(echo $DOCKER_HOST | awk -F '/' '{print $3}' | awk -F ':' '{print $1}')
    export CASS_IP=${DOCKER_IP}
    export ZK_IP=${DOCKER_IP}
    export NIMBUS_IP=${DOCKER_IP}
    echo "Using Docker instance at ${DOCKER_IP}\n"
fi

echo "Creating Kafka topics..."
kafka-topics.sh --zookeeper ${ZK_IP}:2181 --create --replication-factor 1 --partition 4 --topic logs
echo "OK! \n"
sleep 5

echo "Importing Cassandra database schema..."
cqlsh ${CASS_IP} 9042 -f rtla-cassandra/src/main/resources/schema.cql
echo "OK! \n"
sleep 10

echo "Importing Storm topology..."
storm jar rtla-storm/target/rtla-storm-1.0.0.jar org.apache.storm.flux.Flux --remote rtla-storm/config/logs-topology.yaml -c nimbus.host=${NIMBUS_IP} -c nimbus.thrift.port=6627
echo "OK! \n"
sleep 10

echo "Building and starting Docker container with simulation data..."
#docker build -t="bochen/rtla-simulation-data-container" local-env/rtla-simulation-data-container
docker run --name rtla-simulation-data-container bochen/rtla-simulation-data-container /bin/true
echo "OK! \n"
sleep 3

echo "Starting 4 simulators..."
for i in {1..4}
do
	echo "\nSimulator ${i}..."
    docker run --name rtla-simulator-${i} --volumes-from rtla-simulation-data-container --link kafka -e LOGDIR_NUM=${i} -e DELAY=500 -e LOOPS=1 -d bochen/rtla-simulator:1.0.0
    echo "OK!"
    sleep 5
done

echo "\n\nAll systems go!"
