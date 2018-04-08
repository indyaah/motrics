package pro.anuj.challenge.motrics.utils;

import org.junit.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class MedianHolderTest {

    private final MedianHolder medianHolder = new MedianHolder();

    @Test
    public void whenValuesAddedToMedianHolderSequentiallyCorrectMedianIsCalculated() {
        IntStream.range(1, 10000).forEach(i -> medianHolder.add((double) i));

        assertThat(medianHolder.getMedian()).isEqualTo(5000.0);
    }

    @Test
    public void whenValuesAddedToMedianHolderConcurrentlyCorrectMedianIsCalculated() {
        IntStream range = IntStream.range(1, 1000000).parallel();
        assertThat(range.isParallel()).isEqualTo(true);

        range.parallel().forEach(i -> medianHolder.add((double) i));

        assertThat(medianHolder.getMedian()).isEqualTo(500000.0);
    }
}