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

import static com.github.b0ch3nski.rtla.common.utils.RandomLogFactory.*;
import static com.jayway.restassured.RestAssured.*;
import static com.jayway.restassured.http.ContentType.JSON;

/**
 * @author bochen
 */
public abstract class LogsResourceIT extends SimplifiedLogCassDaoIT {
    private static final String HOST = "localhost";
    private static final int PORT = 9876;
    private static final String HOST_PATH = "hostName";
    private static final String QUERY_PATH = "query";
    private static final String URL = "http://" + HOST + ":" + PORT + "/api/logs/{" + HOST_PATH + "}";

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

    protected LogsResourceIT(CassandraTable table) {
        super(SimplifiedLogCassDaoFactory.createDaoForLevel(getConfig(), table));

        config = RestAssuredConfig.config().objectMapperConfig(
                new ObjectMapperConfig().jackson2ObjectMapperFactory(
                        (cls, charset) -> SerializationHandler.createAndConfigureMapper()));
    }

    private List<SimplifiedLog> retrieveListFromHttp(RequestSpecification requestSpec, int pathParams) {
        String url = (pathParams == 2) ? (URL + "/{" + QUERY_PATH + "}") : URL;

        // @formatter:off
        Response response = given()
                    .pathParam(HOST_PATH, HOST1)
                    .spec(requestSpec)
                    .log().all()
                .expect()
                    .statusCode(200)
                    .contentType(JSON)
                    .log().ifError()
                .when()
                    .get(url);
        // @formatter:on

        return Arrays.asList(response.getBody().as(SimplifiedLog[].class));
    }

    @Override
    public void shouldRetrieveLogsByHost() {
        RequestSpecification requestSpec = new RequestSpecBuilder().build();

        checkLists("host", retrieveListFromHttp(requestSpec, 1));
    }

    @Override
    public void shouldRetrieveLogsByTimestamp() {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addQueryParam("startTime", TIME_MIN)
                .addQueryParam("stopTime", TIME_MAX)
                .addPathParam(QUERY_PATH, "getByTime")
                .build();

        checkLists("time", retrieveListFromHttp(requestSpec, 2));
    }

    @Override
    public void shouldRetrieveLogsByLogger() {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addQueryParam("startTime", TIME_MIN)
                .addQueryParam("stopTime", TIME_MAX)
                .addQueryParam("loggerName", LOGGER1)
                .addPathParam(QUERY_PATH, "getByLogger")
                .build();

        checkLists("logger", retrieveListFromHttp(requestSpec, 2));
    }

    @Override
    public void shouldRetrieveLogsByThread() {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addQueryParam("startTime", TIME_MIN)
                .addQueryParam("stopTime", TIME_MAX)
                .addQueryParam("threadName", THREAD1)
                .addPathParam(QUERY_PATH, "getByThread")
                .build();

        checkLists("thread", retrieveListFromHttp(requestSpec, 2));
    }

    @Override
    public void shouldRetrieveLogsByLoggerAndThread() {
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .addQueryParam("startTime", TIME_MIN)
                .addQueryParam("stopTime", TIME_MAX)
                .addQueryParam("loggerName", LOGGER1)
                .addQueryParam("threadName", THREAD1)
                .addPathParam(QUERY_PATH, "getByLoggerAndThread")
                .build();

        checkLists("logger_thread", retrieveListFromHttp(requestSpec, 2));
    }
}
