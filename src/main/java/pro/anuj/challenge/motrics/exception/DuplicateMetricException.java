package pro.anuj.challenge.motrics.exception;

import org.springframework.http.HttpStatus;

import java.text.MessageFormat;
import java.util.UUID;

import static pro.anuj.challenge.motrics.Constants.DUPLICATE_METRIC;

public class DuplicateMetricException extends AbstractMotricException {

    private String value;
    private UUID uuid;

    public DuplicateMetricException(final String value, final UUID uuid) {
        this.value = value;
        this.uuid = uuid;
    }

    @Override
    public String message() {
        return MessageFormat.format(DUPLICATE_METRIC, value, uuid);
    }

    @Override
    public HttpStatus httpStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
