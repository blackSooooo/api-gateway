package worksmobile.intern.apigateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import worksmobile.intern.apigateway.error.Error;
import worksmobile.intern.apigateway.error.ErrorResponse;

@Configuration
@Order(-2)
@AllArgsConstructor
public class GatewayRoutingErrorHandler implements ErrorWebExceptionHandler {
    private ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange serverWebExchange, Throwable throwable) {
        HttpStatus statusCode = serverWebExchange.getResponse().getStatusCode();
        ErrorResponse errorResponse;
        if (statusCode == HttpStatus.TOO_MANY_REQUESTS) {
            errorResponse = new ErrorResponse(Error.TOO_MANY_REQUEST);
        } else {
            errorResponse = new ErrorResponse(Error.NOT_FOUND_RESOURCE);
        }
        DataBufferFactory bufferFactory = serverWebExchange.getResponse().bufferFactory();
        DataBuffer dataBuffer = null;
        try {
            dataBuffer = bufferFactory.wrap(objectMapper.writeValueAsBytes(errorResponse));
        } catch (JsonProcessingException e) {
            dataBuffer = bufferFactory.wrap(errorResponse.getErrorMessage().getBytes());
        }
        serverWebExchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        serverWebExchange.getResponse().setStatusCode(HttpStatus.valueOf(errorResponse.getCode()));
        return serverWebExchange.getResponse().writeWith(Mono.just(dataBuffer));
    }
}
