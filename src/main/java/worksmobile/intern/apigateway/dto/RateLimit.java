package worksmobile.intern.apigateway.dto;

import lombok.*;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
public class RateLimit {
    private int rateLimit;
    private long updateTimestamp;
    private ConcurrentMap<Long, AtomicInteger> localWindows;
    private ConcurrentMap<Long, AtomicInteger> redisWindows;
}
