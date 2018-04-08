package pro.anuj.challenge.motrics.exception;

import org.springframework.http.HttpStatus;

public abstract class AbstractMotricsException extends RuntimeException {

    public abstract String message();

    public abstract HttpStatus httpStatus();

}
