LimeSurvey RemoteControl Java Client

Proyecto: Cliente Java para la API RemoteControl de LimeSurvey.

Requisitos:
- Java 21, Maven

Build
- mvn -DskipTests package

Run example JAR
- The shade plugin sets the main class to org.example.limesurveyclient.examples.ExampleUsage
- Run: java -jar target/limesurvey-clientV2-0.1.0-shaded.jar

Usage (example snippets)
- Basic (autoReleaseSession = true): methods will login and automatically release the session after each RPC.

    LimeSurveyClient client = new LimeSurveyClient(url, user, pass);
    client.copySurvey(1, "Copy of survey");

- Web-backend / reuse session (autoReleaseSession = false): login once, reuse session across requests, logout when done.

    LimeSurveyClient shared = new LimeSurveyClient(url, user, pass, false);
    try {n        shared.login();
        shared.getSurveyProperties(1, List.of("title"));
    } finally {
        shared.logout();
    }

autoReleaseSession flag
- Default: true. Each API call will automatically call logout() after completion when pooling is disabled.
- Set to false to keep session keys across multiple calls (recommended for web backends to avoid repeated logins).

Connection pooling and concurrency
- The client supports an optional session pool and concurrency limit. Construct with:

  LimeSurveyClient client = new LimeSurveyClient(url, user, pass, autoReleaseSession, sessionPoolSize, maxConcurrentRequests);

  * sessionPoolSize = number of pooled session keys (0 disables pooling).
  * maxConcurrentRequests = maximum concurrent RPC calls allowed (useful to protect LimeSurvey or network).

- Example for backend reuse: new LimeSurveyClient(url, user, pass, false, 5, 10) — keeps up to 5 pooled sessions and allows 10 concurrent requests.
- Call client.close() to release pooled sessions (they will call release_session_key against the server).
- When pooling is enabled and autoReleaseSession=false, sessions are returned to the pool for reuse. If autoReleaseSession=true and pooling enabled, sessions are invalidated after each use.

Notes for web backends
- Create one shared LimeSurveyClient per host+credentials with pooling enabled and autoReleaseSession=false for best throughput.
- The client is thread-safe; use shared instances across threads.

Implemented RPCs
- copy_survey, get_survey_properties, set_survey_properties

Testing
- Unit tests: mvn -DskipTests=false -Dtest=*Test test
- Integration tests (Testcontainers, IT): mvn verify (Failsafe ejecuta los *IT.java)
  * Requiere Docker. Si Docker no está disponible los IT fallarán.

CI / Recomendaciones
- Las pruebas de integración usan Testcontainers y están gestionadas por Failsafe.
- Ajustar timeouts en pom.xml para entornos CI lentos.
- Considerar publicar artefacto en un repositorio Maven para reutilización.


