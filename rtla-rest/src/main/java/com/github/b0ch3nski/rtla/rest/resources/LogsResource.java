package com.github.b0ch3nski.rtla.rest.resources;

import com.github.b0ch3nski.rtla.cassandra.dao.SimplifiedLogCassDao;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.rest.utils.Required;
import com.google.common.base.Strings;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 * @author bochen
 */
@Path("/logs")
@Produces(MediaType.APPLICATION_JSON)
public final class LogsResource {

    private final Map<String, SimplifiedLogCassDao> daos;

    @Inject
    public LogsResource(Map<String, SimplifiedLogCassDao> daos) {
        this.daos = daos;
    }

    private List<SimplifiedLog> querySpecifiedDao(String level, Function<SimplifiedLogCassDao, Stream<SimplifiedLog>> function) {
        SimplifiedLogCassDao dao = daos.get(level.toUpperCase());
        if (dao == null) throw new WebApplicationException("Unknown log level = " + level, HTTP_BAD_REQUEST);
        return function
                .apply(dao)
                .collect(Collectors.toList());
    }

    private List<SimplifiedLog> queryAllDaos(Function<SimplifiedLogCassDao, Stream<SimplifiedLog>> function) {
        return daos.values()
                .parallelStream()
                .flatMap(function)
                .collect(Collectors.toList());
    }

    private List<SimplifiedLog> handleDaoQuery(String level, Function<SimplifiedLogCassDao, Stream<SimplifiedLog>> function) {
        if (!Strings.isNullOrEmpty(level)) return querySpecifiedDao(level, function);
        else return queryAllDaos(function);
    }

    @GET
    @Path("/query")
    public List<SimplifiedLog> query(@QueryParam("hostName") @Required String hostName,
                                     @Context UriInfo uriInfo) {
        MultivaluedMapWrapper<String, String> params = new MultivaluedMapWrapper<>(uriInfo.getQueryParameters());

        String level = params.getFirst("level");
        long startTime = parseLongOrZero(params.getFirst("startTime"));
        long stopTime = parseLongOrZero(params.getFirst("stopTime"));
        String loggerName = params.getFirst("loggerName");
        String threadName = params.getFirst("threadName");

        if (params.containsKeys("startTime", "stopTime", "loggerName", "threadName"))
            return handleDaoQuery(level, dao -> dao.getByLoggerAndThread(hostName, startTime, stopTime, loggerName, threadName).stream());
        if (params.containsKeys("startTime", "stopTime", "threadName"))
            return handleDaoQuery(level, dao -> dao.getByThread(hostName, startTime, stopTime, threadName).stream());
        if (params.containsKeys("startTime", "stopTime", "loggerName"))
            return handleDaoQuery(level, dao -> dao.getByLogger(hostName, startTime, stopTime, loggerName).stream());
        if (params.containsKeys("startTime", "stopTime"))
            return handleDaoQuery(level, dao -> dao.getByTime(hostName, startTime, stopTime).stream());
        return handleDaoQuery(level, dao -> dao.getByHost(hostName).stream());
    }

    private long parseLongOrZero(String toParse) {
        try {
            return Long.parseLong(toParse);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    private class MultivaluedMapWrapper<K, V> extends AbstractMultivaluedMap<K, V> {
        public MultivaluedMapWrapper(Map<K, List<V>> store) {
            super(store);
        }

        @SafeVarargs
        public final boolean containsKeys(K... keys) {
            boolean toReturn = true;
            for (K key : keys) toReturn = toReturn && (getFirst(key) != null);
            return toReturn;
        }
    }
}
