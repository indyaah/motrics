package pro.anuj.challenge.motrics;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import springfox.documentation.spring.web.plugins.Docket;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ContextLoadingTest {

    @Autowired
    private Docket docket;

    @Test
    public void contextLoads() {
        assertThat(docket).isNotNull();
    }
}