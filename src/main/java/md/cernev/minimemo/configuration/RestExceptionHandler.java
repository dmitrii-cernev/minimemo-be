package md.cernev.minimemo.configuration;

import md.cernev.minimemo.dto.ErrorDto;
import md.cernev.minimemo.util.CustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(value = {CustomException.class})
    @ResponseBody
    public ResponseEntity<ErrorDto> handleCustomException(CustomException e) {
        return ResponseEntity.status(e.getStatus()).body(new ErrorDto(e.getMessage()));
    }
}
