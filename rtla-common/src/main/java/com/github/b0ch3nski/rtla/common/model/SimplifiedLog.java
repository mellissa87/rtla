package com.github.b0ch3nski.rtla.common.model;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.b0ch3nski.rtla.common.utils.Validators;
import com.google.common.base.Preconditions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * @author bochen
 */
public final class SimplifiedLog implements SerializableByKryo {
    private final long timeStamp;
    private final String hostName;
    private final String level;
    private final String threadName;
    private final String loggerName;
    private final String formattedMessage;

    private SimplifiedLog(SimplifiedLogBuilder builder) {
        timeStamp = builder.timeStamp;
        hostName = builder.hostName;
        level = builder.level;
        threadName = builder.threadName;
        loggerName = builder.loggerName;
        formattedMessage = builder.formattedMessage;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getHostName() {
        return hostName;
    }

    public String getLevel() {
        return level;
    }

    public String getThreadName() {
        return threadName;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public String getFormattedMessage() {
        return formattedMessage;
    }

    @Override
    @JsonIgnore
    public int getObjectSizeInBytes() {
        return 9 // timestamp is long
                + hostName.length()
                + level.length()
                + threadName.length()
                + loggerName.length()
                + formattedMessage.length();
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || (obj.getClass() != getClass())) return false;
        if (obj == this) return true;
        SimplifiedLog log = (SimplifiedLog) obj;
        return (timeStamp == log.timeStamp) &&
                (hostName.equals(log.hostName)) &&
                (level.equals(log.level)) &&
                (threadName.equals(log.threadName)) &&
                (loggerName.equals(log.loggerName)) &&
                (formattedMessage.equals(log.formattedMessage));
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeStamp, hostName, level, threadName, loggerName, formattedMessage);
    }

    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        return "[" + hostName + "] [" + dateFormat.format(new Date(timeStamp)) + "] [" + level +
                "] [" + threadName + "] [" + loggerName + "] " + formattedMessage;
    }


    public static final class SimplifiedLogBuilder {
        private long timeStamp;
        private String hostName;
        private String level;
        private String threadName;
        private String loggerName;
        private String formattedMessage;

        public SimplifiedLogBuilder fromILoggingEvent(ILoggingEvent event) {
            Preconditions.checkNotNull(event, "ILoggingEvent cannot be null!");
            timeStamp = event.getTimeStamp();
            level = event.getLevel().toString();
            threadName = event.getThreadName();
            loggerName = event.getLoggerName();
            formattedMessage = event.getFormattedMessage();
            return this;
        }

        public SimplifiedLogBuilder withTimeStamp(long timeStamp) {
            this.timeStamp = timeStamp;
            return this;
        }

        public SimplifiedLogBuilder withHostName(String hostName) {
            this.hostName = hostName;
            return this;
        }

        public SimplifiedLogBuilder withLevel(String level) {
            this.level = level;
            return this;
        }

        public SimplifiedLogBuilder withThreadName(String threadName) {
            this.threadName = threadName;
            return this;
        }

        public SimplifiedLogBuilder withLoggerName(String loggerName) {
            this.loggerName = loggerName;
            return this;
        }

        public SimplifiedLogBuilder withFormattedMessage(String formattedMessage) {
            this.formattedMessage = formattedMessage;
            return this;
        }

        public SimplifiedLog build() {
            Validators.isNotNullOrEmpty(hostName, "hostName");
            Validators.isNotNullOrEmpty(level, "level");
            Validators.isNotNullOrEmpty(threadName, "threadName");
            Validators.isNotNullOrEmpty(loggerName, "loggerName");
            Validators.isNotNullOrEmpty(formattedMessage, "formattedMessage");
            return new SimplifiedLog(this);
        }
    }
}
