package com.github.b0ch3nski.rtla.rest.resource;

import com.github.b0ch3nski.rtla.common.model.SimplifiedLog;
import com.github.b0ch3nski.rtla.common.utils.RandomLogFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * @author bochen
 */
@Path("/test")
public final class TestResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SimplifiedLog getTest() {
        return RandomLogFactory.create();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getTest(@PathParam("id") int id) {
        return "Test " + id;
    }
}
