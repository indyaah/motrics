package pro.anuj.challenge.motrics;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import springfox.documentation.spring.web.plugins.Docket;

import java.io.IOException;
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

        final ResponseEntity entity = this.restTemplate.getForEntity("/health", String.class);

        final Map<String, String> value = mapper.readValue(entity.getBody().toString(),
                new TypeReference<Map<String, String>>() {
                }
        );
        assertThat(entity.getStatusCode().is2xxSuccessful()).isEqualTo(true);
        assertThat(value.get("status")).isEqualTo("UP");
    }


}