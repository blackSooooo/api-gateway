package worksmobile.intern.apigateway.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import worksmobile.intern.apigateway.dto.RateLimit;
import worksmobile.intern.apigateway.dto.RoutingData;
import worksmobile.intern.apigateway.error.Error;
import worksmobile.intern.apigateway.error.ErrorResponse;
import worksmobile.intern.apigateway.exception.ResponseException;
import worksmobile.intern.apigateway.repository.RateLimitDataSet;
import worksmobile.intern.apigateway.repository.RoutingDataSet;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimiter implements WebFilter {
    RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final RoutingDataSet routingDataSet;
    private final RateLimitDataSet rateLimitDataSet;

    public RateLimiter(RequestMappingHandlerMapping requestMappingHandlerMapping, RoutingDataSet routingDataSet, RateLimitDataSet rateLimitDataSet) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.routingDataSet = routingDataSet;
        this.rateLimitDataSet = rateLimitDataSet;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 해당 코드를 통해 exchange.attributes를 일찍 초기화 (작성하지 않으면 exchange.attributes는 empty)
        requestMappingHandlerMapping.getHandler(exchange);
        String path = String.valueOf((Object) exchange.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE));
        String method = request.getMethodValue();

        RoutingData routingData = getRoutingData(path, method);
        if (routingData == null) {
            return chain.filter(exchange);
        }

        String ip = getIpAddress(request);
        String key = getKey(routingData, ip);

        if (rateLimitDataSet.get(key) != null && rateLimitDataSet.get(key).getRateLimit() != routingData.getRateLimit()) {
            rateLimitDataSet.get(key).setRateLimit(routingData.getRateLimit());
        }


        rateLimitDataSet.getRateLimitDataLists()
                .putIfAbsent(key, RateLimit.builder()
                        .rateLimit(routingData.getRateLimit())
                        .updateTimestamp(System.currentTimeMillis())
                        .localWindows(new ConcurrentHashMap<>())
                        .redisWindows(new ConcurrentHashMap<>())
                        .build());

        RateLimit rateLimitData = rateLimitDataSet.get(key);

        rateLimitData.setUpdateTimestamp(System.currentTimeMillis());

        if (!isAllowed(rateLimitData)) {
            response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            throw new ResponseException(new ErrorResponse(Error.TOO_MANY_REQUEST));
        }
        return chain.filter(exchange);
    }

    // 해당 api가 rate limit을 허용하는지 체크한다.
    public boolean isAllowed(RateLimit rateLimitData) {
        long currentTimeMillis = System.currentTimeMillis();
        long currentWindowKey = currentTimeMillis / 1000;
        long prevWindowKey = currentWindowKey - 1;
        int rateLimit = rateLimitData.getRateLimit();

        ConcurrentMap<Long, AtomicInteger> localWindows = rateLimitData.getLocalWindows();
        ConcurrentMap<Long, AtomicInteger> redisWindows = rateLimitData.getRedisWindows();

        localWindows.putIfAbsent(currentWindowKey, new AtomicInteger(0));
        localWindows.putIfAbsent(prevWindowKey, new AtomicInteger(0));
        redisWindows.putIfAbsent(currentWindowKey, new AtomicInteger(0));
        redisWindows.putIfAbsent(prevWindowKey, new AtomicInteger(rateLimit));

        AtomicInteger prevRedisWindowCount = redisWindows.get(prevWindowKey);
        AtomicInteger currentRedisWindowCount = redisWindows.get(currentWindowKey);

        double prevRate = 1 - (currentTimeMillis - currentWindowKey * 1000) / 1000.0;
        long prevCount = Math.min(rateLimit, prevRedisWindowCount.get() + localWindows.get(prevWindowKey).get());

        if (prevCount * prevRate + currentRedisWindowCount.get() + localWindows.get(currentWindowKey).get() > rateLimit) {
            return false;
        }
        localWindows.get(currentWindowKey).incrementAndGet();
        return true;
    }

    public RoutingData getRoutingData(String path, String method) {
        RoutingData routingData = routingDataSet.get(path, method);
        return routingData;
    }

    public String getIpAddress(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        List<String> ip = headers.get("X-Forwarded-For");
        if (ip == null) {
            return request.getRemoteAddress().getAddress().getHostAddress();
        }
        return ip.get(0); // [최초 ip, 첫번째 proxy, 두번째 proxy, ...]
    }

    // redis에서 조회를 위한 키를 찾는다. clientAPI id + '-' + ip
    public String getKey(RoutingData routingData, String ip) {
        return routingData.getId() + '-' + ip;
    }

}
