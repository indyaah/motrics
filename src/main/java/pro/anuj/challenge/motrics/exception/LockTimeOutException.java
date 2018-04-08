package pro.anuj.challenge.motrics.exception;

import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

public class LockTimeOutException extends AbstractMotricsException {

    @Override
    public String message() {
        return "";
    }

    @Override
    public HttpStatus httpStatus() {
        return INTERNAL_SERVER_ERROR;
    }
}
