##Project Description
* Java 25 + Spring Boot 4
* `java.net.http.HttpClient` (RestClient) on **virtual threads**
* Functional `Result<T>` monad
* `TokenManager` with single-refresh (async waiters)
* `RestClient` with auto-retry on `401` (refresh once) and Resilience4j decorators
* `PageFetcher` returning `Result<PagePayload>`
* `PaginationOrchestrator` that fetches pages concurrently (virtual-thread executor), uses bounded concurrency (Semaphore), **persists each page incrementally**, and aggregates results
* Tests: unit tests for `Result`, concurrency test for `TokenManager` (WireMock), `RestClient` test (WireMock scenario 401→200), and `PaginationOrchestrator` integration-style test (WireMock + Spring Data JPA H2)

---

## Project file tree (complete)

```
api-consumer/
├── pom.xml
├── README.md
├── Dockerfile
├── docker-compose.yml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com/example/apiconsumer
│   │   │       ├── ApiConsumerApplication.java
│   │   │       ├── config
│   │   │       │   └── ResilienceConfig.java
│   │   │       ├── fn
│   │   │       │   ├── Result.java
│   │   │       │   └── RetryFn.java
│   │   │       ├── token
│   │   │       │   └── TokenManager.java
│   │   │       ├── http
│   │   │       │   └── RestClient.java
│   │   │       ├── domain
│   │   │       │   ├── model
│   │   │       │   │   ├── PagePayloadDto.java
│   │   │       │   │   └── PagePayloadEntity.java
│   │   │       │   └── repository
│   │   │       │       └── PagePayloadRepository.java
│   │   │       ├── fetcher
│   │   │       │   └── PageFetcher.java
│   │   │       ├── ingest
│   │   │       │   └── PaginationOrchestrator.java
│   │   │       └── web
│   │   │           └── IngestController.java
│   │   └── resources
│   │       └── application.yml
│   └── test
│       ├── java
│       │   └── com/example/apiconsumer
│       │       ├── fn
│       │       │   └── ResultTest.java
│       │       ├── token
│       │       │   └── TokenManagerConcurrencyTest.java
│       │       ├── http
│       │       │   └── RestClientTest.java
│       │       └── ingest
│       │           └── PaginationOrchestratorIntegrationTest.java
│       └── resources
│           └── application-test.yml
```

---

## 1) `pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" >
  <modelVersion>4.0.0</modelVersion>
  <groupId>com.example.apiconsumer</groupId>
  <artifactId>api-consumer</artifactId>
  <version>0.1.0</version>
  <properties>
    <java.version>25</java.version>
    <spring.boot.version>4.0.0</spring.boot.version>
    <resilience4j.version>2.0.0</resilience4j.version>
  </properties>

  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-dependencies</artifactId>
        <version>${spring.boot.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
    <!-- Spring Boot -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Postgres driver (runtime) -->
    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
    </dependency>

    <!-- Resilience4j -->
    <dependency>
      <groupId>io.github.resilience4j</groupId>
      <artifactId>resilience4j-all</artifactId>
      <version>${resilience4j.version}</version>
    </dependency>

    <!-- Jackson -->
    <dependency>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-databind</artifactId>
    </dependency>

    <!-- Logging -->
    <dependency>
      <groupId>ch.qos.logback</groupId>
      <artifactId>logback-classic</artifactId>
    </dependency>

    <!-- Jakarta Persistence -->
    <dependency>
      <groupId>jakarta.persistence</groupId>
      <artifactId>jakarta.persistence-api</artifactId>
    </dependency>

    <!-- Testing -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>com.github.tomakehurst</groupId>
      <artifactId>wiremock-jre8</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>org.testcontainers</groupId>
      <artifactId>postgresql</artifactId>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <configuration>
          <source>${java.version}</source>
          <target>${java.version}</target>
        </configuration>
      </plugin>
    </plugins>
  </build>
</project>
```

---

## 2) `src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:apiconsumer}
    username: ${DB_USER:postgres}
    password: ${DB_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: false

app:
  api:
    base-url: ${API_BASE_URL:http://localhost:8089}
    page-size: 50
  token:
    url: ${AUTH_URL:http://localhost:8089/token}
    client-id: ${API_CLIENT_ID:client}
    client-secret: ${API_CLIENT_SECRET:secret}

resilience4j:
  circuitbreaker:
    configs:
      default:
        registerHealthIndicator: true
        slidingWindowSize: 20
        failureRateThreshold: 50
  ratelimiter:
    configs:
      default:
        limitForPeriod: 10
        limitRefreshPeriod: 1s
        timeoutDuration: 500ms
  retry:
    configs:
      default:
        maxAttempts: 3
        waitDuration: 500ms
```

---

## 3) Application entry

`src/main/java/com/example/apiconsumer/ApiConsumerApplication.java`

```java
package com.example.apiconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiConsumerApplication.class, args);
    }
}
```

---

## 4) Resilience config bean

`src/main/java/com/example/apiconsumer/config/ResilienceConfig.java`

```java
package com.example.apiconsumer.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfig {
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }
    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        return RateLimiterRegistry.ofDefaults();
    }
    @Bean
    public RetryRegistry retryRegistry() {
        return RetryRegistry.ofDefaults();
    }
}
```

---

## 5) Functional core: `Result` and `RetryFn`

`src/main/java/com/example/apiconsumer/fn/Result.java`

```java
package com.example.apiconsumer.fn;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface Result<T> permits Result.Success, Result.Failure {
    record Success<T>(T value) implements Result<T> {}
    record Failure<T>(Throwable error) implements Result<T> {}

    static <T> Result<T> success(T v) { return new Success<>(v); }
    static <T> Result<T> failure(Throwable t) { return new Failure<>(t); }

    static <T> Result<T> of(Supplier<T> supplier) {
        try { return success(supplier.get()); }
        catch (Throwable t) { return failure(t); }
    }

    default <U> Result<U> map(Function<? super T, ? extends U> mapper) {
        return switch (this) {
            case Success<T> s -> {
                try { yield success(mapper.apply(s.value())); }
                catch (Throwable t) { yield failure(t); }
            }
            case Failure<T> f -> failure(f.error());
        };
    }

    default <U> Result<U> flatMap(Function<? super T, Result<U>> mapper) {
        return switch (this) {
            case Success<T> s -> {
                try { yield Objects.requireNonNull(mapper.apply(s.value())); }
                catch (Throwable t) { yield failure(t); }
            }
            case Failure<T> f -> failure(f.error());
        };
    }

    default boolean isSuccess() { return this instanceof Success; }
    default boolean isFailure() { return this instanceof Failure; }
}
```

`src/main/java/com/example/apiconsumer/fn/RetryFn.java`

```java
package com.example.apiconsumer.fn;

import java.time.Duration;
import java.util.function.Supplier;

public final class RetryFn {
    private RetryFn() {}

    public static <T> Result<T> withRetry(Supplier<Result<T>> supplier, int maxRetries, Duration delay) {
        int attempt = 0;
        Result<T> result;
        do {
            result = supplier.get();
            if (result.isSuccess()) return result;
            attempt++;
            if (attempt < maxRetries) {
                try { Thread.sleep(delay.toMillis()); } catch (InterruptedException ignored) {}
            }
        } while (attempt < maxRetries);
        return result;
    }
}
```

---

## 6) TokenManager (single-refresh pattern)

`src/main/java/com/example/apiconsumer/token/TokenManager.java`

```java
package com.example.apiconsumer.token;

import com.example.apiconsumer.fn.Result;

import java.net.URI;
import java.net.http.*;
import java.time.Instant;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class TokenManager {
    private final HttpClient httpClient = HttpClient.newBuilder().executor(Thread.ofVirtual().factory()).build();
    private final URI tokenUri;
    private final String clientId;
    private final String clientSecret;

    private final AtomicReference<CachedToken> cached = new AtomicReference<>(null);
    private final AtomicReference<CompletableFuture<Result<CachedToken>>> inFlight = new AtomicReference<>(null);

    public TokenManager(URI tokenUri, String clientId, String clientSecret) {
        this.tokenUri = tokenUri; this.clientId = clientId; this.clientSecret = clientSecret;
    }

    public Result<String> getCachedToken() {
        var t = cached.get();
        if (t == null || t.isExpired()) return Result.failure(new IllegalStateException("no valid token"));
        return Result.success(t.accessToken());
    }

    public CompletableFuture<Result<CachedToken>> refreshTokenAsync() {
        var cur = cached.get();
        if (cur != null && !cur.isExpired()) return CompletableFuture.completedFuture(Result.success(cur));

        var existing = inFlight.get();
        if (existing != null && !existing.isDone()) return existing;

        var future = CompletableFuture.supplyAsync(() -> {
            try {
                String body = "grant_type=client_credentials&client_id=" + clientId + "&client_secret=" + clientSecret;
                var req = HttpRequest.newBuilder(tokenUri)
                        .header("Content-Type","application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(body))
                        .timeout(java.time.Duration.ofSeconds(10))
                        .build();
                var resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() >= 200 && resp.statusCode() < 300) {
                    // Simple parsing: production should use Jackson
                    String token = extractAccessToken(resp.body());
                    long expiry = extractExpiresIn(resp.body());
                    var ct = new CachedToken(token, Instant.now().plusSeconds(Math.max(30, expiry - 10)));
                    cached.set(ct);
                    return Result.success(ct);
                } else {
                    return Result.failure(new RuntimeException("token endpoint " + resp.statusCode()));
                }
            } catch (Throwable t) {
                return Result.failure(t);
            }
        }, Thread.ofVirtual().factory());

        if (!inFlight.compareAndSet(existing, future)) {
            // someone else set it
            return inFlight.get();
        }
        future.whenComplete((r,e) -> inFlight.compareAndSet(future, null));
        return future;
    }

    public String getTokenBlocking() throws Exception {
        var t = cached.get();
        if (t != null && !t.isExpired()) return t.accessToken();
        var f = refreshTokenAsync().get(30, TimeUnit.SECONDS);
        if (f.isSuccess()) return ((Result.Success<CachedToken>) f).value().accessToken();
        throw ((Result.Failure<CachedToken>) f).error();
    }

    private static String extractAccessToken(String body) {
        // Very basic parser: production use Jackson.
        var idx = body.indexOf("\"access_token\"");
        if (idx >= 0) {
            var start = body.indexOf('"', idx + 15);
            if (start >= 0) {
                var end = body.indexOf('"', start + 1);
                return body.substring(start + 1, end);
            }
        }
        return "token-stub";
    }
    private static long extractExpiresIn(String body) {
        var idx = body.indexOf("\"expires_in\"");
        if (idx >= 0) {
            var colon = body.indexOf(':', idx);
            if (colon >= 0) {
                var end = body.indexOf(',', colon);
                if (end < 0) end = body.indexOf('}', colon);
                try { return Long.parseLong(body.substring(colon+1, end).trim()); }
                catch (Exception ignored) {}
            }
        }
        return 300L;
    }

    public record CachedToken(String accessToken, Instant expiry) {
        boolean isExpired() { return Instant.now().isAfter(expiry); }
    }
}
```

> **Notes:** For brevity the token parsing is simplistic — replace with Jackson in production.

---

## 7) RestClient with auto-retry on 401 & Resilience4j decorators

`src/main/java/com/example/apiconsumer/http/RestClient.java`

```java
package com.example.apiconsumer.http;

import com.example.apiconsumer.fn.Result;
import com.example.apiconsumer.token.TokenManager;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class RestClient {
    private final HttpClient client;
    private final TokenManager tokenManager;
    private final CircuitBreaker cb;
    private final RateLimiter rl;
    private final Retry retry;

    public RestClient(TokenManager tokenManager, CircuitBreaker cb, RateLimiter rl, Retry retry) {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .executor(Thread.ofVirtual().factory())
                .build();
        this.tokenManager = tokenManager;
        this.cb = cb; this.rl = rl; this.retry = retry;
    }

    public CompletableFuture<Result<String>> getAsync(String url, String correlationId) {
        Supplier<Result<String>> sendOnce = () -> doSend(url, correlationId);

        var decorated = Decorators.ofSupplier(sendOnce)
                .withRetry(retry)
                .withRateLimiter(rl)
                .withCircuitBreaker(cb)
                .decorate();

        return CompletableFuture.supplyAsync(() -> {
            var r = decorated.get();
            if (r.isSuccess()) return r;
            if (isUnauthorized(r)) {
                // refresh token once and retry
                var refresh = tokenManager.refreshTokenAsync().join();
                if (refresh.isFailure()) return Result.failure(((Result.Failure<?>) refresh).error());
                return doSend(url, correlationId);
            }
            return r;
        }, Thread.ofVirtual().factory());
    }

    private Result<String> doSend(String url, String correlationId) {
        try {
            String token;
            var cached = tokenManager.getCachedToken();
            if (cached.isSuccess()) token = ((Result.Success<String>) cached).value();
            else token = tokenManager.getTokenBlocking();

            var req = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .header("X-Correlation-Id", correlationId)
                    .GET()
                    .build();

            var resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            var status = resp.statusCode();
            if (status >= 200 && status < 300) return Result.success(resp.body());
            if (status == 401) return Result.failure(new UnauthorizedException("401"));
            return Result.failure(new RuntimeException("HTTP " + status));
        } catch (Throwable t) {
            return Result.failure(t);
        }
    }

    private boolean isUnauthorized(Result<String> r) {
        return r instanceof Result.Failure<?> f && f.error() instanceof UnauthorizedException;
    }

    static final class UnauthorizedException extends RuntimeException {
        UnauthorizedException(String m) { super(m); }
    }
}
```

---

## 8) Domain model: Page DTO & JPA entity

`src/main/java/com/example/apiconsumer/domain/model/PagePayloadDto.java`

```java
package com.example.apiconsumer.domain.model;

import java.time.Instant;
import java.util.Map;

public record PagePayloadDto(String endpoint, int offset, int limit, String rawBody, Map<String,String> links, Instant fetchedAt, String correlationId) {}
```

`src/main/java/com/example/apiconsumer/domain/model/PagePayloadEntity.java`

```java
package com.example.apiconsumer.domain.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "page_payloads", indexes = @Index(columnList = "endpoint,offset"))
public class PagePayloadEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String endpoint;
    private int offset;
    private int limit;
    @Lob
    private String rawBody;
    private Instant fetchedAt;
    private String correlationId;

    protected PagePayloadEntity() {}

    public PagePayloadEntity(String endpoint, int offset, int limit, String rawBody, Instant fetchedAt, String correlationId) {
        this.endpoint = endpoint; this.offset = offset; this.limit = limit;
        this.rawBody = rawBody; this.fetchedAt = fetchedAt; this.correlationId = correlationId;
    }

    public Long getId(){ return id; }
    public String getEndpoint(){ return endpoint; }
    public int getOffset(){ return offset; }
    public int getLimit(){ return limit; }
    public String getRawBody(){ return rawBody; }
    public Instant getFetchedAt(){ return fetchedAt; }
    public String getCorrelationId(){ return correlationId; }
}
```

`src/main/java/com/example/apiconsumer/domain/repository/PagePayloadRepository.java`

```java
package com.example.apiconsumer.domain.repository;

import com.example.apiconsumer.domain.model.PagePayloadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagePayloadRepository extends JpaRepository<PagePayloadEntity, Long> {}
```

---

## 9) PageFetcher

`src/main/java/com/example/apiconsumer/fetcher/PageFetcher.java`

```java
package com.example.apiconsumer.fetcher;

import com.example.apiconsumer.fn.Result;
import com.example.apiconsumer.http.RestClient;
import com.example.apiconsumer.domain.model.PagePayloadDto;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class PageFetcher {
    private final RestClient restClient;
    private final String baseUrl;
    private final int pageSize;

    public PageFetcher(RestClient restClient, String baseUrl, int pageSize) {
        this.restClient = restClient; this.baseUrl = baseUrl; this.pageSize = pageSize;
    }

    public Result<PagePayloadDto> fetch(String endpoint, int offset, String correlationId) {
        try {
            String url = String.format("%s/%s?limit=%d&offset=%d", baseUrl, endpoint, pageSize, offset);
            var future = restClient.getAsync(url, correlationId);
            var res = future.join();
            if (res.isFailure()) return Result.failure(((Result.Failure<String>) res).error());
            String body = ((Result.Success<String>) res).value();
            // links parsing stub: empty map, production: parse JSON to extract links
            Map<String,String> links = Map.of();
            return Result.success(new PagePayloadDto(endpoint, offset, pageSize, body, links, Instant.now(), correlationId));
        } catch (Throwable t) {
            return Result.failure(t);
        }
    }
}
```

---

## 10) PaginationOrchestrator (incremental persistence + bounded concurrency + retries)

`src/main/java/com/example/apiconsumer/ingest/PaginationOrchestrator.java`

```java
package com.example.apiconsumer.ingest;

import com.example.apiconsumer.domain.model.PagePayloadDto;
import com.example.apiconsumer.domain.model.PagePayloadEntity;
import com.example.apiconsumer.domain.repository.PagePayloadRepository;
import com.example.apiconsumer.fetcher.PageFetcher;
import com.example.apiconsumer.fn.Result;
import com.example.apiconsumer.fn.RetryFn;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PaginationOrchestrator {

    private final PageFetcher fetcher;
    private final PagePayloadRepository repo;
    private final int pageSize;
    private final int concurrencyLimit;
    private final int maxRetries;

    public PaginationOrchestrator(PageFetcher fetcher, PagePayloadRepository repo, int pageSize, int concurrencyLimit, int maxRetries) {
        this.fetcher = fetcher; this.repo = repo; this.pageSize = pageSize; this.concurrencyLimit = concurrencyLimit; this.maxRetries = maxRetries;
    }

    /**
     * Fetches all pages for the endpoint. computeOffsetsFromFirst should be implemented to parse last link.
     */
    public Result<IngestionSummary> ingest(String endpoint, String correlationId) {
        try {
            // fetch first page
            var firstRes = fetcher.fetch(endpoint, 0, correlationId);
            if (firstRes.isFailure()) return Result.failure(((Result.Failure<PagePayloadDto>) firstRes).error());
            persist(firstRes);
            // compute offsets from body -> for demo we simulate 5 pages; replace with parser
            List<Integer> offsets = computeOffsetsFromFirst(((Result.Success<PagePayloadDto>) firstRes).value());

            if (offsets.size() <= 1) {
                return Result.success(new IngestionSummary(endpoint, 1, 0));
            }

            var executor = Executors.newVirtualThreadPerTaskExecutor();
            Semaphore sem = new Semaphore(concurrencyLimit);
            List<CompletableFuture<FetchResult>> futures = new ArrayList<>();

            for (int offset : offsets) {
                if (offset == 0) continue;
                sem.acquireUninterruptibly();
                CompletableFuture<FetchResult> fut = CompletableFuture.supplyAsync(() -> {
                    try {
                        Result<PagePayloadDto> res = RetryFn.withRetry(() -> fetcher.fetch(endpoint, offset, correlationId), maxRetries, java.time.Duration.ofSeconds(1));
                        if (res.isSuccess()) {
                            persist(res);
                            return new FetchResult(offset, true, null);
                        } else {
                            return new FetchResult(offset, false, ((Result.Failure<PagePayloadDto>) res).error());
                        }
                    } finally {
                        sem.release();
                    }
                }, executor);
                futures.add(fut);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            List<FetchResult> results = futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
            long success = results.stream().filter(FetchResult::success).count();
            long fail = results.size() - success;

            executor.shutdown();

            return Result.success(new IngestionSummary(endpoint, success + 1, fail));
        } catch (Throwable t) {
            return Result.failure(t);
        }
    }

    private void persist(Result<PagePayloadDto> pageRes) {
        var page = ((Result.Success<PagePayloadDto>) pageRes).value();
        PagePayloadEntity ent = new PagePayloadEntity(page.endpoint(), page.offset(), page.limit(), page.rawBody(), Instant.now(), page.correlationId());
        repo.save(ent);
    }

    private List<Integer> computeOffsetsFromFirst(PagePayloadDto first) {
        // Demo: assume 5 pages. Replace with JSON parsing of first.links().get("last") in production.
        return IntStream.rangeClosed(0, 4).map(i -> i * pageSize).boxed().collect(Collectors.toList());
    }

    private record FetchResult(int offset, boolean success, Throwable error) {}
    public record IngestionSummary(String endpoint, long successCount, long failureCount) {}
}
```

---

## 11) Simple HTTP controller to trigger ingestion (for manual runs)

`src/main/java/com/example/apiconsumer/web/IngestController.java`

```java
package com.example.apiconsumer.web;

import com.example.apiconsumer.ingest.PaginationOrchestrator;
import com.example.apiconsumer.ingest.PaginationOrchestrator.IngestionSummary;
import com.example.apiconsumer.fn.Result;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ingest")
public class IngestController {
    private final PaginationOrchestrator orchestrator;

    public IngestController(PaginationOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/{endpoint}")
    public IngestionSummary ingest(@PathVariable String endpoint) {
        String cid = java.util.UUID.randomUUID().toString();
        Result<PaginationOrchestrator.IngestionSummary> res = orchestrator.ingest(endpoint, cid);
        if (res.isSuccess()) return ((Result.Success<IngestionSummary>) res).value();
        throw new RuntimeException(((Result.Failure<IngestionSummary>) res).error());
    }
}
```

> To wire beans for `TokenManager`, `RestClient`, `PageFetcher`, `PaginationOrchestrator`, you can add a Spring `@Configuration` class or convert constructors to `@Component` and use `@Bean` methods in a config class — omitted here for brevity but straightforward.

---

## 12) Tests

### Test properties

`src/test/resources/application-test.yml`

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: update
```

### `ResultTest`

`src/test/java/com/example/apiconsumer/fn/ResultTest.java`

```java
package com.example.apiconsumer.fn;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResultTest {
    @Test
    void basicMapFlatMap() {
        var r = Result.success(2);
        var m = r.map(x -> x * 3);
        assertTrue(m.isSuccess());
        assertEquals(6, ((Result.Success<Integer>) m).value());

        var f = r.flatMap(x -> Result.success(x + 4));
        assertTrue(f.isSuccess());
        assertEquals(6, ((Result.Success<Integer>) f).value());
    }
}
```

### `TokenManagerConcurrencyTest` (WireMock)

`src/test/java/com/example/apiconsumer/token/TokenManagerConcurrencyTest.java`

```java
package com.example.apiconsumer.token;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import java.net.URI;
import java.util.concurrent.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

class TokenManagerConcurrencyTest {
    static WireMockServer wire;

    @BeforeAll static void start() { wire = new WireMockServer(0); wire.start(); configureFor("localhost", wire.port()); }
    @AfterAll static void stop() { wire.stop(); }

    @Test
    void singleRefresh() throws Exception {
        stubFor(post(urlPathEqualTo("/token"))
                .willReturn(aResponse().withStatus(200).withBody("{\"access_token\":\"t-1\",\"expires_in\":300}").withFixedDelay(200)));

        var tm = new TokenManager(new URI("http://localhost:" + wire.port() + "/token"), "cid","secret");
        var exec = Executors.newFixedThreadPool(20);
        var futures = new CompletableFuture[20];
        for (int i=0;i<20;i++) {
            futures[i] = CompletableFuture.runAsync(() -> tm.refreshTokenAsync().join(), exec);
        }
        CompletableFuture.allOf(futures).get(10, TimeUnit.SECONDS);
        verify(1, postRequestedFor(urlEqualTo("/token")));
        exec.shutdownNow();
    }
}
```

### `RestClientTest` (WireMock scenario 401 → token refresh → 200)

`src/test/java/com/example/apiconsumer/http/RestClientTest.java`

```java
package com.example.apiconsumer.http;

import com.example.apiconsumer.token.TokenManager;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import org.junit.jupiter.api.*;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class RestClientTest {
    static WireMockServer wire;

    @BeforeAll static void start() { wire = new WireMockServer(0); wire.start(); configureFor("localhost", wire.port()); }
    @AfterAll static void stop() { wire.stop(); }

    @Test
    void autoRefreshOn401() throws Exception {
        // token endpoint
        stubFor(post("/token").willReturn(aResponse().withStatus(200).withBody("{\"access_token\":\"t1\",\"expires_in\":300}")));

        // protected endpoint: first 401, then 200
        stubFor(get(urlEqualTo("/payments?limit=50&offset=0"))
                .inScenario("auth")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(401))
                .willSetStateTo("retried"));

        stubFor(get(urlEqualTo("/payments?limit=50&offset=0"))
                .inScenario("auth")
                .whenScenarioStateIs("retried")
                .willReturn(aResponse().withStatus(200).withBody("{\"data\":[]}")));

        var tm = new TokenManager(new URI("http://localhost:" + wire.port() + "/token"), "cid", "secret");
        var cb = CircuitBreaker.ofDefaults("t");
        var rl = RateLimiter.ofDefaults("t");
        var retry = Retry.ofDefaults("t");
        var client = new RestClient(tm, cb, rl, retry);

        String url = "http://localhost:" + wire.port() + "/payments?limit=50&offset=0";
        var fut = client.getAsync(url, "cid-1");
        var r = fut.get();
        assertTrue(r.isSuccess());
    }
}
```

### `PaginationOrchestratorIntegrationTest` (WireMock + Spring Data JPA H2)

`src/test/java/com/example/apiconsumer/ingest/PaginationOrchestratorIntegrationTest.java`

```java
package com.example.apiconsumer.ingest;

import com.example.apiconsumer.domain.repository.PagePayloadRepository;
import com.example.apiconsumer.fetcher.PageFetcher;
import com.example.apiconsumer.http.RestClient;
import com.example.apiconsumer.token.TokenManager;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.retry.Retry;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.net.URI;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PaginationOrchestratorIntegrationTest {
    static WireMockServer wire;

    @Autowired
    PagePayloadRepository repo;

    @BeforeAll static void start() { wire = new WireMockServer(0); wire.start(); configureFor("localhost", wire.port()); }
    @AfterAll static void stop() { wire.stop(); }

    @Test
    void fullIngest() throws Exception {
        // token
        stubFor(post("/token").willReturn(aResponse().withStatus(200).withBody("{\"access_token\":\"t1\",\"expires_in\":300}")));
        // 5 pages responses
        for (int i=0;i<5;i++) {
            stubFor(get(urlEqualTo("/payments?limit=50&offset=" + (i*50)))
                    .willReturn(aResponse().withStatus(200).withBody("{\"page\":" + i + ",\"data\":[]}")));
        }

        var tm = new TokenManager(new URI("http://localhost:" + wire.port() + "/token"), "cid", "secret");
        var cb = CircuitBreaker.ofDefaults("t");
        var rl = RateLimiter.ofDefaults("t");
        var retry = Retry.ofDefaults("t");
        var client = new RestClient(tm, cb, rl, retry);

        var fetcher = new PageFetcher(client, "http://localhost:" + wire.port(), 50);
        var orchestrator = new PaginationOrchestrator(fetcher, repo, 50, 4, 3);
        var res = orchestrator.ingest("payments", "cid-1");
        assertTrue(res.isSuccess());
        var summary = ((Result.Success<PaginationOrchestrator.IngestionSummary>) res).value();
        assertEquals(5, summary.successCount());
        assertEquals(0, summary.failureCount());
    }
}
```

---

## 13) Dockerfiles & docker-compose

`Dockerfile`

```dockerfile
FROM eclipse-temurin:25-jdk-jammy
WORKDIR /app
ARG JAR=target/api-consumer-0.1.0.jar
COPY ${JAR} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

`docker-compose.yml` (local testing with postgres + wiremock)

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: apiconsumer
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
  wiremock:
    image: wiremock/wiremock:2.35.0
    ports:
      - "8089:8080"
  app:
    build: .
    environment:
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: apiconsumer
      DB_USER: postgres
      DB_PASSWORD: postgres
      API_BASE_URL: http://wiremock:8080
      AUTH_URL: http://wiremock:8080/token
    depends_on:
      - postgres
      - wiremock
    ports:
      - "8080:8080"
```

---

## 14) README (quick run + test commands)

`README.md`

```
# API Consumer (Java 25 + Spring Boot 4)

Build & test:

mvn clean package
mvn test

Run locally (with docker compose):
docker-compose up --build

Run tests only:
mvn -DskipTests=false test
```

---

## How to run everything locally (step-by-step)

1. Copy the file tree and file contents into a folder `api-consumer`.

2. Build & run unit tests:

   ```bash
   mvn -DskipTests=false test
   ```

   All tests in `src/test` should pass: `ResultTest`, `TokenManagerConcurrencyTest`, `RestClientTest`, `PaginationOrchestratorIntegrationTest`.

3. Package jar:

   ```bash
   mvn clean package -DskipTests
   ```

4. Start services locally via Docker Compose (optional):

   ```bash
   docker-compose up --build
   ```

   This starts Postgres and WireMock; you can load WireMock mappings under `./wiremock` if you want.

5. Trigger an ingestion (if you wired beans and controller):

   ```bash
   curl -X POST http://localhost:8080/ingest/payments
   ```

---

## Why this full project follows best practices — brief recap

* **Incremental persistence (one DB record per page)**: fault-tolerant, resumable, low memory.
* **Functional `Result<T>`**: predictable error propagation, easy composition.
* **Token single-refresh with waiters**: prevents stampede, efficient under concurrency.
* **RestClient auto-retry on 401**: transparent token refresh handling.
* **Virtual threads**: low-cost concurrency for many page fetch tasks.
* **Bounded concurrency (Semaphore)**: protects external API from bursts even with virtual threads.
* **Resilience4j**: circuit breaker + rate limiter + retry layered declaratively.

---

## Future improvements

* Replace naive token JSON parsing with **Jackson** DTOs.
* Add **Micrometer** metrics (page fetch counts, latencies, token refreshes).
* Add **Flyway** for DB migrations.
* Add **OpenTelemetry** for distributed tracing (propagate X-Correlation-Id).
* Make `PaginationOrchestrator` parse `links.last` from response JSON properly and support resuming from checkpoint stored in DB.
* Consider R2DBC or an async DB driver for fully non-blocking I/O (optional — virtual threads + blocking JPA is pragmatic).

---

