FROM java:8-jre
MAINTAINER bochen

RUN mkdir -p /simulation-data/logs1; \
    mkdir /simulation-data/logs2; \
    mkdir /simulation-data/logs3; \
    mkdir /simulation-data/logs4;

COPY simulation-data/cassandra1.log /simulation-data/logs1/
COPY simulation-data/cassandra2.log /simulation-data/logs2/
COPY simulation-data/cassandra3.log /simulation-data/logs3/
COPY simulation-data/cassandra4.log /simulation-data/logs4/

VOLUME ["/simulation-data"]
