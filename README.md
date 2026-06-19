# LimeSurvey RemoteControl Java Client

Proyecto: Cliente Java para la API RemoteControl de LimeSurvey.

## Requisitos

- Java 21
- Maven

## Build

```bash
mvn -DskipTests package
```

## Run example JAR

The shade plugin sets the main class to `org.example.limesurveyclient.examples.ExampleUsage`

```bash
java -jar target/limesurvey-clientV2-0.1.0-shaded.jar
```

## Usage

### Basic (autoReleaseSession = true)

Methods will login and automatically release the session after each RPC.

```java
LimeSurveyClient client = new LimeSurveyClient(url, user, pass);
client.copySurvey(1, "Copy of survey");
```

### Web-backend / reuse session (autoReleaseSession = false)

Login once, reuse session across requests, logout when done.

```java
LimeSurveyClient shared = new LimeSurveyClient(url, user, pass, false);
try {
    shared.login();
    shared.getSurveyProperties(1, List.of("title"));
} finally {
    shared.logout();
}
```

### autoReleaseSession flag

- **Default: true** - Each API call will automatically call logout() after completion when pooling is disabled.
- **Set to false** - Keep session keys across multiple calls (recommended for web backends to avoid repeated logins).

### Connection pooling and concurrency

The client supports an optional session pool and concurrency limit. Construct with:

```java
LimeSurveyClient client = new LimeSurveyClient(
    url, user, pass,           // credentials
    autoReleaseSession,         // false for pooling
    sessionPoolSize,            // number of pooled sessions (0 disables)
    maxConcurrentRequests       // concurrency limit
);
```

**Example for backend reuse:**
```java
// Keeps up to 5 pooled sessions and allows 10 concurrent requests
new LimeSurveyClient(url, user, pass, false, 5, 10)
```

- Call `client.close()` to release pooled sessions (they will call `release_session_key` against the server).
- When pooling is enabled and `autoReleaseSession=false`, sessions are returned to the pool for reuse.
- If `autoReleaseSession=true` and pooling enabled, sessions are invalidated after each use.

### Notes for web backends

- Create one shared `LimeSurveyClient` per host+credentials with pooling enabled and `autoReleaseSession=false` for best throughput.
- The client is thread-safe; use shared instances across threads.

## Implemented RPCs

- `copy_survey`
- `get_survey_properties`
- `set_survey_properties`

## Testing

### Unit tests

```bash
mvn -DskipTests=false -Dtest=*Test test
```

### Integration tests

```bash
mvn verify
```

### Pruebas de integración

Las pruebas de integración se ejecutan con Testcontainers mediante Maven Failsafe.

## CI / Recomendaciones

- Las pruebas de integración usan Testcontainers y están gestionadas por Failsafe.
- Ajustar timeouts en pom.xml para entornos CI lentos.
- Considerar publicar artefacto en un repositorio Maven para reutilización.
