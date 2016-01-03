package com.github.b0ch3nski.rtla.rest.resources;

import com.github.b0ch3nski.rtla.cassandra.dao.SimplifiedLogCassDao;
import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @GET
    @Path("/{hostName}")
    public List<SimplifiedLog> getByHost(@PathParam("hostName") String hostName) {
        return daos.values().parallelStream()
                .flatMap(dao -> dao.getByHost(hostName).stream())
                .collect(Collectors.toList());
    }

    @GET
    @Path("{hostName}/{level}")
    public List<SimplifiedLog> getByHost(@PathParam("hostName") String hostName,
                                         @PathParam("level") String level) {
        SimplifiedLogCassDao dao = daos.get(level.toUpperCase());
        if (dao == null)
            throw new WebApplicationException(
                    Response.status(HttpURLConnection.HTTP_BAD_REQUEST)
                            .entity("Unknown log level = " + level)
                            .build()
            );
        return dao.getByHost(hostName);
    }
}
