package org.example.limesurveyclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe LimeSurvey JSON-RPC client with optional session pooling and concurrency limit.
 */
public class LimeSurveyClient {
    private final String baseUrl;
    private final String username;
    private final String password;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final AtomicLong idCounter = new AtomicLong(1);

    // Backwards-compatible single session key (used when user calls login()/logout() or when pooling disabled)
    private volatile String sessionKey;

    // Session pool
    private final int sessionPoolSize;
    private final BlockingQueue<String> sessionPool;
    private final AtomicInteger currentSessions = new AtomicInteger(0);
    private final Object sessionCreationLock = new Object();

    // Concurrency control
    private final java.util.concurrent.Semaphore requestSemaphore;

    // Behavior
    private final boolean autoReleaseSession; // when true and pooling disabled, logout after each RPC; when pooling enabled, true means invalidate after use

    // Constructor: default pool size 0 (no pooling), unlimited concurrent requests
    public LimeSurveyClient(String baseUrl, String username, String password) {
        this(baseUrl, username, password, true, 0, Integer.MAX_VALUE);
    }

    // Backwards-compatible constructor: autoReleaseSession flag
    public LimeSurveyClient(String baseUrl, String username, String password, boolean autoReleaseSession) {
        this(baseUrl, username, password, autoReleaseSession, 0, Integer.MAX_VALUE);
    }

    // Constructor with options
    public LimeSurveyClient(String baseUrl, String username, String password, boolean autoReleaseSession, int sessionPoolSize, int maxConcurrentRequests) {
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
        this.autoReleaseSession = autoReleaseSession;
        this.sessionPoolSize = Math.max(0, sessionPoolSize);
        this.sessionPool = this.sessionPoolSize > 0 ? new LinkedBlockingQueue<>(this.sessionPoolSize) : null;
        this.requestSemaphore = maxConcurrentRequests <= 0 ? new java.util.concurrent.Semaphore(Integer.MAX_VALUE) : new java.util.concurrent.Semaphore(maxConcurrentRequests);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    // Acquire a session key from the pool or create a new one if pool not full
    private String acquirePooledSession(long timeout, TimeUnit unit) throws IOException, InterruptedException {
        if (sessionPoolSize <= 0) {
            // pooling disabled: if user logged in (sessionKey set), reuse it; otherwise create temporary session
            if (sessionKey != null) return sessionKey;
            return createSessionKeyWithRetries();
        }

        // try immediate poll
        String sk = sessionPool.poll();
        if (sk != null) return sk;

        // create new session if allowed
        synchronized (sessionCreationLock) {
            if (currentSessions.get() < sessionPoolSize) {
                String newSk = createSessionKeyWithRetries();
                currentSessions.incrementAndGet();
                return newSk;
            }
        }

        // otherwise wait for available session
        sk = sessionPool.poll(timeout, unit);
        if (sk == null) throw new org.example.limesurveyclient.exceptions.LimeSurveyException("Timeout waiting for session from pool");
        return sk;
    }

    private void returnPooledSession(String sk) {
        if (sk == null) return;
        if (sessionPoolSize <= 0) {
            // no pooling: release immediately if autoReleaseSession true
            if (autoReleaseSession) {
                try { releaseSessionKey(sk); } catch (Exception ignored) {}
            }
            return;
        }
        // pooling enabled: offer back to pool, if failed, invalidate
        boolean offered = sessionPool.offer(sk);
        if (!offered) {
            try { releaseSessionKey(sk); } catch (Exception ignored) {}
            currentSessions.decrementAndGet();
        }
    }

    private String createSessionKeyWithRetries() throws IOException, InterruptedException {
        int attempts = 0;
        while (attempts < 3) {
            attempts++;
            List<Object> params = new ArrayList<>();
            params.add(username);
            params.add(password);
            JsonNode resp = callRpc("get_session_key", params);
            if (resp != null && !resp.isNull() && !resp.asText().isEmpty()) {
                return resp.asText();
            }
            Thread.sleep(500L * attempts);
        }
        throw new org.example.limesurveyclient.exceptions.LimeSurveyException("Failed to obtain session key after retries");
    }

    private void releaseSessionKey(String sk) throws IOException, InterruptedException {
        if (sk == null) return;
        List<Object> params = new ArrayList<>();
        params.add(sk);
        callRpc("release_session_key", params);
    }

    // Backwards-compatible login/logout
    public synchronized void login() throws IOException, InterruptedException {
        if (sessionKey != null) return;
        sessionKey = createSessionKeyWithRetries();
    }

    public synchronized void logout() throws IOException, InterruptedException {
        if (sessionKey == null) return;
        try {
            releaseSessionKey(sessionKey);
        } finally {
            sessionKey = null;
        }
    }

    private JsonNode callRpc(String method, List<Object> params) throws IOException, InterruptedException {
        long id = idCounter.getAndIncrement();
        JsonRpcRequest req = new JsonRpcRequest("2.0", method, params, id);
        String body = objectMapper.writeValueAsString(req);
        HttpRequest httpReq = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        HttpResponse<String> httpResp = httpClient.send(httpReq, HttpResponse.BodyHandlers.ofString());
        if (httpResp.statusCode() >= 400) throw new org.example.limesurveyclient.exceptions.LimeSurveyException("HTTP error: " + httpResp.statusCode());
        JsonRpcResponse rpcResp = objectMapper.readValue(httpResp.body(), JsonRpcResponse.class);
        if (rpcResp.getError() != null) throw new org.example.limesurveyclient.exceptions.LimeSurveyException(rpcResp.getError().toString());
        return rpcResp.getResult();
    }

    // Public API methods — these acquire a session (from pool or fresh), perform the RPC, and return or invalidate the session appropriately
    public JsonNode copySurvey(int surveyId, String newSurveyName) throws IOException, InterruptedException {
        requestSemaphore.acquireUninterruptibly();
        String sk = null;
        try {
            sk = acquirePooledSession(30, TimeUnit.SECONDS);
            List<Object> params = new ArrayList<>();
            params.add(sk);
            params.add(surveyId);
            params.add(newSurveyName);
            return callRpc("copy_survey", params);
        } finally {
            // decide whether to return to pool or invalidate
            if (sessionPoolSize > 0) {
                if (autoReleaseSession) {
                    try { releaseSessionKey(sk); currentSessions.decrementAndGet(); } catch (Exception ignored) {}
                } else {
                    returnPooledSession(sk);
                }
            } else {
                // pooling disabled
                if (autoReleaseSession) {
                    try { releaseSessionKey(sk); } catch (Exception ignored) {}
                }
            }
            requestSemaphore.release();
        }
    }

    public JsonNode getSurveyProperties(int surveyId, List<String> properties) throws IOException, InterruptedException {
        requestSemaphore.acquireUninterruptibly();
        String sk = null;
        try {
            sk = acquirePooledSession(30, TimeUnit.SECONDS);
            List<Object> params = new ArrayList<>();
            params.add(sk);
            params.add(surveyId);
            params.add(properties);
            return callRpc("get_survey_properties", params);
        } finally {
            if (sessionPoolSize > 0) {
                if (autoReleaseSession) {
                    try { releaseSessionKey(sk); currentSessions.decrementAndGet(); } catch (Exception ignored) {}
                } else {
                    returnPooledSession(sk);
                }
            } else {
                if (autoReleaseSession) {
                    try { releaseSessionKey(sk); } catch (Exception ignored) {}
                }
            }
            requestSemaphore.release();
        }
    }

    public JsonNode setSurveyProperties(int surveyId, JsonNode properties) throws IOException, InterruptedException {
        requestSemaphore.acquireUninterruptibly();
        String sk = null;
        try {
            sk = acquirePooledSession(30, TimeUnit.SECONDS);
            List<Object> params = new ArrayList<>();
            params.add(sk);
            params.add(surveyId);
            params.add(properties);
            return callRpc("set_survey_properties", params);
        } finally {
            if (sessionPoolSize > 0) {
                if (autoReleaseSession) {
                    try { releaseSessionKey(sk); currentSessions.decrementAndGet(); } catch (Exception ignored) {}
                } else {
                    returnPooledSession(sk);
                }
            } else {
                if (autoReleaseSession) {
                    try { releaseSessionKey(sk); } catch (Exception ignored) {}
                }
            }
            requestSemaphore.release();
        }
    }

    // Close method to release all pooled session keys
    public void close() {
        if (sessionPoolSize <= 0 || sessionPool == null) return;
        List<String> drained = new ArrayList<>();
        sessionPool.drainTo(drained);
        for (String sk : drained) {
            try { releaseSessionKey(sk); } catch (Exception ignored) {}
            currentSessions.decrementAndGet();
        }
    }
}
