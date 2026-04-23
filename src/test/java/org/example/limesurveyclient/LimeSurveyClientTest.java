package org.example.limesurveyclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@org.junit.jupiter.api.Disabled("Moved to IT")
public class LimeSurveyClientTest {
    private HttpServer server;
    private int port;
    private final ObjectMapper mapper = new ObjectMapper();
    private final AtomicInteger getSessionCalls = new AtomicInteger();
    private final AtomicInteger releaseSessionCalls = new AtomicInteger();

    @BeforeEach
    public void startServer() throws Exception {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();
        server.createContext("/", new TestHandler());
        server.start();
    }

    @AfterEach
    public void stopServer() {
        if (server != null) server.stop(0);
        getSessionCalls.set(0);
        releaseSessionCalls.set(0);
    }

    private class TestHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            byte[] reqBytes = readAll(exchange.getRequestBody());
            String reqBody = new String(reqBytes, StandardCharsets.UTF_8);
            JsonNode req = mapper.readTree(reqBody);
            String method = req.path("method").asText();
            JsonNode id = req.path("id");

            ObjectNode resp = mapper.createObjectNode();
            resp.put("jsonrpc", "2.0");
            resp.set("id", id);

            switch (method) {
                case "get_session_key":
                    getSessionCalls.incrementAndGet();
                    resp.set("result", mapper.convertValue("TEST-SK", JsonNode.class));
                    break;
                case "release_session_key":
                    releaseSessionCalls.incrementAndGet();
                    resp.set("result", mapper.nullNode());
                    break;
                case "copy_survey":
                    // return a numeric result
                    resp.set("result", mapper.convertValue(42, JsonNode.class));
                    break;
                case "get_survey_properties":
                    // return an object with requested properties
                    ObjectNode props = mapper.createObjectNode();
                    props.put("surveyls_title", "My Survey Title");
                    props.put("sid", 5);
                    resp.set("result", props);
                    break;
                case "set_survey_properties":
                    resp.set("result", mapper.convertValue(true, JsonNode.class));
                    break;
                default:
                    ObjectNode err = mapper.createObjectNode();
                    err.put("code", -32601);
                    err.put("message", "Method not found");
                    resp.set("error", err);
                    break;
            }

            byte[] respBytes = mapper.writeValueAsBytes(resp);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, respBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(respBytes);
            }
        }

        private byte[] readAll(InputStream in) throws IOException {
            return in.readAllBytes();
        }
    }

    @Test
    public void testLoginAndLogout() throws Exception {
        String baseUrl = "http://localhost:" + server.getAddress().getPort() + "/";
        LimeSurveyClient client = new LimeSurveyClient(baseUrl, "u", "p", false);
        // sessionKey should be null initially
        java.lang.reflect.Field f = LimeSurveyClient.class.getDeclaredField("sessionKey");
        f.setAccessible(true);
        assertNull(f.get(client));

        client.login();
        assertEquals(1, getSessionCalls.get(), "get_session_key should have been called once");
        assertEquals("TEST-SK", f.get(client));

        client.logout();
        assertEquals(1, releaseSessionCalls.get(), "release_session_key should have been called once");
        assertNull(f.get(client));
    }

    @Test
    public void testCopySurvey_autoRelease() throws Exception {
        String baseUrl = "http://localhost:" + server.getAddress().getPort() + "/";
        LimeSurveyClient client = new LimeSurveyClient(baseUrl, "u", "p"); // autoReleaseSession = true
        JsonNode res = client.copySurvey(1, "newName");
        assertNotNull(res);
        assertTrue(res.isNumber());
        assertEquals(42, res.asInt());
        // since autoReleaseSession is true, release_session_key should have been called
        assertEquals(1, releaseSessionCalls.get());
    }

    @Test
    public void testGetSurveyProperties() throws Exception {
        String baseUrl = "http://localhost:" + server.getAddress().getPort() + "/";
        LimeSurveyClient client = new LimeSurveyClient(baseUrl, "u", "p");
        List<String> props = Arrays.asList("surveyls_title");
        JsonNode res = client.getSurveyProperties(5, props);
        assertNotNull(res);
        assertTrue(res.has("surveyls_title"));
        assertEquals("My Survey Title", res.get("surveyls_title").asText());
    }

    @Test
    public void testSetSurveyProperties() throws Exception {
        String baseUrl = "http://localhost:" + server.getAddress().getPort() + "/";
        LimeSurveyClient client = new LimeSurveyClient(baseUrl, "u", "p");
        ObjectNode props = mapper.createObjectNode();
        props.put("active", "Y");
        JsonNode res = client.setSurveyProperties(7, props);
        assertNotNull(res);
        assertTrue(res.isBoolean());
        assertTrue(res.asBoolean());
    }
}
