package worksmobile.intern.apigateway.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import worksmobile.intern.apigateway.error.ErrorResponse;

@Getter
@AllArgsConstructor
public class ResponseException extends RuntimeException{
    private ErrorResponse errorResponse;
}
