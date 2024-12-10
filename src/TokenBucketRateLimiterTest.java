import org.junit.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TokenBucketRateLimiterTest {

    private static final String BOB = "Bob";

    // Custom clock implementation to simulate time passage
    private static class TestClock extends Clock {
        private long currentTimeMillis;

        public TestClock(long initialTimeMillis) {
            this.currentTimeMillis = initialTimeMillis;
        }

        public void setTime(long timeMillis) {
            this.currentTimeMillis = timeMillis;
        }

        public void advanceTime(long millis) {
            this.currentTimeMillis += millis;
        }

        @Override
        public Instant instant() {
            return Instant.ofEpochMilli(currentTimeMillis);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.systemDefault();
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this; // Ignoring zone changes for simplicity
        }

        public long millis() {
            return currentTimeMillis;
        }
    }

    @Test
    public void allowed_burstyTraffic_acceptsAllAccumulatedRequestsWithinRateLimitThresholds() {
        TestClock clock = new TestClock(0L);

        TokenBucketRateLimiter limiter
                = new TokenBucketRateLimiter(4, Duration.ofSeconds(1), 1, clock);

        // 0 seconds passed
        assertTrue("Bob's request 1 at timestamp=0 must pass, because bucket has 1 token available", limiter.allowed(BOB));
        clock.advanceTime(1L);
        assertFalse("Bob's request 2 at timestamp=1 must not be allowed, because bucket has 0 tokens available", limiter.allowed(BOB));

        // 4 seconds passed
        clock.advanceTime(4000L);
        assertTrue("Bob's request 3 at timestamp=4001 must pass, because bucket accumulated 4 tokens since the last refill", limiter.allowed(BOB));
        assertTrue("Bob's request 4 at timestamp=4002 must pass, because bucket has 3 tokens available", limiter.allowed(BOB));
        assertTrue("Bob's request 5 at timestamp=4003 must pass, because bucket has 2 tokens available", limiter.allowed(BOB));
        assertTrue("Bob's request 6 at timestamp=4004 must pass, because bucket has 1 token available", limiter.allowed(BOB));
        clock.advanceTime(1L);
        assertFalse("Bob's request 7 at timestamp=4005 must not be allowed, because bucket has 0 tokens available", limiter.allowed(BOB));
    }

    @Test
    public void allowed_noTokensInitially_deniesRequestUntilRefill() {
        TestClock clock = new TestClock(0L);

        TokenBucketRateLimiter limiter
                = new TokenBucketRateLimiter(2, Duration.ofSeconds(2), 1, clock);

        // No tokens initially
        assertTrue("Bob's request 1 at timestamp=0 must pass, as bucket starts with 1 token", limiter.allowed(BOB));
        assertFalse("Bob's request 2 at timestamp=1 must not be allowed, as no tokens are available", limiter.allowed(BOB));

        // Advance time to allow refill
        clock.advanceTime(2000L);
        assertTrue("Bob's request 3 at timestamp=2000 must pass, as bucket refills with 1 token", limiter.allowed(BOB));
    }
}
