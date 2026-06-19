package org.example.limesurveyclient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LimeSurveyClientIT {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Test
    @Timeout(900)
    public void testLoginLogoutWithDockerCompose() throws Exception {
        runDockerCompose("up", "-d");

        try {
            String baseUrl = "http://localhost:8080";
            String remoteUrl = baseUrl + "/index.php/admin/remotecontrol";

            assertTrue(waitForHttpStatus(baseUrl, Duration.ofMinutes(5)),
                    "LimeSurvey did not become ready on " + baseUrl);
            assertTrue(waitForHttpStatus(remoteUrl, Duration.ofMinutes(5)),
                    "Remote control endpoint did not become ready on " + remoteUrl);
        } finally {
            runDockerCompose("down", "-v");
        }
    }

    private static boolean waitForHttpStatus(String url, Duration timeout) throws Exception {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();
                HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    return true;
                }
            } catch (IOException ignored) {
                // service may still be starting
            }
            TimeUnit.SECONDS.sleep(5);
        }
        return false;
    }

    private static void runDockerCompose(String... args) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("docker", "compose", "-f", "docker-compose.yml");
        pb.command().addAll(Arrays.asList(args));
        pb.redirectErrorStream(true);
        Process process = pb.start();
        String output = readAll(process.getInputStream());
        int exitCode = process.waitFor();
        assertEquals(0, exitCode, "docker compose failed: " + output);
    }

    private static String readAll(InputStream inputStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append(System.lineSeparator());
            }
        }
        return builder.toString();
    }
}
