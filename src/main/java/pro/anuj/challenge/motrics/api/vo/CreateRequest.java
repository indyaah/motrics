package pro.anuj.challenge.motrics.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@ApiModel
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreateRequest {

    @NotNull
    @NotEmpty
    @JsonProperty("name")
    @ApiModelProperty(name = "name", dataType = "String")
    private String name;

}
