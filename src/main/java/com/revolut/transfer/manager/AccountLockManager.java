package com.revolut.transfer.manager;

import com.revolut.transfer.model.AccountLockingSession;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <p></p>Account level locking, It's mandatory to have only one instance of this manager in the system as
 * the locking is done with in-memory structures. If more of one instance is being executed in the system, there is
 * high risk of running race conditions</p>
 * <p>The manager will keep in memory locks for the accounts involved on current transfers. If one specific account
 * is involved in more than one transfer (being executed and pending for execution), the manager will keep a counter
 * of the transactions in which the account is involved. Once all the transactions has finished. The lock will be removed
 * from memory to avoid memory leaks</p>
 */
public class AccountLockManager {

    private final static Logger logger = Logger.getLogger(AccountLockManager.class);

    /**
     * Lock used to let only one thread at the same time to modify the account locks data structures
     */
    private Lock mapLock = new ReentrantLock();

    /**
     * Map containing the account locks
     */
    // Open for testing, do not access directly from production code
    protected Map<Long, Lock> accountLocks = new HashMap<>();

    /**
     * Map containing the counters of the transactions that already have a lock on the accounts
     */
    // Open for testing, do not access directly from production code
    protected Map<Long, Integer> lockCounter = new HashMap<>();

    /**
     * <p>Creates or returns already created locks for the given accounts.
     * It increases the counter of the locks if more than one transaction retrieve the lock of a given account</p>
     * <p>Locks are returned in the natural order of the account ids to avoid deadlocks</p>
     * @param originAccountId
     * @param destinationAccountId
     * @return
     */
    public AccountLockingSession getOrCreateAccountLocks(Long originAccountId, Long destinationAccountId) {
        List<Long> orderedAccountIds = orderAccounts(originAccountId, destinationAccountId);
        mapLock.lock();
        Lock lock1 = getOrCreateLock(orderedAccountIds.get(0));
        Lock lock2 = getOrCreateLock(orderedAccountIds.get(1));
        mapLock.unlock();

        return new AccountLockingSession(lock1, lock2);
    }

    /**
     * To be called when the transaction has been completed (successfully or unsuccessfully). If not, there
     * is risk of memory leaks
     * @param originAccountId
     * @param destinationAccountId
     */
    public void releaseLocks(Long originAccountId, Long destinationAccountId) {
        List<Long> orderedAccountIds = orderAccounts(originAccountId, destinationAccountId);
        mapLock.lock();
        releaseLock(orderedAccountIds.get(1));
        releaseLock(orderedAccountIds.get(0));
        mapLock.unlock();

    }

    private List<Long> orderAccounts(Long originAccountId, Long destinationAccountId) {
        List<Long> orderedAccountIds = Arrays.asList(originAccountId, destinationAccountId);
        orderedAccountIds.sort(Comparator.naturalOrder());
        return orderedAccountIds;
    }

    private Lock getOrCreateLock(Long accountId) {
        Lock result = accountLocks.get(accountId);
        if (result == null) {
            result = new ReentrantLock();
            accountLocks.put(accountId, result);
            lockCounter.put(accountId, 1);
        } else {
            lockCounter.put(accountId, lockCounter.get(accountId) + 1);
        }
        return result;
    }

    private void releaseLock(Long accountId) {
        Lock lock = accountLocks.get(accountId);
        Integer count = lockCounter.get(accountId);
        if (count == null) {
            accountLocks.remove(accountId);
        } else {
            count = count - 1;
        }
        if (lock == null) {
            lockCounter.remove(accountId);
            return;
        }
        if (count == 0) {
            lockCounter.remove(accountId);
            accountLocks.remove(accountId);
        } else {
            lockCounter.put(accountId, count);
        }
        try {
            lock.unlock();
        } catch (IllegalMonitorStateException e) {
            logger.error("Trying to unlock a not locked Lock", e);
        }
    }
}
