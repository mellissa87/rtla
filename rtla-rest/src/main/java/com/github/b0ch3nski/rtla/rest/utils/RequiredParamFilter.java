package com.github.b0ch3nski.rtla.rest.utils;

import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Parameter;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

/**
 * @author bochen
 */
@Provider
public class RequiredParamFilter implements ContainerRequestFilter {

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        for (Parameter parameter : resourceInfo.getResourceMethod().getParameters()) {
            QueryParam queryAnnotation = parameter.getAnnotation(QueryParam.class);

            if ((queryAnnotation != null)
                    && parameter.isAnnotationPresent(Required.class)
                    && !requestContext.getUriInfo().getQueryParameters().containsKey(queryAnnotation.value())
               ) throw new WebApplicationException("Missing parameter " + queryAnnotation.value(), HTTP_BAD_REQUEST);
        }
    }
}
