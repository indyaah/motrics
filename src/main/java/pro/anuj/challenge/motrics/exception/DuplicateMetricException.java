package pro.anuj.challenge.motrics.exception;

import org.springframework.http.HttpStatus;

import java.text.MessageFormat;
import java.util.UUID;

import static pro.anuj.challenge.motrics.Constants.TEMPLATE;

public class DuplicateMetricException extends AbstractMotricException {

    private String value;
    private UUID uuid;
    private final HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

    public DuplicateMetricException(final String value, final UUID uuid) {
        this.value = value;
        this.uuid = uuid;
    }

    @Override
    public String message() {
        return MessageFormat.format(TEMPLATE, value, uuid);
    }

    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }
}
