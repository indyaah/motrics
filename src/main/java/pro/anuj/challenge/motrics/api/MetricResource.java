package pro.anuj.challenge.motrics.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pro.anuj.challenge.motrics.api.vo.CreateRequest;
import pro.anuj.challenge.motrics.domain.Metric;
import pro.anuj.challenge.motrics.repo.MetricRepository;

import javax.validation.Valid;

import static org.springframework.http.ResponseEntity.ok;

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
            @ApiResponse(code = 503, message = "Internal Server Error"),
            @ApiResponse(code = 400, message = "Invalid Input")
    })
    @PostMapping(value = "/metric", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity create(@RequestBody @Valid CreateRequest request) {
        return ok().body(repository.createMetric(request));
    }


}
