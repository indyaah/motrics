package pro.anuj.challenge.motrics.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Setter
@Getter
@ApiModel
@NoArgsConstructor
@AllArgsConstructor
public class InsertRequest {

    @NotNull(message = "metric id is a required field")
    @JsonProperty("id")
    @ApiModelProperty(name = "metric id", dataType = "UUID")
    private UUID id;
    @NotNull(message = "metric value to be inserted is a required field")
    @JsonProperty("value")
    @ApiModelProperty(name = "metric value", dataType = "Double")
    private Double value;
}
