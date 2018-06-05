package com.revolut.transfer.manager;

import com.revolut.transfer.model.AccountLockingSession;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;

public class AccountLockManagerTest {

    private AccountLockManager accountLockManager;

    @Before
    public void init() {
        accountLockManager = new AccountLockManager();
    }

    @Test
    public void testLockCleanUpSingleCase() {
        final Long originAccountId = 1L;
        final Long destinationAccountId = 2L;

        assertThat(accountLockManager.accountLocks.size(), is(equalTo(0)));
        assertThat(accountLockManager.lockCounter.size(), is(equalTo(0)));

        AccountLockingSession session = accountLockManager.getOrCreateAccountLocks(originAccountId, destinationAccountId);
        session.lock();

        assertThat(accountLockManager.accountLocks.size(), is(equalTo(2)));
        assertThat(accountLockManager.lockCounter.size(), is(equalTo(2)));

        accountLockManager.releaseLocks(originAccountId, destinationAccountId);

        assertThat(accountLockManager.accountLocks.size(), is(equalTo(0)));
        assertThat(accountLockManager.lockCounter.size(), is(equalTo(0)));
    }

    @Test
    public void testLockCleanUpMultipleCase() {
        final Long originAccountId = 1L;
        final Long destinationAccountId = 2L;

        assertThat(accountLockManager.accountLocks.size(), is(equalTo(0)));
        assertThat(accountLockManager.lockCounter.size(), is(equalTo(0)));

        AccountLockingSession session1 = accountLockManager.getOrCreateAccountLocks(originAccountId, destinationAccountId);
        AccountLockingSession session2 = accountLockManager.getOrCreateAccountLocks(originAccountId, destinationAccountId);
        AccountLockingSession session3 = accountLockManager.getOrCreateAccountLocks(originAccountId, destinationAccountId);

        assertThat(accountLockManager.accountLocks.size(), is(equalTo(2)));
        assertThat(accountLockManager.lockCounter.size(), is(equalTo(2)));
        assertThat(accountLockManager.lockCounter.get(originAccountId), is(equalTo(3)));
        assertThat(accountLockManager.lockCounter.get(destinationAccountId), is(equalTo(3)));
        session1.lock();

        accountLockManager.releaseLocks(originAccountId, destinationAccountId);

        assertThat(accountLockManager.accountLocks.size(), is(equalTo(2)));
        assertThat(accountLockManager.lockCounter.size(), is(equalTo(2)));
        assertThat(accountLockManager.lockCounter.get(originAccountId), is(equalTo(2)));
        assertThat(accountLockManager.lockCounter.get(destinationAccountId), is(equalTo(2)));
        session1.lock();

        accountLockManager.releaseLocks(originAccountId, destinationAccountId);

        assertThat(accountLockManager.accountLocks.size(), is(equalTo(2)));
        assertThat(accountLockManager.lockCounter.size(), is(equalTo(2)));
        assertThat(accountLockManager.lockCounter.get(originAccountId), is(equalTo(1)));
        assertThat(accountLockManager.lockCounter.get(destinationAccountId), is(equalTo(1)));
        session1.lock();

        accountLockManager.releaseLocks(originAccountId, destinationAccountId);

        assertThat(accountLockManager.accountLocks.size(), is(equalTo(0)));
        assertThat(accountLockManager.lockCounter.size(), is(equalTo(0)));

    }

    @Test
    public void testReleaseLocksWithoutPreviousLockCreationExitsSmoothly() {
        final Long originAccountId = 1L;
        final Long destinationAccountId = 2L;

        assertThat(accountLockManager.accountLocks.size(), is(equalTo(0)));
        assertThat(accountLockManager.lockCounter.size(), is(equalTo(0)));

        try {
            accountLockManager.releaseLocks(originAccountId, destinationAccountId);
        } catch (Throwable e) {
            fail();
        }
    }

    @Test
    public void testReleaseLocksWithoutPreviousLockingExitsSmoothly() {
        final Long originAccountId = 1L;
        final Long destinationAccountId = 2L;

        assertThat(accountLockManager.accountLocks.size(), is(equalTo(0)));
        assertThat(accountLockManager.lockCounter.size(), is(equalTo(0)));

        accountLockManager.getOrCreateAccountLocks(originAccountId, destinationAccountId);

        try {
            accountLockManager.releaseLocks(originAccountId, destinationAccountId);
        } catch (Throwable e) {
            fail();
        }
    }

    @Test
    public void testLockOrderingIsGuaranteedToAvoidDeadlock() {
        final Long originAccountId = 1L;
        final Long destinationAccountId = 2L;

        AccountLockingSession session1 = accountLockManager.getOrCreateAccountLocks(originAccountId, destinationAccountId);
        AccountLockingSession session2 = accountLockManager.getOrCreateAccountLocks(destinationAccountId, originAccountId);

        assertThat(session1, is(equalTo(session2)));
    }
}
