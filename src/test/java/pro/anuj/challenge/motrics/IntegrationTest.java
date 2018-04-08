package pro.anuj.challenge.motrics;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import pro.anuj.challenge.motrics.api.vo.ApiErrorResponse;
import pro.anuj.challenge.motrics.api.vo.CreateRequest;
import pro.anuj.challenge.motrics.api.vo.InsertRequest;
import pro.anuj.challenge.motrics.api.vo.ValueObject;
import pro.anuj.challenge.motrics.domain.Metric;
import pro.anuj.challenge.motrics.domain.Statistics;
import springfox.documentation.spring.web.plugins.Docket;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static java.text.MessageFormat.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static pro.anuj.challenge.motrics.Constants.DUPLICATE_METRIC;
import static pro.anuj.challenge.motrics.Constants.REQUIRED_ARGUMENT_MISSING;

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
        assertThat(entity.getStatusCode()).isEqualTo(OK);
        assertThat(value.get("status")).isEqualTo("UP");
    }

    @Test
    public void whenNewMetricWithNamePostedThenNewMetricIsCreated() {
        final String metricName = "metricName1";
        final ResponseEntity<Metric> entity = restTemplate.postForEntity("/metric", new CreateRequest(metricName), Metric.class);

        assertThat(entity.getStatusCode()).isEqualTo(OK);

        final Metric metric = entity.getBody();
        assertThat(metric).isNotNull();
        assertThat(metric.getName()).isEqualTo(metricName);
        assertThat(metric.getId().toString()).isNotBlank();
    }

    @Test
    public void whenNewMetricWithoutNamePostedThenError() {
        final ResponseEntity<ApiErrorResponse> apiErrorResponse = restTemplate.postForEntity("/metric", new CreateRequest(), ApiErrorResponse.class);

        assertThat(apiErrorResponse.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(apiErrorResponse.getBody().getMessage()).isEqualTo(format(REQUIRED_ARGUMENT_MISSING, "name"));
    }

    @Test
    public void whenDuplicateMetricTheException() {
        final String metricName = "metricName2";
        final ResponseEntity<Metric> entity = restTemplate.postForEntity("/metric", new CreateRequest(metricName), Metric.class);

        assertThat(entity.getStatusCode()).isEqualTo(OK);

        final Metric metric = entity.getBody();
        assertThat(metric).isNotNull();
        assertThat(metric.getName()).isEqualTo(metricName);
        assertThat(metric.getId().toString()).isNotBlank();

        final ResponseEntity<ApiErrorResponse> duplicateResponse = restTemplate.postForEntity("/metric", new CreateRequest(metricName), ApiErrorResponse.class);

        assertThat(duplicateResponse.getStatusCode()).isEqualTo(BAD_REQUEST);

        ApiErrorResponse apiErrorResponse = duplicateResponse.getBody();
        assertThat(apiErrorResponse.getMessage()).isEqualTo(format(DUPLICATE_METRIC, metricName, metric.getId()));
    }

    @Test
    public void whenNewMetricValuePostedThenMetricHasValues() {
        final String metricName = "metricname3";
        final ResponseEntity<Metric> entity = restTemplate.postForEntity("/metric", new CreateRequest(metricName), Metric.class);

        assertThat(entity.getStatusCode()).isEqualTo(OK);

        final Metric metric = entity.getBody();
        assertThat(metric).isNotNull();
        assertThat(metric.getName()).isEqualTo(metricName);
        assertThat(metric.getId().toString()).isNotBlank();

        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.ACCEPT, ImmutableList.of(MediaType.APPLICATION_JSON_VALUE));
        headers.put(HttpHeaders.CONTENT_TYPE, ImmutableList.of(MediaType.APPLICATION_JSON_VALUE));
        HttpEntity<InsertRequest> httpEntity = new HttpEntity<>(new InsertRequest(metric.getId(), 10.0), headers);

        final ResponseEntity<Metric> updated = restTemplate.exchange("/metric", PUT, httpEntity, Metric.class, Collections.emptyMap());

        final Metric updatedMetric = updated.getBody();
        assertThat(updatedMetric).isNotNull();
        assertThat(updatedMetric.getName()).isEqualTo(metricName);
        assertThat(updatedMetric.getId()).isEqualTo(metric.getId());
        assertThat(updatedMetric.getStatistics()).isNotNull();
        assertThat(updatedMetric.getStatistics().getSampleCount()).isEqualTo(1);
        assertThat(updatedMetric.getStatistics().getMinimum()).isEqualTo(10.0);
        assertThat(updatedMetric.getStatistics().getMaximum()).isEqualTo(10.0);
        assertThat(updatedMetric.getStatistics().getAverage()).isEqualTo(10.0);
        assertThat(updatedMetric.getStatistics().getMedian()).isEqualTo(10.0);

        final ResponseEntity<Statistics> all = restTemplate.getForEntity("/metric/" + metric.getId(), Statistics.class);

        assertThat(all).isNotNull();

        assertThat(all.getBody()).isNotNull();
        assertThat(all.getBody().getSampleCount()).isEqualTo(1);
        assertThat(all.getBody().getMinimum()).isEqualTo(10.0);
        assertThat(all.getBody().getMaximum()).isEqualTo(10.0);
        assertThat(all.getBody().getAverage()).isEqualTo(10.0);
        assertThat(all.getBody().getMedian()).isEqualTo(10.0);

        final ResponseEntity<ValueObject> min = restTemplate.getForEntity("/metric/" + metric.getId() + "/min", ValueObject.class);
        assertThat(min).isNotNull();
        assertThat(min.getBody().getValue()).isEqualTo(10.0);

        final ResponseEntity<ValueObject> max = restTemplate.getForEntity("/metric/" + metric.getId() + "/max", ValueObject.class);
        assertThat(max).isNotNull();
        assertThat(max.getBody().getValue()).isEqualTo(10.0);

        final ResponseEntity<ValueObject> avg = restTemplate.getForEntity("/metric/" + metric.getId() + "/avg", ValueObject.class);
        assertThat(avg).isNotNull();
        assertThat(avg.getBody().getValue()).isEqualTo(10.0);

        final ResponseEntity<ValueObject> median = restTemplate.getForEntity("/metric/" + metric.getId() + "/med", ValueObject.class);
        assertThat(median).isNotNull();
        assertThat(median.getBody().getValue()).isEqualTo(10.0);

        final ResponseEntity<ValueObject> count = restTemplate.getForEntity("/metric/" + metric.getId() + "/count", ValueObject.class);
        assertThat(count).isNotNull();
        assertThat(count.getBody().getValue()).isEqualTo(1);
    }

}