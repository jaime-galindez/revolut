package com.revolut.transfer.exceptions;

public class InsufficientFundsException extends NotAcceptableException {
    public static final String ACCOUNT_ID = "ACCOUNT_ID";
    public static final String CURRENT_FUNDS = "CURRENT_FUNDS";

    @Override
    public String getErrorCode() {
        return "revolut.error.insufficient_funds";
    }
}
