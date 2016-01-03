package com.github.b0ch3nski.rtla.rest.resources;

import com.github.b0ch3nski.rtla.cassandra.dao.SimplifiedLogCassDao;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.rest.utils.Required;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 * @author bochen
 */
@Path("/logs/{hostName}")
@Produces(MediaType.APPLICATION_JSON)
public final class LogsResource {

    private final Map<String, SimplifiedLogCassDao> daos;

    @Inject
    public LogsResource(Map<String, SimplifiedLogCassDao> daos) {
        this.daos = daos;
    }

    @GET
    public List<SimplifiedLog> getByHost(@PathParam("hostName") String hostName) {
        return daos.values().parallelStream()
                .flatMap(dao -> dao.getByHost(hostName).stream())
                .collect(Collectors.toList());
    }

    @GET
    @Path("/getByTime")
    public List<SimplifiedLog> getByTime(@PathParam("hostName") String hostName,
                                         @QueryParam("startTime") @Required long startTime,
                                         @QueryParam("stopTime") @Required long stopTime) {
        return daos.values().parallelStream()
                .flatMap(dao -> dao.getByTime(hostName, startTime, stopTime).stream())
                .collect(Collectors.toList());
    }

    @GET
    @Path("/getByLogger")
    public List<SimplifiedLog> getByLogger(@PathParam("hostName") String hostName,
                                           @QueryParam("startTime") @Required long startTime,
                                           @QueryParam("stopTime") @Required long stopTime,
                                           @QueryParam("loggerName") @Required String loggerName) {
        return daos.values().parallelStream()
                .flatMap(dao -> dao.getByLogger(hostName, startTime, stopTime, loggerName).stream())
                .collect(Collectors.toList());
    }

    @GET
    @Path("/getByThread")
    public List<SimplifiedLog> getByThread(@PathParam("hostName") String hostName,
                                           @QueryParam("startTime") @Required long startTime,
                                           @QueryParam("stopTime") @Required long stopTime,
                                           @QueryParam("threadName") @Required String threadName) {
        return daos.values().parallelStream()
                .flatMap(dao -> dao.getByThread(hostName, startTime, stopTime, threadName).stream())
                .collect(Collectors.toList());
    }

    @GET
    @Path("/getByLoggerAndThread")
    public List<SimplifiedLog> getByLoggerAndThread(@PathParam("hostName") String hostName,
                                                    @QueryParam("startTime") @Required long startTime,
                                                    @QueryParam("stopTime") @Required long stopTime,
                                                    @QueryParam("loggerName") @Required String loggerName,
                                                    @QueryParam("threadName") @Required String threadName) {
        return daos.values().parallelStream()
                .flatMap(dao -> dao.getByLoggerAndThread(hostName, startTime, stopTime, loggerName, threadName).stream())
                .collect(Collectors.toList());
    }

    /*
    @GET
    @Path("/query")
    public List<SimplifiedLog> getByHost(@PathParam("hostName") String hostName,
                                         @QueryParam("level") @Required String level) {
        SimplifiedLogCassDao dao = daos.get(level.toUpperCase());
        if (dao == null) throw new WebApplicationException("Unknown log level = " + level, HTTP_BAD_REQUEST);
        return dao.getByHost(hostName);
    }
    */
}
