package pro.anuj.challenge.motrics;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import pro.anuj.challenge.motrics.api.vo.ApiErrorResponse;
import pro.anuj.challenge.motrics.api.vo.CreateRequest;
import pro.anuj.challenge.motrics.domain.Metric;
import springfox.documentation.spring.web.plugins.Docket;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class IntegrationTest {


    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private Docket docket;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void whenAppStartsContextIsCorrectlyLoaded() {
        assertThat(docket).isNotNull();
    }

    @Test
    public void whenActuatorEndpointCheckThenStatusIsUp() throws IOException {

        final ResponseEntity entity = restTemplate.getForEntity("/health", String.class);

        final Map<String, String> value = mapper.readValue(entity.getBody().toString(),
                new TypeReference<Map<String, String>>() {
                }
        );
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(value.get("status")).isEqualTo("UP");
    }

    @Test
    public void whenNewMetricWithNamePostedThenNewMetricIsCreated() {
        final String metricName = "metricName";
        final ResponseEntity<Metric> entity = restTemplate.postForEntity("/metric", new CreateRequest(metricName), Metric.class);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);

        final Metric metric = entity.getBody();
        assertThat(metric).isNotNull();
        assertThat(metric.getName()).isEqualTo(metricName);
        assertThat(metric.getId().toString()).isNotBlank();
    }

    @Test
    public void whenDuplicateMetricTheException() {
        final String metricName = "DUPLICATE_METRIC";
        final ResponseEntity<Metric> entity = restTemplate.postForEntity("/metric", new CreateRequest(metricName), Metric.class);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);

        final Metric metric = entity.getBody();
        assertThat(metric).isNotNull();
        assertThat(metric.getName()).isEqualTo(metricName);
        assertThat(metric.getId().toString()).isNotBlank();

        final ResponseEntity<ApiErrorResponse> duplicateResponse = restTemplate.postForEntity("/metric", new CreateRequest(metricName), ApiErrorResponse.class);

        assertThat(duplicateResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ApiErrorResponse apiErrorResponse = duplicateResponse.getBody();
        assertThat(apiErrorResponse.getMessage()).contains(MessageFormat.format(Constants.TEMPLATE, metricName, metric.getId()));
    }
}