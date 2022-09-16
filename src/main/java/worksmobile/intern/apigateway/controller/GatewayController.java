package worksmobile.intern.apigateway.controller;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import worksmobile.intern.apigateway.dto.RoutingInformation;
import worksmobile.intern.apigateway.service.GatewayService;
import worksmobile.intern.apigateway.service.RestApiService;

import javax.annotation.PostConstruct;

@RestController
@AllArgsConstructor
@DependsOn("applicationContextProvider")
public class GatewayController {
    private final RestApiService restApiService;
    private final GatewayService gatewayService;
    private static final Logger logger = LoggerFactory.getLogger(GatewayController.class);

    @PostConstruct
    public void init() {
        restApiService.getRoutingInformation().subscribe(s -> {
            for (RoutingInformation routing : s.getBody()) {
                gatewayService.registerRouting(routing);
            }
        });
    }

    public Mono<ResponseEntity<Object>> route(ServerWebExchange exchange, @RequestBody(required = false) String body) {
        return gatewayService.routeWithClientAPI(exchange, body);
    }
    
    @RequestMapping("/monitor/l7check")
    public Mono<Void> healthCheck() {
        logger.info("l7 health check");
        return Mono.empty();
    }
}
