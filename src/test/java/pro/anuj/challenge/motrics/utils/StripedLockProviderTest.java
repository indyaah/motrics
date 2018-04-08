package pro.anuj.challenge.motrics.utils;

import org.junit.Test;
import pro.anuj.challenge.motrics.exception.AbstractMotricsException;
import pro.anuj.challenge.motrics.exception.LockTimeOutException;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.*;

public class StripedLockProviderTest {

    private static final UUID KEY = UUID.randomUUID();
    private static final UUID KEY_1 = UUID.randomUUID();
    private final StripedLockProvider<UUID, String> manager = new StripedLockProvider<>(Long.MAX_VALUE, TimeUnit.SECONDS);
    private final StripedLockProvider<UUID, String> timeoutManager = new StripedLockProvider<>(500, TimeUnit.MILLISECONDS);

    @Test
    public void whenFirstThreadThrowsExceptionThenAnotherWaitingThreadCanAcquireLock() throws Exception {
        final CountDownLatch t1WorkUnitEntry = new CountDownLatch(1);
        final CountDownLatch t1ThrowException = new CountDownLatch(1);
        final Thread firstThread = new Thread(() -> {
            try {
                manager.executeLocked(KEY, () -> {
                    try {
                        t1WorkUnitEntry.countDown();
                        t1ThrowException.await();
                        throw new DummyException();
                    } catch (final InterruptedException ignored) {
                        return null;
                    }
                });
                fail();
            } catch (DummyException ignored) {
            }
        });
        final Thread secondThread = new Thread(() -> manager.executeLocked(KEY, () -> ""));

        firstThread.start();
        t1WorkUnitEntry.await();
        secondThread.start();

        while (manager.stripesWaiting() < 1) {
            sleep(10);
        }

        t1ThrowException.countDown();
        secondThread.join();

        firstThread.interrupt();
        firstThread.join();

        assertCleanup(manager);
    }

    @Test
    public void whenFirstThreadInterruptedThenAnotherWaitingThreadCanAcquireLock() throws Exception {
        final CountDownLatch t1WorkUnitEntry = new CountDownLatch(1);
        final Thread t1 = buildLongRunningThread(manager, t1WorkUnitEntry, KEY);

        t1.start();
        t1WorkUnitEntry.await();
        assertEquals(0, manager.stripesWaiting());

        final Thread t2 = new Thread(() -> {
            try {
                manager.executeLocked(KEY, () -> "");
            } catch (final LockInterruptedException ignored) {
            }
        });
        t2.start();

        while (manager.stripesWaiting() < 1) {
            sleep(10);
        }

        assertEquals(1, manager.stripesWaiting());

        t1.interrupt();
        t1.join();

        t2.join();

        assertCleanup(manager);
    }

    @Test
    public void whenFirstThreadReleasesLockNormallyThenAnotherWaitingThreadCanAcquireLock() throws Exception {

        final CountDownLatch t1WorkUnitEntry = new CountDownLatch(1);
        final CountDownLatch t1SignalToExit = new CountDownLatch(1);

        final Thread t1 = new Thread(() ->
                manager.executeLocked(KEY, () -> {
                    try {
                        t1WorkUnitEntry.countDown();
                        t1SignalToExit.await();
                        return "";
                    } catch (final InterruptedException ignored) {
                        return null;
                    }
                }));
        t1.start();

        t1WorkUnitEntry.await();

        final Thread t2 = new Thread(() -> manager.executeLocked(KEY, () -> ""));
        t2.start();

        while (manager.stripesWaiting() < 1) {
            sleep(10);
        }

        t1SignalToExit.countDown();

        t1.join();
        t2.join();

        assertCleanup(manager);
    }


    @Test
    public void whenAThreadInterruptedWhileWaitingThenLockInterruptedException() throws Exception {
        final CountDownLatch t1WorkUnitEntry = new CountDownLatch(1);
        final Thread t1 = buildLongRunningThread(manager, t1WorkUnitEntry, KEY);
        final Exchanger<AbstractMotricsException> exchanger = new Exchanger<>();
        final Thread t2 = buildInterruptedThread(manager, exchanger);

        t1.start();
        t1WorkUnitEntry.await();
        t2.start();

        while (manager.stripesWaiting() < 1) {
            sleep(10);
        }
        t2.interrupt();

        assertThat(exchanger.exchange(null), instanceOf(LockInterruptedException.class));

        t1.interrupt();
        t1.join();
        t2.join();

        assertCleanup(manager);
    }

    @Test
    public void whenTwoThreadsUseDifferentKeyThenTheyDoNotBlockEachOther() throws Exception {
        final CountDownLatch t1WorkUnitEntry = new CountDownLatch(1);
        final CountDownLatch t2WorkUnitEntry = new CountDownLatch(1);

        final Thread t1 = buildLongRunningThread(manager, t1WorkUnitEntry, KEY);

        t1.start();
        t1WorkUnitEntry.await();
        assertEquals(0, manager.stripesWaiting());

        final Thread t2 = new Thread(() ->
                manager.executeLocked(KEY_1, () -> {
                    try {
                        t2WorkUnitEntry.countDown();
                        Thread.sleep(Long.MAX_VALUE);
                        return "";
                    } catch (final InterruptedException ignored) {
                        return null;
                    }
                }));

        t2.start();

        t2WorkUnitEntry.await();

        t1.interrupt();
        t2.interrupt();
        t1.join();
        t2.join();

        assertCleanup(manager);
    }


    @Test
    public void whenFirstThreadDoesntReleaseLockWithinTimeOutThenSecondThreadHasLockTimeOutException() throws Exception {

        final CountDownLatch t1WorkUnitEntry = new CountDownLatch(1);

        final Thread t1 = buildLongRunningThread(timeoutManager, t1WorkUnitEntry, KEY);
        t1.start();
        t1WorkUnitEntry.await();

        final Exchanger<AbstractMotricsException> exchanger = new Exchanger<>();

        final Thread t2 = buildInterruptedThread(timeoutManager, exchanger);
        t2.start();

        assertThat(exchanger.exchange(null), instanceOf(LockTimeOutException.class));

        assertEquals(1, timeoutManager.activeStripes());

        t1.interrupt();
        t1.join();

        assertCleanup(timeoutManager);
    }


    @Test
    public void aThreadCanAcquireLocksOnMultipleKeys() throws Exception {

        final CountDownLatch workUnitEntry = new CountDownLatch(1);
        final CountDownLatch workUnitExit = new CountDownLatch(1);

        final Thread t1 = buildNestedThread(manager, workUnitEntry, workUnitExit, KEY_1);

        t1.start();

        workUnitEntry.await();

        assertEquals(2, manager.activeStripes());
        assertEquals(0, manager.stripesWaiting());

        workUnitExit.countDown();

        t1.join();

        assertCleanup(manager);
    }

    @Test
    public void aThreadCanEnterLockedAreaWithSameKeyMultipleTimeWith() throws Exception {

        final CountDownLatch workUnitEntry = new CountDownLatch(1);
        final CountDownLatch workUnitExit = new CountDownLatch(1);

        final Thread t1 = buildNestedThread(manager, workUnitEntry, workUnitExit, KEY);

        t1.start();

        workUnitEntry.await();

        assertEquals(1, manager.activeStripes());
        assertEquals(0, manager.stripesWaiting());

        workUnitExit.countDown();

        t1.join();

        assertCleanup(manager);
    }


    private Thread buildInterruptedThread(StripedLockProvider<UUID, String> manager, Exchanger<AbstractMotricsException> exchanger) {
        return new Thread(() -> {
            try {
                manager.executeLocked(KEY, () -> "");
            } catch (final AbstractMotricsException e) {
                try {
                    exchanger.exchange(e);
                } catch (final InterruptedException ignored) {
                }
            }
        });
    }

    private Thread buildNestedThread(StripedLockProvider<UUID, String> manager, CountDownLatch workUnitEntry, CountDownLatch workUnitExit, UUID key) {
        return new Thread(() ->
                manager.executeLocked(KEY, () ->
                        manager.executeLocked(key, () ->
                        {
                            try {
                                workUnitEntry.countDown();
                                workUnitExit.await();
                                return "";
                            } catch (final InterruptedException e) {
                                e.printStackTrace();
                                return null;
                            }
                        })));
    }


    private Thread buildLongRunningThread(StripedLockProvider<UUID, String> manager, CountDownLatch t1WorkUnitEntry, UUID key) {
        return new Thread(() ->
                manager.executeLocked(key, () -> {
                    try {
                        t1WorkUnitEntry.countDown();
                        sleep(Long.MAX_VALUE);
                        return "";
                    } catch (final InterruptedException ignored) {
                        return null;
                    }
                }));
    }

    private void assertCleanup(final StripedLockProvider<UUID, String> lock) {
        assertEquals(0, lock.activeStripes());
    }

    private static class DummyException extends RuntimeException {
        private static final long serialVersionUID = 8588581202895705617L;
    }

}
