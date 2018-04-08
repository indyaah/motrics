package pro.anuj.challenge.motrics.exception;

import org.springframework.http.HttpStatus;

public abstract class AbstractMotricException extends RuntimeException {

    public abstract String message();

    public abstract HttpStatus httpStatus();

}
