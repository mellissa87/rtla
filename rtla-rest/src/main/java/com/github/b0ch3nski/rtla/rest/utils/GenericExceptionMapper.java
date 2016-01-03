package com.github.b0ch3nski.rtla.rest.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

/**
 * @author bochen
 */
public final class GenericExceptionMapper implements ExceptionMapper<Throwable> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        RestExceptionWrapper toReturn = new RestExceptionWrapper();

        if (exception instanceof WebApplicationException) {
            toReturn.setStatus(((WebApplicationException) exception).getResponse().getStatus());
            toReturn.setMessage(exception.getMessage());
            LOGGER.warn("WebApplicationException was thrown = {}", toReturn);
        } else {
            toReturn.setStatus(INTERNAL_SERVER_ERROR.getStatusCode());
            toReturn.setMessage("Internal Server Error - please report to administrator");
            LOGGER.error("Exception was thrown | returned Internal Server Error [{}]", toReturn.getStatus(), exception);
        }

        return Response
                .status(toReturn.getStatus())
                .entity(toReturn)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private final class RestExceptionWrapper {
        private int status;
        private String message;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = (message != null) ? message : "";
        }

        @Override
        public String toString() {
            return "[" + status + "] [" + message + "]";
        }
    }
}
