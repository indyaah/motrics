package pro.anuj.challenge.motrics.repo;

import org.junit.Test;
import pro.anuj.challenge.motrics.api.vo.CreateRequest;
import pro.anuj.challenge.motrics.domain.Metric;
import pro.anuj.challenge.motrics.exception.DuplicateMetricException;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SuppressWarnings("unchecked")
public class MetricRepositoryTest {

    private static final String METRIC_NAME = "SOME_THING";
    private final Map<UUID, Metric> metricCache = mock(Map.class);
    private final Map<String, UUID> nameCache = mock(Map.class);
    private final CreateRequest createRequest = mock(CreateRequest.class);

    private final MetricRepository sut = new MetricRepository(metricCache, nameCache);

    @Test(expected = DuplicateMetricException.class)
    public void whenMetricWithSameNameAlreadyExistsThenException() {
        when(nameCache.get(METRIC_NAME)).thenReturn(UUID.randomUUID());
        when(createRequest.getName()).thenReturn(METRIC_NAME);

        sut.createMetric(createRequest);
    }


}