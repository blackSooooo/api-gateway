package worksmobile.intern.apigateway.repository;

import lombok.Getter;
import org.springframework.stereotype.Repository;
import worksmobile.intern.apigateway.dto.RateLimit;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Getter
@Repository
public class RateLimitDataSet {
    private ConcurrentMap<String, RateLimit> rateLimitDataLists = new ConcurrentHashMap<>();

    public void add(String key, RateLimit value) {
        rateLimitDataLists.put(key, value);
    }

    public void delete(String key) {
        rateLimitDataLists.remove(key);
    }

    public RateLimit get(String key) {
        return rateLimitDataLists.get(key);
    }

}
