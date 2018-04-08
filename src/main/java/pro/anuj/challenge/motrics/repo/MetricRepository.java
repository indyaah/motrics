package pro.anuj.challenge.motrics.repo;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.anuj.challenge.motrics.api.vo.CreateRequest;
import pro.anuj.challenge.motrics.domain.Metric;
import pro.anuj.challenge.motrics.domain.Statistics;
import pro.anuj.challenge.motrics.exception.DuplicateMetricException;

import java.util.Map;
import java.util.UUID;

@Log4j2
@Component
public class MetricRepository {

    private final Map<UUID, Metric> metricCache;
    private final Map<String, UUID> nameCache;

    @Autowired
    public MetricRepository(final Map<UUID, Metric> metricCache,
                            final Map<String, UUID> nameCache) {
        this.metricCache = metricCache;
        this.nameCache = nameCache;
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
}