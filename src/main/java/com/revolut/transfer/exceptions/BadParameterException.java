package com.revolut.transfer.exceptions;

/**
 * This exception will be translated to a 400 BAD REQUEST response by the REST layer
 */
public class BadParameterException extends RevolutException {

    public final static String PARAMETER_NAME = "PARAMETER_NAME";
    public final static String EXPECTED_CONDITION = "EXPECTED_CONDITION";
    @Override
    public String getErrorCode() {
        return "revolut.error.bad_parameter";
    }
}
