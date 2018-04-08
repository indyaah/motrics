package pro.anuj.challenge.motrics.repo;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.anuj.challenge.motrics.api.vo.CreateRequest;
import pro.anuj.challenge.motrics.api.vo.ValueObject;
import pro.anuj.challenge.motrics.domain.Metric;
import pro.anuj.challenge.motrics.domain.Statistics;
import pro.anuj.challenge.motrics.exception.DuplicateMetricException;
import pro.anuj.challenge.motrics.exception.MetricNotFoundException;
import pro.anuj.challenge.motrics.utils.MedianHolder;
import pro.anuj.challenge.motrics.utils.StripedLockProvider;

import java.util.Map;
import java.util.UUID;

@Log4j2
@Component
public class MetricRepository {

    private final Map<UUID, Metric> metricCache;
    private final Map<String, UUID> nameCache;
    private final StripedLockProvider<UUID, Metric> stripedLockProvider;

    @Autowired
    public MetricRepository(final Map<UUID, Metric> metricCache,
                            final Map<String, UUID> nameCache,
                            final StripedLockProvider<UUID, Metric> stripedLockProvider) {
        this.metricCache = metricCache;
        this.nameCache = nameCache;
        this.stripedLockProvider = stripedLockProvider;
    }

    public Metric createMetric(final CreateRequest request) throws DuplicateMetricException {
        final String metricName = request.getName();
        final UUID uuid = nameCache.get(metricName);
        if (uuid != null) {
            log.debug("Duplicate attempt to add metric {}", metricName);
            throw new DuplicateMetricException(metricName, uuid);
        }
        Metric metric = new Metric(UUID.randomUUID(), metricName, new Statistics());
        metricCache.put(metric.getId(), metric);
        nameCache.put(metricName, metric.getId());
        return metric;
    }

    public Metric addValueToMetric(final UUID uuid,
                                   final Double value) {
        return stripedLockProvider.executeLocked(uuid, () -> addValueToMetricConcurrently(uuid, value));
    }

    Metric addValueToMetricConcurrently(final UUID uuid,
                                        final Double value) {
        Metric metric = metricCache.get(uuid);
        if (metric == null) {
            throw new MetricNotFoundException(uuid);
        }
        Statistics statistics = metric.getStatistics();
        Integer newCount = statistics.getSampleCount() + 1;
        final Double newAvg = (statistics.getAverage() * statistics.getSampleCount() + value) / newCount;
        statistics.setAverage(newAvg);
        statistics.setSampleCount(newCount);
        if (statistics.getMinimum() > value) {
            statistics.setMinimum(value);
        }
        if (statistics.getMaximum() < value) {
            statistics.setMaximum(value);
        }
        final MedianHolder medianHolder = statistics.getMedianHolder();
        medianHolder.add(value);
        statistics.setMedian(medianHolder.getMedian());
        return metric;
    }

    public Statistics getAllStats(UUID uuid) {
        Metric metric = metricCache.get(uuid);
        if (metric == null) {
            throw new MetricNotFoundException(uuid);
        }
        return metric.getStatistics();
    }

    public ValueObject getAverage(UUID uuid) {
        return new ValueObject(getAllStats(uuid).getAverage());
    }

    public ValueObject getMinimum(UUID uuid) {
        return new ValueObject(getAllStats(uuid).getMinimum());
    }

    public ValueObject getMaximum(UUID uuid) {
        return new ValueObject(getAllStats(uuid).getMaximum());
    }

    public ValueObject getMedian(UUID uuid) {
        return new ValueObject(getAllStats(uuid).getMedian());
    }

    public ValueObject getSampleCount(UUID uuid) {
        return new ValueObject(getAllStats(uuid).getSampleCount());
    }

}