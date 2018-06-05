package com.revolut.transfer;


import com.revolut.transfer.model.Account;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import static com.revolut.transfer.RestCallHelper.createAccount;
import static com.revolut.transfer.RestCallHelper.getAccount;
import static com.revolut.transfer.RestCallHelper.transfer;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * To be run manually. It needs the server started
 */
public class TransferMoneyBetweenAccountsTestIT {

    @Test
    public void testSimpleMoneyTransfer() throws Exception {
        final BigDecimal originAccountInitialAmount = new BigDecimal("1000.00");
        final BigDecimal destinationAccountInitialAmount = new BigDecimal("2000.00");
        final BigDecimal amountToTransfer = new BigDecimal("234.56");
        final BigDecimal originFinalAmount = originAccountInitialAmount.subtract(amountToTransfer);
        final BigDecimal destinationFinalAmount = destinationAccountInitialAmount.add(amountToTransfer);
        RestCallHelper.cleanAll();
        Account originAccount = createAccount(originAccountInitialAmount);
        Account destinationAccount = createAccount(destinationAccountInitialAmount);

        originAccount = getAccount(originAccount.getId());

        assertThat(originAccount, is(notNullValue()));
        assertThat(originAccount.getAmount(), is(equalTo(originAccountInitialAmount)));

        destinationAccount = getAccount(destinationAccount.getId());

        assertThat(destinationAccount, is(notNullValue()));
        assertThat(destinationAccount.getAmount(), is(equalTo(destinationAccountInitialAmount)));

        transfer(originAccount.getId(), destinationAccount.getId(), amountToTransfer);

        originAccount = getAccount(originAccount.getId());
        destinationAccount = getAccount(destinationAccount.getId());

        assertThat(originAccount, is(notNullValue()));
        assertThat(originAccount.getAmount(), is(equalTo(originFinalAmount)));

        assertThat(destinationAccount, is(notNullValue()));
        assertThat(destinationAccount.getAmount(), is(equalTo(destinationFinalAmount)));
    }

    @Test
    public void testConcurrentMoneyTransfer() throws Exception {
        final BigDecimal originAccountInitialAmount = new BigDecimal("1000.00");
        final BigDecimal destinationAccountInitialAmount = new BigDecimal("2000.00");
        final BigDecimal amountToTransfer = new BigDecimal("0.56");
        final int numberOfTransfers = 1000;
        BigDecimal originFinalAmount = originAccountInitialAmount;
        BigDecimal destinationFinalAmount = destinationAccountInitialAmount;
        CompletableFuture[] parallelExecutions = new CompletableFuture[numberOfTransfers];

        RestCallHelper.cleanAll();
        Account originAccount = createAccount(originAccountInitialAmount);
        Account destinationAccount = createAccount(destinationAccountInitialAmount);
        final Long originAccountId = originAccount.getId();
        final Long destinationAccountId = destinationAccount.getId();


        originAccount = getAccount(originAccount.getId());

        assertThat(originAccount, is(notNullValue()));
        assertThat(originAccount.getAmount(), is(equalTo(originAccountInitialAmount)));

        destinationAccount = getAccount(destinationAccount.getId());

        assertThat(destinationAccount, is(notNullValue()));
        assertThat(destinationAccount.getAmount(), is(equalTo(destinationAccountInitialAmount)));

        for (int i = 0; i < numberOfTransfers; i++) {
            originFinalAmount = originFinalAmount.subtract(amountToTransfer);
            destinationFinalAmount = destinationFinalAmount.add(amountToTransfer);
            parallelExecutions[i] = CompletableFuture.runAsync(() -> {
                try {
                    transfer(originAccountId, destinationAccountId, amountToTransfer);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(parallelExecutions);
        combinedFuture.join();


        originAccount = getAccount(originAccount.getId());
        destinationAccount = getAccount(destinationAccount.getId());

        assertThat(originAccount, is(notNullValue()));
        assertThat(originAccount.getAmount(), is(equalTo(originFinalAmount)));

        assertThat(destinationAccount, is(notNullValue()));
        assertThat(destinationAccount.getAmount(), is(equalTo(destinationFinalAmount)));
    }

    @Test
    public void testConcurrentMoneyTransferSwitchingAccounts() throws Exception {
        final BigDecimal originAccountInitialAmount = new BigDecimal("1000.00");
        final BigDecimal destinationAccountInitialAmount = new BigDecimal("2000.00");
        final BigDecimal amountToTransfer = new BigDecimal("0.56");
        final int numberOfTransfers = 1000;
        BigDecimal originFinalAmount = originAccountInitialAmount;
        BigDecimal destinationFinalAmount = destinationAccountInitialAmount;
        CompletableFuture[] parallelExecutions = new CompletableFuture[numberOfTransfers];

        RestCallHelper.cleanAll();
        Account originAccount = createAccount(originAccountInitialAmount);
        Account destinationAccount = createAccount(destinationAccountInitialAmount);
        final Long originAccountId = originAccount.getId();
        final Long destinationAccountId = destinationAccount.getId();


        originAccount = getAccount(originAccount.getId());

        assertThat(originAccount, is(notNullValue()));
        assertThat(originAccount.getAmount(), is(equalTo(originAccountInitialAmount)));

        destinationAccount = getAccount(destinationAccount.getId());

        assertThat(destinationAccount, is(notNullValue()));
        assertThat(destinationAccount.getAmount(), is(equalTo(destinationAccountInitialAmount)));

        for (int i = 0; i < numberOfTransfers; i++) {
            if (i % 2 == 0) {
                originFinalAmount = originFinalAmount.subtract(amountToTransfer);
                destinationFinalAmount = destinationFinalAmount.add(amountToTransfer);
                parallelExecutions[i] = CompletableFuture.runAsync(() -> {
                    try {
                        transfer(originAccountId, destinationAccountId, amountToTransfer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                originFinalAmount = originFinalAmount.add(amountToTransfer);
                destinationFinalAmount = destinationFinalAmount.subtract(amountToTransfer);
                parallelExecutions[i] = CompletableFuture.runAsync(() -> {
                    try {
                        transfer(destinationAccountId, originAccountId, amountToTransfer);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            }
        }

        CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(parallelExecutions);
        combinedFuture.join();


        originAccount = getAccount(originAccount.getId());
        destinationAccount = getAccount(destinationAccount.getId());

        assertThat(originAccount, is(notNullValue()));
        assertThat(originAccount.getAmount(), is(equalTo(originFinalAmount)));

        assertThat(destinationAccount, is(notNullValue()));
        assertThat(destinationAccount.getAmount(), is(equalTo(destinationFinalAmount)));
    }

}
