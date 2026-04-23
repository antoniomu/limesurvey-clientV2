package org.example.limesurveyclient.examples;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.limesurveyclient.LimeSurveyClient;

import java.util.List;

public class ExampleUsage {
    public static void main(String[] args) throws Exception {
        // Configure these placeholders for your LimeSurvey instance
        String url = "https://your-limesurvey-host/index.php/admin/remotecontrol";
        String user = "your_user";
        String pass = "your_password";
        int surveyId = 1; // replace with an existing survey id

        // Simple usage (default autoReleaseSession = true)
        LimeSurveyClient client = new LimeSurveyClient(url, user, pass);
        // copy survey - this will login, call API and (because of auto release) logout automatically
        JsonNode copyRes = client.copySurvey(surveyId, "Copy of survey");
        System.out.println("copy result: " + copyRes);

        // get properties
        JsonNode props = client.getSurveyProperties(surveyId, List.of("language", "active"));
        System.out.println("props: " + props);

        // set properties (example)
        ObjectMapper om = new ObjectMapper();
        JsonNode newProps = om.createObjectNode().put("active", "N");
        JsonNode setRes = client.setSurveyProperties(surveyId, newProps);
        System.out.println("set result: " + setRes);

        // Explicit logout is safe (no-op if already released)
        client.logout();

        // Web-backend style: reuse a session across multiple calls to avoid repeated login overhead.
        LimeSurveyClient shared = new LimeSurveyClient(url, user, pass, false, 5, 10); // autoReleaseSession = false, poolSize=5, maxConcurrent=10
        try {
            // When using pooling, prefer not to call login(); sessions are created on demand and reused.
            System.out.println("Shared props1: " + shared.getSurveyProperties(surveyId, List.of("title")));
            System.out.println("Shared props2: " + shared.getSurveyProperties(surveyId, List.of("active")));
        } finally {
            // close will release pooled sessions
            shared.close();
        }
    }
}
