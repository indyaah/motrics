package pro.anuj.challenge.motrics.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pro.anuj.challenge.motrics.utils.MedianHolder;

@Setter
@Getter
@EqualsAndHashCode
public class Statistics {

    @JsonIgnore
    private final MedianHolder medianHolder = new MedianHolder();

    @JsonProperty("count")
    private Integer sampleCount = 0;
    @JsonProperty("average")
    private Double average = 0.0;
    @JsonProperty("minimum")
    private Double minimum = Double.MAX_VALUE;
    @JsonProperty("maximum")
    private Double maximum = Double.MIN_VALUE;
    @JsonProperty("median")
    private Double median;
}
