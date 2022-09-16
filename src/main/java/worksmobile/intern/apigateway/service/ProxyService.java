package worksmobile.intern.apigateway.service;

import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import worksmobile.intern.apigateway.dto.ClientRequest;

import java.util.Map;

public interface ProxyService {
    public Mono<ResponseEntity<Object>> proxy(ClientRequest clientRequest);

    public String getPath(String originalPath, Map<String, String> pathVariables);

}
