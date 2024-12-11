# TokenBucketRateLimiter

## Overview
A Java implementation of the token bucket algorithm for rate limiting. It supports multiple users, configurable limits, and bursty traffic within thresholds.

## Features
- Configurable capacity, refill period, and tokens per period.
- Isolated buckets for each user.
- Simple and lightweight.


###Notes
- Not thread-safe; synchronization is required for concurrent use.
- Designed for lightweight use cases.

## Usage
### Constructor

```
TokenBucketRateLimiter(int capacity, Duration period, int tokensPerPeriod, Clock clock)

boolean allowed(String userId)

TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(5, Duration.ofSeconds(1), 1, Clock.systemDefaultZone());
System.out.println(limiter.allowed("User1")); // true
System.out.println(limiter.allowed("User1")); // false (if bucket is empty)
```
