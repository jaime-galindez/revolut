package com.revolut.transfer.rest.exception;

import com.revolut.transfer.exceptions.NotAcceptableException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NotAcceptableExceptionHandler implements ExceptionMapper<NotAcceptableException> {

    @Override
    public Response toResponse(NotAcceptableException exception) {
        return Response.status(Response.Status.NOT_ACCEPTABLE).entity(exception.getError()).build();
    }
}
