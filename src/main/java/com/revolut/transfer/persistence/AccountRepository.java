package com.revolut.transfer.persistence;

import com.revolut.transfer.model.Account;

public class AccountRepository extends GenericJpaRepository<Account, Long> {

    public AccountRepository() {
        super(Account.class);
    }
}
