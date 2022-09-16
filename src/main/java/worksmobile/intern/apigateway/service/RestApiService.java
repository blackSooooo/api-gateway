package worksmobile.intern.apigateway.service;

import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import worksmobile.intern.apigateway.dto.RoutingInformation;

import java.util.List;

public interface RestApiService {
    public Mono<ResponseEntity<List<RoutingInformation>>> getRoutingInformation();
}
