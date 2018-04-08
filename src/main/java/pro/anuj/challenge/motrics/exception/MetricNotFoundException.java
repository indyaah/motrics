package pro.anuj.challenge.motrics.exception;

import org.springframework.http.HttpStatus;

import java.text.MessageFormat;
import java.util.UUID;

import static pro.anuj.challenge.motrics.Constants.METRIC_NOT_FOUND;

public class MetricNotFoundException extends AbstractMotricException {

    private final UUID value;

    public MetricNotFoundException(UUID value) {
        this.value = value;
    }

    @Override
    public String message() {
        return MessageFormat.format(METRIC_NOT_FOUND, value);
    }

    @Override
    public HttpStatus httpStatus() {
        return null;
    }
}
