package pro.anuj.challenge.motrics.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import pro.anuj.challenge.motrics.api.vo.ApiErrorResponse;
import pro.anuj.challenge.motrics.api.vo.CreateRequest;
import pro.anuj.challenge.motrics.api.vo.InsertRequest;
import pro.anuj.challenge.motrics.domain.Metric;
import pro.anuj.challenge.motrics.exception.DuplicateMetricException;
import pro.anuj.challenge.motrics.repo.MetricRepository;

import javax.validation.Valid;

import static java.text.MessageFormat.format;
import static org.springframework.http.ResponseEntity.ok;
import static pro.anuj.challenge.motrics.Constants.REQUIRED_ARGUMENT_MISSING;

@Log4j2
@RestController
@Api("Metrics Resource")
public class MetricResource {

    private final MetricRepository repository;

    public MetricResource(MetricRepository repository) {
        this.repository = repository;
    }

    @ApiOperation("Create a new metric entry.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Created", response = Metric.class),
            @ApiResponse(code = 503, message = "Internal Server Error", response = ApiErrorResponse.class),
            @ApiResponse(code = 400, message = "Invalid Input", response = ApiErrorResponse.class)
    })
    @PostMapping(value = "/metric", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Metric> create(@RequestBody @Valid CreateRequest request) {
        return ok().body(repository.createMetric(request));
    }

    @ApiOperation("Add value for a metric.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Successfully processed adding new value", response = Metric.class),
            @ApiResponse(code = 503, message = "Internal Server Error", response = ApiErrorResponse.class),
            @ApiResponse(code = 404, message = "Invalid Input", response = ApiErrorResponse.class)
    })
    @PutMapping(value = "/metric", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Metric> put(@RequestBody @Valid InsertRequest req) {
        return ok().body(repository.addValueToMetric(req.getId(), req.getValue()));
    }


    @ExceptionHandler(DuplicateMetricException.class)
    ResponseEntity<ApiErrorResponse> duplicateMetric(DuplicateMetricException e) {
        return ResponseEntity.status(e.httpStatus()).body(new ApiErrorResponse(e.message()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> validationFailed(MethodArgumentNotValidException e) {
        final String message = format(REQUIRED_ARGUMENT_MISSING, e.getBindingResult().getFieldError().getField());
        return ResponseEntity.badRequest().body(new ApiErrorResponse(message));
    }

}
