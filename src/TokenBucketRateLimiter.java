import java.time.Clock;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class TokenBucketRateLimiter {
    private final int capacity;
    private final Duration period;
    private final int tokensPerPeriod;
    private final Clock clock;
    private final Map<String, TokenBucket> userTokenBucket = new HashMap<>();

    public TokenBucketRateLimiter(int capacity, Duration period, int tokensPerPeriod, Clock clock) {
        this.capacity = capacity;
        this.period = period;
        this.tokensPerPeriod = tokensPerPeriod;
        this.clock = clock;
    }

    public boolean allowed(String userId) {
        TokenBucket bucket = userTokenBucket.computeIfAbsent(userId, k-> new TokenBucket(clock.millis(), tokensPerPeriod));
        bucket.refill();

        return bucket.consume();
    }

    private class TokenBucket {
        private long refillTimestamp;
        private long tokenCount;

        TokenBucket(long refillTimestamp, long tokenCount) {
            this.refillTimestamp = refillTimestamp;
            this.tokenCount = tokenCount;
        }

        private void refill() {
            long now = clock.millis();
            long elapsedTime = now - refillTimestamp;
            long elapsedPeriods = elapsedTime/period.toMillis();
            long availableTokens = elapsedPeriods * tokensPerPeriod;

            tokenCount = Math.min(tokenCount + availableTokens, capacity);
            refillTimestamp += elapsedPeriods * period.toMillis();
        }

        boolean consume() {
            if(tokenCount > 0) {
                tokenCount--;
                return true;
            }
            return false;
        }
    }
}