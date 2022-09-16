package worksmobile.intern.apigateway.service;

import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import worksmobile.intern.apigateway.dto.*;
import worksmobile.intern.apigateway.error.Error;
import worksmobile.intern.apigateway.error.ErrorResponse;
import worksmobile.intern.apigateway.exception.ResponseException;
import worksmobile.intern.apigateway.http.ProxyRequest;
import worksmobile.intern.apigateway.repository.RoutingDataSet;

import java.util.Map;

@Service
@AllArgsConstructor
public class ProxyServiceImpl implements ProxyService{
    private final RoutingDataSet routingDataSet;

    @Override
    public Mono<ResponseEntity<Object>> proxy(ClientRequest clientRequest) {
        RoutingData routingData = routingDataSet.get(clientRequest.getPath(), clientRequest.getMethod());

        ProxyRequest proxyRequest = ProxyRequest.builder()
                .method(HttpMethod.valueOf(routingData.getMethod()))
                .path(getPath(routingData.getPath(), clientRequest.getPathVariables()))
                .baseURL(routingData.getBaseUrl())
                .query(clientRequest.getQuery())
                .body(clientRequest.getBody())
                .headers(new HttpHeaders())
                .build();

        proxyRequest.setHeadersWithService();

        WebClient webClient = WebClient.builder()
                .baseUrl(proxyRequest.getBaseURL())
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.addAll(proxyRequest.getHeaders());
                }).build();

        WebClient.RequestBodySpec requestBodySpec = webClient.method(proxyRequest.getMethod())
                .uri(uriBuilder -> {
                    UriBuilder uri = uriBuilder.path(proxyRequest.getPath());
                    if (proxyRequest.getQuery().size() != 0) {
                        uri.queryParams(proxyRequest.getQuery());
                    }
                    return uri.build();
                });

        if (proxyRequest.isBodyRequired()) {
            requestBodySpec.contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromDataBuffers(proxyRequest.getBody()));
        }

        return requestBodySpec
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(
                        HttpStatus::is4xxClientError, clientResponse -> {
                            HttpStatus statusCode = clientResponse.statusCode();
                            System.out.println("HERE COEMS" + statusCode);
                            if (statusCode == HttpStatus.UNAUTHORIZED) {
                                return Mono.error(new ResponseException(new ErrorResponse(Error.UNAUTHORIZED)));
                            } else if (statusCode == HttpStatus.NOT_FOUND) {
                                return Mono.error(new ResponseException(new ErrorResponse(Error.NOT_FOUND)));
                            } else if (statusCode == HttpStatus.FORBIDDEN) {
                                return Mono.error(new ResponseException(new ErrorResponse(Error.FORBIDDEN)));
                            } else if (statusCode == HttpStatus.TOO_MANY_REQUESTS) {
                                return Mono.error(new ResponseException(new ErrorResponse(Error.TOO_MANY_REQUEST)));
                            }
                            return Mono.error(new ResponseException(new ErrorResponse(Error.BAD_REQUEST)));
                        }
                )
                .onStatus(
                        HttpStatus::is5xxServerError, clientResponse -> Mono.error(new ResponseException(new ErrorResponse(Error.SYSTEM_ERROR)))
                )
                .toEntity(Object.class);
    }

    @Override
    public String getPath(String originalPath, Map<String, String> pathVariables) {
        String path = originalPath;
        for (String key : pathVariables.keySet()) {
            path = path.replace("{" + key + "}", pathVariables.get(key));
        }
        return path;
    }

}
