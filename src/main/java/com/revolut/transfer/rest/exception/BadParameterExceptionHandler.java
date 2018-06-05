package com.revolut.transfer.rest.exception;

import com.revolut.transfer.exceptions.BadParameterException;
import com.revolut.transfer.exceptions.NotAcceptableException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class BadParameterExceptionHandler implements ExceptionMapper<BadParameterException> {

    @Override
    public Response toResponse(BadParameterException exception) {
        return Response.status(Response.Status.BAD_REQUEST).entity(exception.getError()).build();
    }
}
