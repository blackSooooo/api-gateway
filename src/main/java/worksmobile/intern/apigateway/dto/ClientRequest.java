package worksmobile.intern.apigateway.dto;

import lombok.*;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;

import java.util.Map;

@Getter
@Setter
@Builder
@ToString
public class ClientRequest {
    // 라우팅 정보를 찾기 위한 path, method -> doubleKeyMap
    private String method;
    private String path;
    // 파라미터 검증을 위한 body, query, pathVariables
    private Flux<DataBuffer> body;
    private MultiValueMap<String, String> query;
    private Map<String, String> pathVariables;
    // body cache
    private Map<String, Object> convertedBody;
}
