package com.blemberg.providers.fred;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class FredClientTest {

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void convertsFredPercentageRatesToDecimals() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/fred/series/observations", exchange -> {
            byte[] body = """
                {"observations":[
                  {"date":"2026-06-01","value":"."},
                  {"date":"2026-05-31","value":"4.50"}
                ]}
                """.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();

        FredClient client = new FredClient(new FredProperties(baseUrl(), "key", Duration.ofSeconds(2)));

        FredRateObservation observation = client.fetchLatestTreasuryRate(FredTenor.ONE_YEAR);

        assertThat(observation.rateDecimal()).isEqualByComparingTo("0.0450");
        assertThat(observation.observationDate()).hasToString("2026-05-31");
    }

    private String baseUrl() {
        return "http://localhost:" + server.getAddress().getPort();
    }
}
