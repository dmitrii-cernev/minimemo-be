package md.cernev.minimemo.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class CustomHttpException extends RuntimeException {

    private final HttpStatus status;

    public CustomHttpException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

}
