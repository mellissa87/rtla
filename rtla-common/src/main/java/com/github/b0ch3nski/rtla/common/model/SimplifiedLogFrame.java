package com.github.b0ch3nski.rtla.common.model;

import com.github.b0ch3nski.rtla.common.serialization.SerializationHandler;
import com.google.common.base.MoreObjects;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author bochen
 */
public final class SimplifiedLogFrame implements SerializableByKryo, InsertableToCass {
    private final long timeStamp;
    private final String hostName;
    private final String level;
    private final byte[] simplifiedLog;

    public SimplifiedLogFrame(long timeStamp, String hostName, String level, byte[] simplifiedLog) {
        this.timeStamp = timeStamp;
        this.hostName = hostName;
        this.level = level;
        this.simplifiedLog = simplifiedLog;
    }

    public SimplifiedLogFrame(byte[] simplifiedLog) {
        this(0L, "", "", simplifiedLog);
    }

    public SimplifiedLogFrame(SimplifiedLog log) {
        timeStamp = log.getTimeStamp();
        hostName = log.getHostName();
        level = log.getLevel();
        simplifiedLog = SerializationHandler.toBytesUsingKryo(log);
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

    public byte[] getSimplifiedLog() {
        return simplifiedLog;
    }

    public int getSimplifiedLogLength() {
        return simplifiedLog.length;
    }

    @Override
    public int getObjectSizeInBytes() {
        return 12 // timeStamp as long + length of simplifiedLog as int
                + hostName.length()
                + level.length()
                + getSimplifiedLogLength();
    }

    @Override
    public String getPartitionKey() {
        return hostName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;
        SimplifiedLogFrame frame = (SimplifiedLogFrame) o;
        return (timeStamp == frame.timeStamp) &&
                (hostName.equals(frame.hostName)) &&
                (level.equals(frame.level)) &&
                Arrays.equals(simplifiedLog, frame.simplifiedLog);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeStamp, hostName, level, simplifiedLog);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("timeStamp", timeStamp)
                .add("hostName", hostName)
                .add("level", level)
                .add("simplifiedLogLength", getSimplifiedLogLength())
                .toString();
    }
}
