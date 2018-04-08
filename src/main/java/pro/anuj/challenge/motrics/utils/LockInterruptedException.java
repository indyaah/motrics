package pro.anuj.challenge.motrics.utils;

import org.springframework.http.HttpStatus;
import pro.anuj.challenge.motrics.exception.AbstractMotricsException;

public class LockInterruptedException extends AbstractMotricsException {
    @Override
    public String message() {
        return null;
    }

    @Override
    public HttpStatus httpStatus() {
        return null;
    }
}
