package worksmobile.intern.apigateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import worksmobile.intern.apigateway.controller.GatewayController;
import worksmobile.intern.apigateway.dto.*;
import worksmobile.intern.apigateway.repository.DoubleKeyMap;
import worksmobile.intern.apigateway.repository.RoutingDataSet;
import worksmobile.intern.apigateway.utils.BeanUtils;

import java.util.Map;

@Service
@AllArgsConstructor
public class GatewayServiceImpl implements GatewayService{
    private final ProxyService proxyService;
    private final RoutingDataSet routingDataSet;
    private final ParameterVerificationService parameterVerificationService;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(GatewayServiceImpl.class);
    private final DataBufferFactory dbf = new DefaultDataBufferFactory();

    @Override
    public Mono<ResponseEntity<Object>> routeWithClientAPI(ServerWebExchange exchange, String body) {
        ServerHttpRequest request = exchange.getRequest();

        String originalPath = String.valueOf((Object) exchange.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE));
        Map<String, String> pathVariables = exchange.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        Map<String, Object> convertedBody = convertBody(body);
        Flux<DataBuffer> originBody = body != null ? getBodyDataBuffer(body) : request.getBody();

        ClientRequest clientRequest = ClientRequest.builder()
                .method(request.getMethodValue())
                .path(originalPath)
                .query(request.getQueryParams())
                .body(originBody)
                .pathVariables(pathVariables)
                .convertedBody(convertedBody)
                .build();

        RoutingData routingData = routingDataSet.get(originalPath, request.getMethodValue());

        parameterVerificationService.checkVerification(clientRequest, routingData);

        return proxyService.proxy(clientRequest);
    }

    @Override
    public void addRoutingDataSet(RoutingInformation routingInformation) {
        String clientPath = "/" + routingInformation.getDomain() + routingInformation.getPath();
        String method = routingInformation.getMethod();
        String routingId = routingInformation.getId();

        DoubleKeyMap<String, String> key = new DoubleKeyMap<>(clientPath, method);

        RoutingData routingData = RoutingData.builder()
                .id(routingId)
                .method(method)
                .rateLimit(routingInformation.getRateLimit())
                .baseUrl(routingInformation.getBaseUrl())
                .path(routingInformation.getPath())
                .body(routingInformation.getBody())
                .query(routingInformation.getQuery())
                .pathVariable(routingInformation.getPathVariable())
                .build();

        routingDataSet.add(key, routingData);
    }

    @Override
    public void deleteRoutingDataSet(DeletedInformation deletedInformation) {
        String clientPath = "/" + deletedInformation.getDomain() + deletedInformation.getPath();
        String method = deletedInformation.getMethod();

        DoubleKeyMap<String, String> key = new DoubleKeyMap<>(clientPath, method);

        routingDataSet.delete(key);
    }

    @Override
    public void registerRequestMapping (RequestMethod method, String path) throws NoSuchMethodException {
        requestMappingHandlerMapping.registerMapping(
                RequestMappingInfo.paths(path).methods(method)
                        .produces(MediaType.APPLICATION_JSON_VALUE).build(),
                BeanUtils.getBean("gatewayController"),
                GatewayController.class.getDeclaredMethod("route", ServerWebExchange.class, String.class));
    }

    @Override
    public void unregisterRequestMapping(RequestMethod method, String path) {
        requestMappingHandlerMapping.unregisterMapping(
                RequestMappingInfo.paths(path).methods(method)
                        .produces(MediaType.APPLICATION_JSON_VALUE).build()
        );
    }

    @Override
    public void registerRouting(RoutingInformation routingInformation) {
        addRoutingDataSet(routingInformation);
        RequestMethod method = RequestMethod.valueOf(routingInformation.getMethod());
        String path = routingInformation.getDomain() + routingInformation.getPath();
        try {
            registerRequestMapping(method, path);
        } catch (NoSuchMethodException e) {
            logger.info("No such method exists");
        }
    }

    @Override
    public void unregisterRouting(DeletedInformation deletedInformation) {
        deleteRoutingDataSet(deletedInformation);
        RequestMethod method = RequestMethod.valueOf(deletedInformation.getMethod());
        String path = deletedInformation.getDomain() + deletedInformation.getPath();
        unregisterRequestMapping(method, path);
    }
    @Override
    public Map<String, Object> convertBody(String body) {
        Map<String, Object> ret = null;
        try {
            if (body != null) {
                ret = objectMapper.readValue(body, Map.class);
            }
        } catch (JsonProcessingException e) {
            logger.info("JsonProcessingException in converting body");
        }
        return ret;
    }

    @Override
    public Flux<DataBuffer> getBodyDataBuffer(String body) {
        byte[] byteArray = body.getBytes();
        return Flux.just(dbf.wrap(byteArray));
    }
}
