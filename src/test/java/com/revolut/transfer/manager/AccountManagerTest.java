package com.revolut.transfer.manager;

import com.revolut.transfer.exceptions.AccountNotFoundException;
import com.revolut.transfer.exceptions.BadParameterException;
import com.revolut.transfer.exceptions.InsufficientFundsException;
import com.revolut.transfer.model.Account;
import com.revolut.transfer.model.AccountLockingSession;
import com.revolut.transfer.model.TransferTransaction;
import com.revolut.transfer.persistence.AccountRepository;
import com.revolut.transfer.persistence.TransferTransactionRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AccountManagerTest {

    @Mock
    private AccountLockManager accountLockManager;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransferTransactionRepository transferTransactionRepository;

    private AccountManager accountManager;

    @Before
    public void init() {
        accountManager = new AccountManager(accountLockManager, accountRepository, transferTransactionRepository);
    }

    @Test
    public void testSimpleCase() {
        final Lock lock1 = new ReentrantLock();
        final Lock lock2 = new ReentrantLock();
        final AccountLockingSession accountLockingSession = new AccountLockingSession(lock1, lock2);
        final Long originAccountId = 1L;
        final Long destinationAccountId = 2L;
        final BigDecimal amount = new BigDecimal(123.15);
        final BigDecimal originAccountAmount = new BigDecimal(1000.0);
        final BigDecimal destinationAccountAmount = new BigDecimal(1000.0);
        final Account originAccount = new Account();
        originAccount.setId(originAccountId);
        originAccount.setAmount(originAccountAmount);
        final Account destinationAccount = new Account();
        destinationAccount.setId(destinationAccountId);
        destinationAccount.setAmount(destinationAccountAmount);
        final BigDecimal expectedOriginAmount = originAccountAmount.subtract(amount);
        final BigDecimal expectedDestinationAmount = destinationAccountAmount.add(amount);

        when(accountLockManager.getOrCreateAccountLocks(eq(originAccountId), eq(destinationAccountId))).thenReturn(accountLockingSession);
        when(accountRepository.findById(eq(originAccountId))).thenReturn(originAccount);
        when(accountRepository.findById(eq(destinationAccountId))).thenReturn(destinationAccount);

        accountManager.transfer(originAccountId, destinationAccountId, amount);

        assertThat(originAccount.getAmount(), is(equalTo(expectedOriginAmount)));
        assertThat(destinationAccount.getAmount(), is(equalTo(expectedDestinationAmount)));

        ArgumentCaptor<TransferTransaction> transferTransactionArgumentCaptor = ArgumentCaptor.forClass(TransferTransaction.class);
        verify(accountRepository, times(1)).startTransaction();
        verify(accountRepository, times(1)).commitTransaction();
        verify(transferTransactionRepository, times(1)).save(transferTransactionArgumentCaptor.capture());

        TransferTransaction transferTransaction = transferTransactionArgumentCaptor.getValue();

        assertThat(transferTransaction.getAmount(), is(equalTo(amount)));
        assertThat(transferTransaction.getDestinationAccount(), is(equalTo(destinationAccount)));
        assertThat(transferTransaction.getOriginAccount(), is(equalTo(originAccount)));
    }

    @Test(expected = BadParameterException.class)
    public void testOriginAccountIdIsNull() {
        accountManager.transfer(null, 1L, new BigDecimal(10.0));
    }

    @Test(expected = BadParameterException.class)
    public void testDestinationAccountIdIsNull() {
        accountManager.transfer(1L, null, new BigDecimal(10.0));
    }

    @Test(expected = BadParameterException.class)
    public void testAmountIsNull() {
        accountManager.transfer(1L, 2L, null);
    }

    @Test(expected = BadParameterException.class)
    public void testAccountIdsAreEquals() {
        accountManager.transfer(1L, 1L, new BigDecimal(10.0));
    }

    @Test(expected = BadParameterException.class)
    public void testAMountIsNegative() {
        accountManager.transfer(1L, 2L, new BigDecimal(-10.0));
    }

    @Test(expected = BadParameterException.class)
    public void testAMountIsZero() {
        accountManager.transfer(1L, 2L, new BigDecimal(0.0));
    }

    @Test
    public void testOriginAccountNotFound() {
        final Lock lock1 = new ReentrantLock();
        final Lock lock2 = new ReentrantLock();
        final AccountLockingSession accountLockingSession = new AccountLockingSession(lock1, lock2);
        final Long originAccountId = 1L;
        final Long destinationAccountId = 2L;
        final BigDecimal amount = new BigDecimal(123.15);

        when(accountLockManager.getOrCreateAccountLocks(eq(originAccountId), eq(destinationAccountId))).thenReturn(accountLockingSession);
        when(accountRepository.findById(eq(originAccountId))).thenReturn(null);

        try {
            accountManager.transfer(originAccountId, destinationAccountId, amount);
            fail();
        } catch (Exception e) {
            assertThat(e.getClass(), is(equalTo(AccountNotFoundException.class)));
            verify(accountRepository, times(1)).startTransaction();
            verify(accountRepository, never()).commitTransaction();
            verify(accountRepository, times(1)).rollbackTransaction();
        }

    }

    @Test
    public void testDestinationAccountNotFound() {
        final Lock lock1 = new ReentrantLock();
        final Lock lock2 = new ReentrantLock();
        final AccountLockingSession accountLockingSession = new AccountLockingSession(lock1, lock2);
        final Long originAccountId = 1L;
        final Long destinationAccountId = 2L;
        final BigDecimal amount = new BigDecimal(123.15);
        final BigDecimal originAccountAmount = new BigDecimal(1000.0);
        final Account originAccount = new Account();
        originAccount.setId(originAccountId);
        originAccount.setAmount(originAccountAmount);

        when(accountLockManager.getOrCreateAccountLocks(eq(originAccountId), eq(destinationAccountId))).thenReturn(accountLockingSession);
        when(accountRepository.findById(eq(originAccountId))).thenReturn(originAccount);
        when(accountRepository.findById(eq(destinationAccountId))).thenReturn(null);

        try {
            accountManager.transfer(originAccountId, destinationAccountId, amount);
            fail();
        } catch (Exception e) {
            assertThat(e.getClass(), is(equalTo(AccountNotFoundException.class)));
            verify(accountRepository, times(1)).startTransaction();
            verify(accountRepository, never()).commitTransaction();
            verify(accountRepository, times(1)).rollbackTransaction();
        }

    }

    @Test
    public void testInsufficientFunds() {
        final Lock lock1 = new ReentrantLock();
        final Lock lock2 = new ReentrantLock();
        final AccountLockingSession accountLockingSession = new AccountLockingSession(lock1, lock2);
        final Long originAccountId = 1L;
        final Long destinationAccountId = 2L;
        final BigDecimal amount = new BigDecimal(123.15);
        final BigDecimal originAccountAmount = new BigDecimal(0.0);
        final Account originAccount = new Account();
        originAccount.setId(originAccountId);
        originAccount.setAmount(originAccountAmount);

        when(accountLockManager.getOrCreateAccountLocks(eq(originAccountId), eq(destinationAccountId))).thenReturn(accountLockingSession);
        when(accountRepository.findById(eq(originAccountId))).thenReturn(originAccount);

        try {
            accountManager.transfer(originAccountId, destinationAccountId, amount);
            fail();
        } catch (Exception e) {
            assertThat(e.getClass(), is(equalTo(InsufficientFundsException.class)));
            verify(accountRepository, times(1)).startTransaction();
            verify(accountRepository, never()).commitTransaction();
            verify(accountRepository, times(1)).rollbackTransaction();
        }

    }

    @Test
    public void testGetAccountByIdSimpleCase() {
        final Long accountId = 1L;
        final Account account = new Account();
        account.setId(accountId);
        account.setAmount(new BigDecimal(100.0));

        when(accountRepository.findById(eq(accountId))).thenReturn(account);

        Account result = accountManager.getAccountById(accountId);

        assertThat(result, is(equalTo(account)));

    }

    @Test(expected = BadParameterException.class)
    public void testGetAccountByIdAccountIsNull() {
        accountManager.getAccountById(null);
    }

    @Test(expected = AccountNotFoundException.class)
    public void testGetAccountByNotFound() {
        final Long accountId = 1L;
        when(accountRepository.findById(eq(accountId))).thenReturn(null);
        accountManager.getAccountById(accountId);
    }
}

