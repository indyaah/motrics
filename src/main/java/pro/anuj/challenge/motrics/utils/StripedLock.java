package pro.anuj.challenge.motrics.utils;

import pro.anuj.challenge.motrics.exception.LockTimeOutException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

class StripedLock {

    private final ReentrantLock delegate = new ReentrantLock();
    private final long lockTimeout;
    private final TimeUnit lockTimeoutUnit;
    private long uses = 0;

    StripedLock(final long lockTimeout, final TimeUnit lockTimeoutUnit) {
        this.lockTimeout = lockTimeout;
        this.lockTimeoutUnit = lockTimeoutUnit;
    }

    void decrementUses() {
        uses--;
    }

    void incrementUses() {
        uses++;
    }

    boolean isUsed() {
        return uses != 0L;
    }

    int getQueueLength() {
        return delegate.getQueueLength();
    }

    void tryLock() {
        try {
            if (!delegate.tryLock(lockTimeout, lockTimeoutUnit)) {
                throw new LockTimeOutException();
            }
        } catch (InterruptedException e) {
            throw new LockInterruptedException();
        }

    }

    void unlock() {
        delegate.unlock();
    }
}
