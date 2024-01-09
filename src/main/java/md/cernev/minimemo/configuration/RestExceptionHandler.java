package md.cernev.minimemo.configuration;

import lombok.RequiredArgsConstructor;
import md.cernev.minimemo.dto.ErrorDto;
import md.cernev.minimemo.util.CustomHttpException;
import md.cernev.minimemo.util.DataBufferWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RestExceptionHandler implements ErrorWebExceptionHandler {
    private final DataBufferWriter bufferWriter;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        if (ex instanceof CustomHttpException) {
            status = ((CustomHttpException) ex).getStatus();
        }

        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }

        exchange.getResponse().setStatusCode(status);
        return bufferWriter.write(exchange.getResponse(), new ErrorDto(ex.getMessage()));
    }
}
