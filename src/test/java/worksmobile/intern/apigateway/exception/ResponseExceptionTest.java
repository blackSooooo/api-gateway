package worksmobile.intern.apigateway.exception;

import org.junit.jupiter.api.Test;

import worksmobile.intern.apigateway.error.Error;
import worksmobile.intern.apigateway.error.ErrorResponse;
import static org.junit.jupiter.api.Assertions.*;

class ResponseExceptionTest {

    @Test
    void 예외_타입_검사() {
        // given
        ErrorResponse errorResponse = new ErrorResponse(Error.MISSING_PARAMETER);
        // when
        ResponseException responseException = new ResponseException(errorResponse);
        // then
        assertThrows(ResponseException.class, () -> {
            throw responseException;
        });
    }

    @Test
    void 예외_객체_확인() {
        //given
        ErrorResponse errorResponse = new ErrorResponse(Error.MISSING_PARAMETER);
        try {
            // when
            throw new ResponseException(errorResponse);
        } catch (ResponseException e) {
            // then
            assertInstanceOf(ErrorResponse.class, e.getErrorResponse());
        }
    }

    @Test
    void 예외_코드_메세지_확인() {
        // given
        ErrorResponse errorResponse = new ErrorResponse(Error.NOT_FOUND);
        try {
            // when
            throw new ResponseException(errorResponse);
        } catch (ResponseException e) {
            // then
            assertEquals(e.getErrorResponse().getCode(), 404);
            assertEquals(e.getErrorResponse().getErrorMessage(), "자원을 찾을 수 없습니다.");
        }
    }
}