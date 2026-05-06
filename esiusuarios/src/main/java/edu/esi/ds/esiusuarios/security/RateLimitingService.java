package edu.esi.ds.esiusuarios.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class RateLimitingService {

    private static final int REQUESTS_PER_WINDOW = 5;  // 5 intentos
    private static final int WINDOW_MINUTES = 15;      // cada 15 minutos

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public boolean isAllowed(String email) {
        Bucket bucket = buckets.computeIfAbsent(email, k -> createNewBucket());
        return bucket.tryConsume(1);
    }

    public long getRemainingTokens(String email) {
        Bucket bucket = buckets.get(email);
        if (bucket != null) {
            return bucket.getAvailableTokens();
        }
        return REQUESTS_PER_WINDOW;
    }

    public void reset(String email) {
        buckets.remove(email);
    }

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(
            REQUESTS_PER_WINDOW,
            Refill.intervally(REQUESTS_PER_WINDOW, Duration.ofMinutes(WINDOW_MINUTES))
        );
        return Bucket4j.builder()
            .addLimit(limit)
            .build();
    }
}