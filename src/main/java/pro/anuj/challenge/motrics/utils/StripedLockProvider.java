package pro.anuj.challenge.motrics.utils;

import lombok.extern.log4j.Log4j2;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static java.lang.Math.abs;
import static java.util.Arrays.setAll;

@Log4j2
public class StripedLockProvider<T, R> {

    private final ConcurrentHashMap<T, StripedLock> key2lock = new ConcurrentHashMap<>();
    private final StripedLock[] stripes;
    private final long lockTimeout;
    private final TimeUnit lockTimeoutUnit;


    public StripedLockProvider(final long lockTimeout, final TimeUnit lockTimeoutUnit) {
        this(lockTimeout, lockTimeoutUnit, 16);
    }

    private StripedLockProvider(final long lockTimeout, final TimeUnit lockTimeoutUnit, final int numberOfStripes) {
        this.lockTimeout = lockTimeout;
        this.lockTimeoutUnit = lockTimeoutUnit;
        this.stripes = new StripedLock[numberOfStripes];

        setAll(stripes, i -> new StripedLock(lockTimeout, lockTimeoutUnit));
    }


    public final R executeLocked(final T key, LockedFunction<R> function) {
        final StripedLock lock = getKeyLock(key);
        try {
            lock.tryLock();
            try {
                return function.apply();
            } finally {
                lock.unlock();
            }
        } finally {
            freeKeyLock(key, lock);
        }
    }


    private void freeKeyLock(final T key, final StripedLock lock) {
        getStripedLock(key).tryLock();
        try {
            lock.decrementUses();
            if (!lock.isUsed()) {
                key2lock.remove(key);
            }
        } finally {
            getStripedLock(key).unlock();
        }
    }

    private StripedLock getKeyLock(final T key) {
        getStripedLock(key).tryLock();
        try {
            final StripedLock result;
            final StripedLock previousLock = key2lock.get(key);
            if (previousLock == null) {
                result = new StripedLock(lockTimeout, lockTimeoutUnit);
                key2lock.put(key, result);
            } else {
                result = previousLock;
            }
            result.incrementUses();
            return result;
        } finally {
            getStripedLock(key).unlock();
        }
    }

    private StripedLock getStripedLock(final T key) {
        return stripes[abs(key.hashCode() % stripes.length)];
    }

    int activeStripes() {
        return key2lock.size();
    }

    int stripesWaiting() {
        int result = 0;
        for (final StripedLock lock : key2lock.values()) {
            result += lock.getQueueLength();
        }
        return result;
    }
}
