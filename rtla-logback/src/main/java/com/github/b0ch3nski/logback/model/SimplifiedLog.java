package com.github.b0ch3nski.logback.model;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

/**
 * @author bochen
 */
public final class SimplifiedLog implements Serializable {
    private static final long serialVersionUID = 5726328967486071970L;
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
    public boolean equals(Object obj) {
        if ((obj == null) || (obj.getClass() != getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        SimplifiedLog that = (SimplifiedLog) obj;
        return Objects.equals(timeStamp, that.timeStamp) &&
                Objects.equals(hostName, that.hostName) &&
                Objects.equals(level, that.level) &&
                Objects.equals(threadName, that.threadName) &&
                Objects.equals(loggerName, that.loggerName) &&
                Objects.equals(formattedMessage, that.formattedMessage);
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

        private void validate(String toValidate, String varName) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(toValidate), varName + " cannot be null or empty!");
        }

        public SimplifiedLog build() {
            validate(hostName, "hostName");
            validate(level, "level");
            validate(threadName, "threadName");
            validate(loggerName, "loggerName");
            validate(formattedMessage, "formattedMessage");
            return new SimplifiedLog(this);
        }
    }
}
