package worksmobile.intern.apigateway.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Error {
    NO_CONTENT(204, "No Content"),
    BAD_REQUEST(400, "잘못된 형식이나 내용입니다."),
    INVALID_PARAMETER(400, "잘못된 파라미터 입력입니다."),
    MISSING_PARAMETER(400, "필수 파라미터를 지정하지 않았습니다."),
    UNAUTHORIZED(401, "인증되지 않은 클라이언트입니다."),
    FORBIDDEN(403, "권한이 금지되었습니다."),
    NOT_FOUND_RESOURCE(404, "라우팅 정보 리소스를 찾을 수 없습니다."),
    NOT_FOUND(404, "자원을 찾을 수 없습니다."),
    TOO_MANY_REQUEST(429, "API 사용 요청량을 초과했습니다"),
    SYSTEM_ERROR(500, "서버 시스템 내부의 오류입니다.");

    private int code;
    private String errorMessage;
}
