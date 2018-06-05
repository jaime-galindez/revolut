package com.revolut.transfer.persistence;

import com.revolut.transfer.model.TransferTransaction;

public class TransferTransactionRepository extends GenericJpaRepository<TransferTransaction, Long> {

    public TransferTransactionRepository() {
        super(TransferTransaction.class);
    }
}
