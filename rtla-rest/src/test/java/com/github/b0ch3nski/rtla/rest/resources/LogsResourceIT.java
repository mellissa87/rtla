package com.github.b0ch3nski.rtla.rest.resources;

import com.github.b0ch3nski.rtla.cassandra.CassandraTable;
import com.github.b0ch3nski.rtla.cassandra.dao.SimplifiedLogCassDaoFactory;
import com.github.b0ch3nski.rtla.cassandra.dao.SimplifiedLogCassDaoIT;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.serialization.SerializationHandler;
import com.github.b0ch3nski.rtla.rest.RestServer;
import com.github.b0ch3nski.rtla.rest.utils.RestConfig;
import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.config.ObjectMapperConfig;
import com.jayway.restassured.config.RestAssuredConfig;
import com.jayway.restassured.response.Response;
import com.jayway.restassured.specification.RequestSpecification;
import org.junit.ClassRule;
import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.github.b0ch3nski.rtla.cassandra.CassandraTable.INFO;
import static com.github.b0ch3nski.rtla.common.utils.RandomLogFactory.*;
import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.http.ContentType.JSON;

/**
 * @author bochen
 */
public final class LogsResourceIT extends SimplifiedLogCassDaoIT {
    private static final CassandraTable TABLE = INFO;
    private static final String HOST = "localhost";
    private static final int PORT = 9876;
    private static final String URL = "http://" + HOST + ":" + PORT + "/api/logs/query";

    @ClassRule
    public static final ExternalResource REST_RESOURCE = new ExternalResource() {
        private RestServer server;

        private RestConfig createRestConfig() {
            RestConfig config = new RestConfig();
            config.setServerHost(HOST);
            config.setServerPort(PORT);
            config.setIsSSLEnabled(false);
            config.setCassandraHost(getConfig().getHost());
            config.setCassandraPort(getConfig().getPort());
            return config;
        }

        @Override
        protected void before() throws IOException {
            server = new RestServer(createRestConfig());
            server.start();
        }

        @Override
        protected void after() {
            server.shutdown();
        }
    };

    public LogsResourceIT() {
        super(SimplifiedLogCassDaoFactory.createDaoForLevel(getConfig(), TABLE));

        config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory(
                        (cls, charset) -> SerializationHandler.createAndConfigureMapper()));
    }

    private List<SimplifiedLog> retrieveListFromHttp(RequestSpecification requestSpec) {
        // @formatter:off
        Response response = given()
                    .queryParam("hostName", HOST1)
                    .queryParam("level", TABLE.name())
                    .spec(requestSpec)
                    .log().all()
                .expect()
                    .statusCode(200)
                    .contentType(JSON)
                    .log().ifError()
                .when()
                    .get(URL);
        // @formatter:on

        return Arrays.asList(response.getBody().as(SimplifiedLog[].class));
    }

    @Override
    public void shouldRetrieveLogsByHost() {
        RequestSpecification requestSpec = new RequestSpecBuilder().build();

        checkLists("host", retrieveListFromHttp(requestSpec));
    }

    @Override
    public void shouldRetrieveLogsByTimestamp() {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addQueryParam("startTime", TIME_MIN)
                .addQueryParam("stopTime", TIME_MAX)
                .build();

        checkLists("time", retrieveListFromHttp(requestSpec));
    }

    @Override
    public void shouldRetrieveLogsByLogger() {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addQueryParam("startTime", TIME_MIN)
                .addQueryParam("stopTime", TIME_MAX)
                .addQueryParam("loggerName", LOGGER1)
                .build();

        checkLists("logger", retrieveListFromHttp(requestSpec));
    }

    @Override
    public void shouldRetrieveLogsByThread() {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addQueryParam("startTime", TIME_MIN)
                .addQueryParam("stopTime", TIME_MAX)
                .addQueryParam("threadName", THREAD1)
                .build();

        checkLists("thread", retrieveListFromHttp(requestSpec));
    }

    @Override
    public void shouldRetrieveLogsByLoggerAndThread() {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addQueryParam("startTime", TIME_MIN)
                .addQueryParam("stopTime", TIME_MAX)
                .addQueryParam("loggerName", LOGGER1)
                .addQueryParam("threadName", THREAD1)
                .build();

        checkLists("logger_thread", retrieveListFromHttp(requestSpec));
    }
}
