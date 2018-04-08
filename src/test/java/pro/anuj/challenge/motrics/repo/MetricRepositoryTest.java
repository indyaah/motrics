package pro.anuj.challenge.motrics.repo;

import org.junit.Test;
import pro.anuj.challenge.motrics.api.vo.CreateRequest;
import pro.anuj.challenge.motrics.domain.Metric;
import pro.anuj.challenge.motrics.domain.Statistics;
import pro.anuj.challenge.motrics.exception.DuplicateMetricException;

import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class MetricRepositoryTest {

    private static final String METRIC_NAME = "SOME_THING";
    private final Map<UUID, Metric> metricCache = mock(Map.class);
    private final Map<String, UUID> nameCache = mock(Map.class);
    private final CreateRequest createRequest = mock(CreateRequest.class);

    private final MetricRepository sut = new MetricRepository(metricCache, nameCache);

    @Test
    public void whenNewMetricThenCreated() {
        when(nameCache.get(METRIC_NAME)).thenReturn(null);
        when(createRequest.getName()).thenReturn(METRIC_NAME);

        sut.createMetric(createRequest);

        verify(metricCache, times(1)).put(any(), any());
        verify(nameCache, times(1)).put(eq(METRIC_NAME), any());
    }

    @Test(expected = DuplicateMetricException.class)
    public void whenMetricWithSameNameAlreadyExistsThenException() {
        when(nameCache.get(METRIC_NAME)).thenReturn(UUID.randomUUID());
        when(createRequest.getName()).thenReturn(METRIC_NAME);

        sut.createMetric(createRequest);
    }


    @Test
    public void whenNewValueAddedThenAverageAndCountAreUpdated() {
        Metric metric = mock(Metric.class);
        UUID key = UUID.randomUUID();
        Statistics statistics = mock(Statistics.class);

        when(metricCache.get(key)).thenReturn(metric);
        when(metric.getStatistics()).thenReturn(statistics);
        when(statistics.getSampleCount()).thenReturn(2);
        when(statistics.getAverage()).thenReturn(10.0);

        sut.addValueToMetric(key, 40.0);

        verify(statistics, times(1)).setAverage(20.0);
        verify(statistics, times(1)).setSampleCount(3);
    }

    @Test
    public void whenNewValueAddedAndValueLowerThanMinThenMinIsUpdated() {
        Metric metric = mock(Metric.class);
        UUID key = UUID.randomUUID();
        Statistics statistics = mock(Statistics.class);

        when(metricCache.get(key)).thenReturn(metric);
        when(metric.getStatistics()).thenReturn(statistics);
        when(statistics.getMinimum()).thenReturn(30.0);

        sut.addValueToMetric(key, 10.0);

        verify(statistics, times(1)).setMinimum(10.0);
    }

    @Test
    public void whenNewValueAddedAndValueHigherThanMaxThenMaxIsUpdated() {
        Metric metric = mock(Metric.class);
        UUID key = UUID.randomUUID();
        Statistics statistics = mock(Statistics.class);

        when(metricCache.get(key)).thenReturn(metric);
        when(metric.getStatistics()).thenReturn(statistics);
        when(statistics.getMaximum()).thenReturn(10.0);

        sut.addValueToMetric(key, 30.0);

        verify(statistics, times(1)).setMaximum(30.0);
    }
}