package pro.anuj.challenge.motrics.utils;

import org.junit.Test;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

import static java.lang.Thread.sleep;
import static org.junit.Assert.*;

public class StripedLockProviderIntegrationTest {

    private static final int DIFFERENT_KEYS = 5;
    private static final int THREAD_COUNT = 200;
    private static final int INVOCATIONS_PER_THREAD = 1000;

    @Test
    public void testDoLocked() throws InterruptedException {

        StripedLockProvider<UUID, Double> stripedLockProvider = new StripedLockProvider<>(1, TimeUnit.SECONDS);
        final DummyService dummyService = new DummyServiceImpl();
        final DummyService dummyServiceDecorator = new DummyServiceDecorator(dummyService, stripedLockProvider);

        final List<UUID> keys = new ArrayList<>();
        for (int i = 0; i < DIFFERENT_KEYS; i++) {
            keys.add(UUID.randomUUID());
        }

        final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);

        final CyclicBarrier waitForOtherThreads = new CyclicBarrier(THREAD_COUNT);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executorService.execute(new TestThread(dummyServiceDecorator, keys, waitForOtherThreads));
        }

        executorService.shutdown();
        if (!executorService.awaitTermination(10, TimeUnit.MINUTES)) {
            fail();
        }

        assertFalse(dummyService.assertAdditions());

        assertEquals(0, stripedLockProvider.activeStripes());

    }

    private static class TestThread implements Runnable {

        private final DummyService service;
        private final List<UUID> keys;
        private final Random random = new Random();
        private final CyclicBarrier waitForOtherThreads;

        TestThread(final DummyService service, final List<UUID> keys, final CyclicBarrier waitForOtherThreads) {
            this.service = service;
            this.keys = new ArrayList<>(keys);
            this.waitForOtherThreads = waitForOtherThreads;
        }

        @Override
        public void run() {
            try {
                waitForOtherThreads.await();
                for (int i = 0; i < INVOCATIONS_PER_THREAD; i++) {
                    final UUID key = keys.get(random.nextInt(keys.size()));
                    service.addValue(key, random.nextDouble());
                }
            } catch (final InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }

    interface DummyService {
        Double addValue(UUID uuid, Double value);

        boolean assertAdditions();
    }

    class DummyServiceDecorator implements DummyService {

        private final DummyService dummyService;
        private final StripedLockProvider<UUID, Double> lockProvider;

        DummyServiceDecorator(DummyService dummyService, StripedLockProvider<UUID, Double> lockProvider) {
            this.dummyService = dummyService;
            this.lockProvider = lockProvider;
        }

        @Override
        public Double addValue(UUID uuid, Double value) {
            return lockProvider.executeLocked(uuid, () -> dummyService.addValue(uuid, value));
        }

        @Override
        public boolean assertAdditions() {
            return dummyService.assertAdditions();
        }
    }

    class DummyServiceImpl implements DummyService {
        private final Map<UUID, Double> invocationsSafe = new HashMap<>();
        private final Map<UUID, Double> invocationsUnsafe = new ConcurrentHashMap<>();

        @Override
        public Double addValue(UUID uuid, Double value) {
            synchronized (this) {
                addValue(uuid, invocationsSafe, value, false);
            }
            addValue(uuid, invocationsUnsafe, value, true);
            return 0.0;
        }

        private void addValue(UUID uuid, Map<UUID, Double> valueMap, Double value, boolean wait) {
            final Double prev = valueMap.get(uuid);
            if (wait) {
                try {
                    sleep(0, 10);
                } catch (final InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            valueMap.put(uuid, prev == null ? value : prev + value);
        }

        @Override
        public boolean assertAdditions() {
            boolean result = false;
            for (final Entry<UUID, Double> entry : invocationsSafe.entrySet()) {
                if (!entry.getValue().equals(invocationsUnsafe.get(entry.getKey()))) {
                    result = true;
                    break;
                }
            }
            return result;
        }

    }
}
