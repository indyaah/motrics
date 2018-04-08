package pro.anuj.challenge.motrics.api.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@ApiModel
@AllArgsConstructor
public class ApiErrorResponse {


    @JsonProperty("dateTime")
    @ApiModelProperty(name = "Time of error generation", dataType = "ISO DateTime")
    private final String dateTime = ZonedDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);
    @JsonProperty("message")
    @ApiModelProperty(name = "API error message", dataType = "String")
    private String message;
    @JsonProperty("errorCode")
    @ApiModelProperty(name = "http error code", dataType = "int")
    private Integer errorCode;
}
