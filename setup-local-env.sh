#!/bin/bash

docker build -t="bochen/rtla-simulation-data-container" local-env/rtla-simulation-data-container
docker run --name rtla-simulation-data-container bochen/rtla-simulation-data-container /bin/true

if [ -z ${DOCKER_HOST+x} ]; then
        export CASSIP=$(docker inspect -f '{{ .NetworkSettings.IPAddress }}' cassandra)
else
        export CASSIP=$(echo $DOCKER_HOST | awk -F '/' '{print $3}' | awk -F ':' '{print $1}')
fi

cqlsh $CASSIP 9042 -f rtla-cassandra/schema/schema.cql
