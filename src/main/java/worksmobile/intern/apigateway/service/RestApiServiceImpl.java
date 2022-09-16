package worksmobile.intern.apigateway.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import worksmobile.intern.apigateway.dto.RoutingInformation;
import worksmobile.intern.apigateway.error.Error;
import worksmobile.intern.apigateway.error.ErrorResponse;
import worksmobile.intern.apigateway.exception.ResponseException;

import java.util.List;

@Service
public class RestApiServiceImpl implements RestApiService{
    @Value("${restApiServer.path}")
    private String path;
    @Value("${restApiServer.baseUrl}")
    private String restApiServerBaseUrl;

    @Override
    public Mono<ResponseEntity<List<RoutingInformation>>> getRoutingInformation() {
        WebClient webClient = WebClient.builder()
                .baseUrl(restApiServerBaseUrl)
                .build();

        return webClient.get()
                .uri(path)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(
                        HttpStatus::is4xxClientError, clientResponse -> Mono.error(new ResponseException(new ErrorResponse(Error.NOT_FOUND_RESOURCE)))
                )
                .onStatus(
                        HttpStatus::is5xxServerError, clientResponse -> Mono.error(new ResponseException(new ErrorResponse(Error.SYSTEM_ERROR)))
                )
                .toEntityList(RoutingInformation.class);
    }

}
