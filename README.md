# RTLA
**R**eal **T**ime **L**og **A**nalyzer


### Introduction
Written as a college thesis :)

The main goal of this project is to create real-time, distributed, scalable and
fault tolerant logging solution. It should be also easy to adapt to current
software solutions (Log4J interface). Logs should be available instantly, with
up to 7 days of history (with full text search - 3 days). Taking flexibility
into consideration, system cost vs. features needs to be balanced.

More information about this system can be found in
[rtla-thesis](https://github.com/b0ch3nski/rtla-thesis) repository.


### Technologies currently used
* Docker
* Java 8
* Maven
* Log4J / Logback
* Kryo / Jersey
* Apache Zookeeper
* Apache Kafka
* Apache Storm
* Apache Cassandra
* ElasticSearch
* Kibana
* Grizzly / Jackson


### Requirements
* Shell
* Docker + docker-compose
* JDK 8
* Maven 3+
* ```kafka-topics.sh```, ```cqlsh``` and ```storm``` binaries in **$PATH**


### Launching locally
For local deployment you need my set of **dockerfiles**. As of today, setup
script is assuming that they are placed in parent directory. Also, binaries
listed above should be directly accessible (e.g. in **$PATH**).

* Build all Docker images and all project modules

```./setup-local-env.sh build```

* Start all services, deploy RTLA and start simulators

```./setup-local-env.sh start```

* Follow logs from all services

```./setup-local-env.sh logs```


### Modules
* **rtla-cassandra**

Handles data access and persistence in Cassandra.

* **rtla-common**

Keeps all common code (models, serialization, utilities etc).

* **rtla-elasticsearch**

Handles data access and persistence in ElasticSearch.

* **rtla-kafka**

Implements all Kafka related interfaces required to produce / consume messages.

* **rtla-logback**

Logback plugin implementation that sends logs directly from Log4J interfaces to
Kafka topic.

* **rtla-rest**

HTTP service that serves the data from Cassandra through REST API.

* **rtla-simulator**

Simple simulator that reads example logs from files; used in end-to-end testing
only.

* **rtla-storm**

Creates Storm topology which is consuming logs from Kafka queue and pushing them
further to Cassandra and ElasticSearch.


### Future plans and ideas
* Move to dependency injection concept (Guice vs. Spring)
* Move out from setting TTL in ElasticSearch index properties - create new index
each day and remove old one (get TTL out of ES responsibilities scope)
* Simple JS based web UI + Websockets (SocketIO)
* Move configuration to Apache Zookeeper
* Move deployment from manual Docker setup to Kubernetes
* Stop logs loneliness - gather metrics too :)
