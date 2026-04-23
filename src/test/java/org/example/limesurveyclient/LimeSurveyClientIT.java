package org.example.limesurveyclient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class LimeSurveyClientIT {

    @Test
    @Timeout(900) // allow up to 15 minutes for containers to start and init
    public void testLoginLogoutWithTestcontainers() {
        Network network = Network.newNetwork();

        long startupMinutes = Long.parseLong(System.getProperty("it.startup.timeout.minutes", "10"));
        long testTimeoutMinutes = Long.parseLong(System.getProperty("it.test.timeout.minutes", "15"));

        MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0-debian")
                .withDatabaseName("limesurvey")
                .withUsername("limesurvey")
                .withPassword("limesurvey")
                .withNetwork(network)
                .withNetworkAliases("db")
                .withStartupTimeout(Duration.ofMinutes(startupMinutes));
        mysql.start();

        GenericContainer<?> limesurvey = new GenericContainer<>("martialblog/limesurvey:latest")
                .withExposedPorts(8080)
                .withEnv("DB_TYPE", "mysql")
                .withEnv("DB_HOST", "db")
                .withEnv("DB_PORT", "3306")
                .withEnv("DB_NAME", "limesurvey")
                .withEnv("DB_USERNAME", "limesurvey")
                .withEnv("DB_PASSWORD", "limesurvey")
                .withEnv("ADMIN_USER", "admin")
                .withEnv("ADMIN_PASSWORD", "password")
                .withEnv("REMOTECONTROL_ENABLE", "true")
                .withEnv("REMOTECONTROL_JSON_RPC", "true")
                .withNetwork(network)
                .waitingFor(Wait.forHttp("/admin/").forStatusCode(200).withStartupTimeout(Duration.ofMinutes(startupMinutes)));
        limesurvey.start();

        String host = limesurvey.getHost();
        Integer port = limesurvey.getMappedPort(8080);
        String remoteUrl = String.format("http://%s:%d/index.php/admin/remotecontrol", host, port);

        LimeSurveyClient client = new LimeSurveyClient(remoteUrl, "admin", "password", true);

        assertDoesNotThrow(() -> {
            client.login();
            client.logout();
        });

        limesurvey.stop();
        mysql.stop();
        network.close();
    }
}
