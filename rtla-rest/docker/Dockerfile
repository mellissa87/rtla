FROM java:8-jre
MAINTAINER Piotr Bocheński <bochenski.piotr@gmail.com>

ENV REST_HOME /opt/rtla-rest
RUN mkdir -p $REST_HOME/bin \
    && mkdir $REST_HOME/conf
WORKDIR $REST_HOME

COPY config.yml conf/
COPY rest-config-start.sh bin/
COPY rtla-rest-1.0.0-shaded.jar bin/

EXPOSE 9090
ENTRYPOINT ["bin/rest-config-start.sh"]
CMD ["java", "-jar", "bin/rtla-rest-1.0.0-shaded.jar", "conf/config.yml"]
