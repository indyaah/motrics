package pro.anuj.challenge.motrics;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pro.anuj.challenge.motrics.domain.Metric;
import pro.anuj.challenge.motrics.utils.StripedLockProvider;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Configuration
public class Config {

    private static final Map<UUID, Metric> METRIC_CACHE = new ConcurrentHashMap<>();
    private static final Map<String, UUID> NAME_CACHE = new ConcurrentHashMap<>();

    @Bean
    public Map<UUID, Metric> metricCache() {
        return METRIC_CACHE;
    }

    @Bean
    public Map<String, UUID> nameCache() {
        return NAME_CACHE;
    }

    @Bean
    public StripedLockProvider<UUID, Metric> stripedLockProvider() {
        return new StripedLockProvider<>(100, TimeUnit.MILLISECONDS);
    }
}
