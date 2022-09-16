package worksmobile.intern.apigateway.error;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    void ErrorResponse_코드_확인() {
        // given
        ErrorResponse errorResponse = new ErrorResponse(Error.INVALID_PARAMETER);
        // when
        int code = errorResponse.getCode();
        // then
        assertThat(code).isEqualTo(400);
    }

    @Test
    void ErrorResponse_메세지_확인() {
        // given
        ErrorResponse errorResponse = new ErrorResponse(Error.MISSING_PARAMETER);
        // when
        String errorMessage = errorResponse.getErrorMessage();
        // then
        assertThat(errorMessage).isEqualTo("필수 파라미터를 지정하지 않았습니다.");
    }
}