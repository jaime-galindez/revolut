package com.revolut.transfer.exceptions;

import com.revolut.transfer.model.ApplicationError;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class RevolutException extends RuntimeException {

    private Map<String, Object> properties = new HashMap<>();

    public RevolutException() {
        this.properties = properties;
    }

    public RevolutException(String message) {
        super(message);
        this.properties = properties;
    }

    public RevolutException(String message, Throwable cause) {
        super(message, cause);
        this.properties = properties;
    }

    public RevolutException(Throwable cause) {
        super(cause);
        this.properties = properties;
    }

    public RevolutException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.properties = properties;
    }

    public RevolutException withProperty(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    public RevolutException withProperties(Object... data) {
        for (int i = 0; i < data.length; i += 2) {
            properties.put(((String) data[i]), data[i + 1]);
        }
        return this;
    }

    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public abstract String getErrorCode();

    public ApplicationError getError() {
        ApplicationError error = new ApplicationError();

        error.setCode(getErrorCode());
        error.setClassName(getClass().getName());
        error.setStacktrace(Arrays.stream(getStackTrace()).map(ste -> ste.toString()).collect(Collectors.joining(",")));
        error.setProperties(getProperties());

        return error;
    }

}
