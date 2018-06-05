package com.revolut.transfer.config;

import com.revolut.transfer.manager.AccountLockManager;
import com.revolut.transfer.manager.AccountManager;
import com.revolut.transfer.persistence.AccountRepository;
import com.revolut.transfer.persistence.TransferTransactionRepository;
import org.glassfish.jersey.server.ResourceConfig;

public class ApplicationResourceConfig extends ResourceConfig {
    public ApplicationResourceConfig() {
        packages("com.revolut.transfer");
        register(new AccountLockManager());
        register(new AccountManager());
        register(new AccountRepository());
        register(new TransferTransactionRepository());
    }
}
