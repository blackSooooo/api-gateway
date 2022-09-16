package worksmobile.intern.apigateway.scheduler;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Flux;
import worksmobile.intern.apigateway.dto.RateLimit;
import worksmobile.intern.apigateway.repository.RateLimitDataSet;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
@AllArgsConstructor
@EnableScheduling
public class RedisScheduler {
    private ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;
    private final RateLimitDataSet rateLimitDataSet;
    private RedisScript<List<Long>> script;

    private final long REDIS_DELETE_TIME = 1000;


    @Scheduled(fixedDelay = 250)
    public void redisScheduler() {
        Map<String, RateLimit> rateLimitDataLists = rateLimitDataSet.getRateLimitDataLists();
        for (String key : rateLimitDataLists.keySet()) {
            RateLimit rateLimit = rateLimitDataLists.get(key);
            long currentTimeMillis = System.currentTimeMillis();
            // 최근 들어오지 않은 클라이언트 API 메모리에서 제거
            if (currentTimeMillis - rateLimit.getUpdateTimestamp() > REDIS_DELETE_TIME) {
                rateLimitDataSet.delete(key);
                continue;
            }

            long currentWindowKey = currentTimeMillis / 1000;
            long prevWindowKey = currentWindowKey - 1;
            ConcurrentMap<Long, AtomicInteger> localWindows = rateLimit.getLocalWindows();

            localWindows.putIfAbsent(prevWindowKey, new AtomicInteger(0));
            localWindows.putIfAbsent(currentWindowKey, new AtomicInteger(0));

            List<String> keys = Arrays.asList(key, String.valueOf(prevWindowKey), String.valueOf(currentWindowKey));
            List<Number> args = Arrays.asList(rateLimit.getRateLimit(), localWindows.get(prevWindowKey), localWindows.get(currentWindowKey));

            reactiveRedisTemplate.execute(script, keys, args)
                    .onErrorResume(throwable -> {
                        System.out.println("throwable = " + throwable);
                        return Flux.just(Arrays.asList(0L, 0L));
                    }).subscribe(result -> {
                        rateLimit.getLocalWindows().clear();
                        rateLimit.getRedisWindows().clear();

                        int prevRedisWindowCount = result.get(0).intValue();
                        int currentRedisWindowCount = result.get(1).intValue();

                        if (prevRedisWindowCount == 0) {
                            prevRedisWindowCount = rateLimit.getRateLimit();
                        }

                        rateLimit.getRedisWindows().put(prevWindowKey, new AtomicInteger(prevRedisWindowCount));
                        rateLimit.getRedisWindows().put(currentWindowKey, new AtomicInteger(currentRedisWindowCount));
                    });
        }
    }
}
