package pro.anuj.challenge.motrics.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@EqualsAndHashCode
public class Statistics {

    @JsonProperty("count")
    private Integer sampleCount = 0;
    @JsonProperty("average")
    private Double average = 0.0;
    @JsonProperty("minimum")
    private Double minimum = Double.MAX_VALUE;
    @JsonProperty("maximum")
    private Double maximum = Double.MIN_VALUE;

    @JsonProperty("median")
    // TODO: Introduce MedianCalculator
    public Double getMedian() {
        return 0.0;
    }

}
