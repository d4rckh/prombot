package org.prombot.logtracking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.prombot.config.domain.LogTracking;

@Slf4j
public class LogTrackingStreamClient extends WebSocketClient {
    private static final ObjectMapper mapper = new ObjectMapper();

    private final long startupNano;
    private final Consumer<String> logHandler;
    private final Runnable onClose;
    private Instant opennedAt;
    private final Duration maxAgeClient;

    public LogTrackingStreamClient(LogTracking logTracking, Consumer<String> logHandler, Runnable onClose) {
        super(buildWebSocketUrl(logTracking));
        this.startupNano = Instant.now().toEpochMilli() * 1_000_000L;
        this.logHandler = logHandler;
        this.onClose = onClose;
        this.maxAgeClient = logTracking.getServerMaxDuration();
    }

    private static URI buildWebSocketUrl(LogTracking logTracking) {
        String query = URLEncoder.encode(logTracking.getQuery(), StandardCharsets.UTF_8);
        try {
            return new URI("ws://" + logTracking.getLokiInstance() + "/loki/api/v1/tail?query=" + query);
        } catch (Exception e) {
            log.error("Couldn't initialize URL", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log.info("Connected to Loki WebSocket @ {}", this.getURI());
        this.opennedAt = Instant.now();
    }

    @Override
    public void onMessage(String message) {
        try {
            JsonNode root = mapper.readTree(message);
            JsonNode streams = root.get("streams");

            if (streams != null && streams.isArray()) {
                for (JsonNode stream : streams) {
                    JsonNode values = stream.get("values");
                    if (values != null && values.isArray()) {
                        for (JsonNode value : values) {
                            long logTimeNano = Long.parseLong(value.get(0).asText());
                            if (logTimeNano < startupNano) continue;

                            String formattedLine = value.get(1).asText();
                            logHandler.accept(formattedLine);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse Loki message", e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.warn("Loki WebSocket closed: {}", reason);
        this.onClose.run();
    }

    @Override
    public void onError(Exception ex) {
        log.error("Loki WebSocket error:", ex);
    }

    public boolean shouldCloseSoon() {
        return Duration.between(opennedAt, Instant.now()).toMillis() > (this.maxAgeClient.toMillis() - 30_000);
    }
}
