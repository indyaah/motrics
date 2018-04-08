package pro.anuj.challenge.motrics.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Setter
@Getter
@ApiModel
public class InsertRequest {

    @NotNull
    @JsonProperty("id")
    @ApiModelProperty(name = "metric id", dataType = "UUID")
    private UUID id;
    @NotNull
    @JsonProperty("value")
    @ApiModelProperty(name = "metric value", dataType = "Double")
    private Double value;
}
