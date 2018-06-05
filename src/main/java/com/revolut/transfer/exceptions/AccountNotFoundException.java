package com.revolut.transfer.exceptions;

public class AccountNotFoundException extends ResourceNotFoundException {

    public static final String ACCOUNT_ID = "ACCOUNT_ID";

    @Override
    public String getErrorCode() {
        return "revolut.error.account_not_found";
    }
}
