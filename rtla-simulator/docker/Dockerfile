FROM java:8-jre
MAINTAINER bochen

ENV SIM_HOME /opt/rtla-simulator
RUN mkdir -p $SIM_HOME/bin \
    && mkdir $SIM_HOME/conf
WORKDIR $SIM_HOME

COPY config.yml conf/
COPY logback.xml conf/
COPY simulator-config-start.sh bin/
COPY rtla-simulator-1.0.0-shaded.jar bin/

ENTRYPOINT ["bin/simulator-config-start.sh"]
CMD ["java", "-Dlogback.configurationFile=conf/logback.xml", "-jar", "bin/rtla-simulator-1.0.0-shaded.jar", "conf/config.yml"]
