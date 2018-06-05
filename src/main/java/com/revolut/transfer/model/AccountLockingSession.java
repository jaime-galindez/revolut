package com.revolut.transfer.model;

import java.util.Objects;
import java.util.concurrent.locks.Lock;

public class AccountLockingSession {

    private Lock firstLock;
    private Lock secondLock;

    public AccountLockingSession(Lock firstLock, Lock secondLock) {
        this.firstLock = firstLock;
        this.secondLock = secondLock;
    }

    public void lock() {
        firstLock.lock();
        secondLock.lock();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountLockingSession that = (AccountLockingSession) o;
        return Objects.equals(firstLock, that.firstLock) &&
                Objects.equals(secondLock, that.secondLock);
    }

    @Override
    public int hashCode() {

        return Objects.hash(firstLock, secondLock);
    }
}
