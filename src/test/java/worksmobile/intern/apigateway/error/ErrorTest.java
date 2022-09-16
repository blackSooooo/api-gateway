package worksmobile.intern.apigateway.error;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorTest {

    @Test
    void Error_코드_확인() {
        // given
        Error error = Error.BAD_REQUEST;
        // when
        int code = error.getCode();
        // then
        assertThat(code).isEqualTo(400);
    }

    @Test
    void Error_메세지_확인() {
        // given
        Error error = Error.NOT_FOUND;
        // when
        String errorMessage = error.getErrorMessage();
        // then
        assertThat(errorMessage).isEqualTo("자원을 찾을 수 없습니다.");
    }
}