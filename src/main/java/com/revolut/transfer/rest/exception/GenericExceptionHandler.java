package com.revolut.transfer.rest.exception;

import com.revolut.transfer.exceptions.ResourceNotFoundException;
import com.revolut.transfer.model.ApplicationError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Arrays;
import java.util.stream.Collectors;

@Provider
public class GenericExceptionHandler implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {

        ApplicationError error = new ApplicationError();
        error.setCode("revolut.error.generic");
        error.setStacktrace(Arrays.stream(exception.getStackTrace()).map(ste -> ste.toString()).collect(Collectors.joining(", ")));
        error.setClassName(exception.getClass().getName());

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
    }
}
