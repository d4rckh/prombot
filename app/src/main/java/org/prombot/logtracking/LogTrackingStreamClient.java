package org.prombot.logtracking;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.prombot.config.domain.LogTracking;

@Slf4j
public class LogTrackingStreamClient extends WebSocketClient {
  private static final ObjectMapper mapper = new ObjectMapper();

  private final long startupNano;
  private final TextChannel textChannel;

  // todo: move this inside org.prombot.logtracking.LogTrackingService
  private final List<String> logBuffer = new CopyOnWriteArrayList<>();

  private final Runnable onClose;

  private final Duration maxAgeClient;

  private Instant opennedAt;

  private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

  public LogTrackingStreamClient(JDA jda, LogTracking logTracking, Runnable onClose) {
    super(buildWebSocketUrl(logTracking));
    this.textChannel = jda.getTextChannelById(logTracking.getChannelId());
    this.startupNano = Instant.now().toEpochMilli() * 1_000_000L;
    this.onClose = onClose;
    this.maxAgeClient = logTracking.getServerMaxDuration();

    scheduler.scheduleAtFixedRate(this::flushLogsToDiscord, 5, 10, TimeUnit.SECONDS);
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
              logBuffer.add(formattedLine);
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("Failed to parse Loki message", e);
    }
  }

  private void flushLogsToDiscord() {
    if (textChannel == null || logBuffer.isEmpty()) return;

    List<String> toSend = new ArrayList<>(logBuffer);
    logBuffer.clear();

    // Estimate max messages we allow per flush
    final int maxMessages = 10;
    final int maxLinesPerMessage = 40; // average, varies with line length
    final int maxLinesTotal = maxMessages * maxLinesPerMessage;

    if (toSend.size() > maxLinesTotal) {
      int linesToSkip = toSend.size() - maxLinesTotal + 1;
      int keepStart = maxLinesTotal / 2;
      int keepEnd = maxLinesTotal - keepStart - 1;

      List<String> cut = new ArrayList<>();
      cut.addAll(toSend.subList(0, keepStart));
      cut.add("...skipped " + linesToSkip + " lines...");
      cut.addAll(toSend.subList(toSend.size() - keepEnd, toSend.size()));
      toSend = cut;
    }

    StringBuilder chunk = new StringBuilder("```\n");
    for (String line : toSend) {
      if (chunk.length() + line.length() + 1 >= 1990) {
        chunk.append("```");
        textChannel.sendMessage(chunk.toString()).queue();
        chunk = new StringBuilder("```\n");
      }
      chunk.append(line).append("\n");
    }

    if (chunk.length() > 4) {
      chunk.append("```");
      textChannel.sendMessage(chunk.toString()).queue();
    }

    if (Duration.between(opennedAt, Instant.now()).toMillis()
        > (this.maxAgeClient.toMillis() - 15000)) {
      this.close(1012 /* Service REstart */, "Closing client before server max tail duration.");
    }
  }

  @Override
  public void onClose(int code, String reason, boolean remote) {
    log.warn("Loki WebSocket closed: {}", reason);
    scheduler.shutdownNow();
    this.onClose.run();
  }

  @Override
  public void onError(Exception ex) {
    log.error("Loki WebSocket error:", ex);
  }
}
