#!/bin/bash

if [ -z ${DOCKER_HOST+x} ]; then
        export CASSIP=$(docker inspect -f '{{ .NetworkSettings.IPAddress }}' cassandra)
else
        export CASSIP=$(echo $DOCKER_HOST | awk -F '/' '{print $3}' | awk -F ':' '{print $1}')
fi

cqlsh $CASSIP 9042 -f ../rtla-cassandra/schema/schema.cql
