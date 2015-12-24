package com.github.b0ch3nski.rtla.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog.SimplifiedLogBuilder;
import com.github.b0ch3nski.rtla.common.utils.Validators;
import com.github.b0ch3nski.rtla.kafka.KafkaUtils;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;

import static com.github.b0ch3nski.rtla.kafka.KafkaUtils.KafkaProducerType.ASYNC;

/**
 * @author bochen
 */
public final class KafkaAppender extends AppenderBase<ILoggingEvent> {
    private String hostName;
    private String brokers;
    private String topic;
    private boolean requireAcks;
    private Producer<String, SimplifiedLog> producer;


    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getBrokers() {
        return brokers;
    }

    public void setBrokers(String brokers) {
        this.brokers = brokers;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public boolean isRequireAcks() {
        return requireAcks;
    }

    public void setRequireAcks(boolean requireAcks) {
        this.requireAcks = requireAcks;
    }

    @Override
    public void start() {
        Validators.isNotNullOrEmpty(hostName, "host name");
        Validators.isNotNullOrEmpty(brokers, "broker list");
        Validators.isNotNullOrEmpty(topic, "topic name");

        producer = KafkaUtils.createProducer(brokers, ASYNC, requireAcks);
        super.start();
    }

    @Override
    protected void append(ILoggingEvent event) {
        SimplifiedLog log = new SimplifiedLogBuilder()
                .fromILoggingEvent(event)
                .withHostName(hostName)
                .build();

        producer.send(new KeyedMessage<>(topic, hostName, log));
    }

    @Override
    public void stop() {
        if (producer != null) producer.close();
        super.stop();
    }
}
