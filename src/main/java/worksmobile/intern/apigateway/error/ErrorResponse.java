package worksmobile.intern.apigateway.error;

import lombok.Getter;

@Getter
public class ErrorResponse {
    private int code;
    private String errorMessage;

    public ErrorResponse(Error e) {
        this.code = e.getCode();
        this.errorMessage = e.getErrorMessage();
    }
}
