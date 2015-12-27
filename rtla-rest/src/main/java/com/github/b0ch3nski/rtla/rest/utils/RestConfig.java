package com.github.b0ch3nski.rtla.rest.utils;

import com.google.common.base.MoreObjects;

import java.util.Objects;

/**
 * @author bochen
 */
public class RestConfig {
    private String serverHost;
    private int serverPort;
    private boolean isSSLEnabled;
    private String sslPassword;
    private String cassandraHost;
    private int cassandraPort;

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public boolean isSSLEnabled() {
        return isSSLEnabled;
    }

    public void setIsSSLEnabled(boolean isSSLEnabled) {
        this.isSSLEnabled = isSSLEnabled;
    }

    public String getSslPassword() {
        return sslPassword;
    }

    public void setSslPassword(String sslPassword) {
        this.sslPassword = sslPassword;
    }

    public String getCassandraHost() {
        return cassandraHost;
    }

    public void setCassandraHost(String cassandraHost) {
        this.cassandraHost = cassandraHost;
    }

    public int getCassandraPort() {
        return cassandraPort;
    }

    public void setCassandraPort(int cassandraPort) {
        this.cassandraPort = cassandraPort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o == null) || (getClass() != o.getClass())) return false;
        RestConfig that = (RestConfig) o;
        return (serverPort == that.serverPort) &&
                (isSSLEnabled == that.isSSLEnabled) &&
                (cassandraPort == that.cassandraPort) &&
                (serverHost.equals(that.serverHost)) &&
                (cassandraHost.equals(that.cassandraHost)) &&
                (sslPassword.equals(that.sslPassword));
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverHost, serverPort, isSSLEnabled, cassandraHost, cassandraPort);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("serverHost", serverHost)
                .add("serverPort", serverPort)
                .add("isSSLEnabled", isSSLEnabled)
                .add("cassandraHost", cassandraHost)
                .add("cassandraPort", cassandraPort)
                .toString();
    }
}
