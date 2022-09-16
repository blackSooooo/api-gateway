package worksmobile.intern.apigateway.service;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import worksmobile.intern.apigateway.dto.DeletedInformation;
import worksmobile.intern.apigateway.dto.RoutingInformation;

import java.util.Map;

public interface GatewayService {
    public Mono<ResponseEntity<Object>> routeWithClientAPI(ServerWebExchange exchange, String body);
    public void addRoutingDataSet(RoutingInformation routingInformation);
    public void deleteRoutingDataSet(DeletedInformation deletedInformation);
    public void registerRequestMapping(RequestMethod method, String path) throws NoSuchMethodException;
    public void unregisterRequestMapping(RequestMethod method, String path) ;
    public void registerRouting(RoutingInformation routingInformation);
    public void unregisterRouting(DeletedInformation deletedInformation);
    public Map<String, Object> convertBody(String body);
    public Flux<DataBuffer> getBodyDataBuffer(String body);
}
