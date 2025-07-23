package org.prombot.logtracking;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.prombot.config.ConfigService;
import org.prombot.config.domain.BotConfig;
import org.prombot.config.domain.LogTracking;

@Slf4j
public class LogTrackingService {
    @Inject
    private ConfigService yamlConfigService;

    @Inject
    private LogTrackingStreamClientFactory logTrackingStreamClientFactory;

    @Getter
    private final List<LogTrackingStreamClient> logTrackingStreamClients = new ArrayList<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final Map<LogTracking, List<String>> logBuffers = new ConcurrentHashMap<>();
    private JDA jda;

    private static final int MAX_LOGS_PER_MESSAGE = 50;
    private static final int MAX_MESSAGE_LENGTH = 1990;
    private static final int LOKI_CLIENT_RECONNECTION_DELAY_SECONDS = 5;
    private static final int LOG_TRACKING_FLUSH_RATE_SECONDS = 10;
    private static final int LOG_TRACKING_FLUSH_INITIAL_DELAY_SECONDS = 5;

    public void startTracking(JDA jda) {
        this.jda = jda;

        BotConfig botConfig = yamlConfigService.getBotConfig();
        log.info("Will track {} streams", botConfig.getLogTracking().size());

        for (LogTracking logTracking : botConfig.getLogTracking()) {
            logBuffers.put(logTracking, new CopyOnWriteArrayList<>());
            createAndConnectClient(jda, logTracking);
        }

        scheduler.scheduleAtFixedRate(
                this::flushAll,
                LOG_TRACKING_FLUSH_INITIAL_DELAY_SECONDS,
                LOG_TRACKING_FLUSH_RATE_SECONDS,
                TimeUnit.SECONDS);
    }

    private void createAndConnectClient(JDA jda, LogTracking logTracking) {
        Consumer<String> logHandler = logLine -> logBuffers.get(logTracking).add(logLine);

        Runnable onClose = () -> reconnect(jda, logTracking);

        LogTrackingStreamClient client = logTrackingStreamClientFactory.create(logTracking, logHandler, onClose);
        logTrackingStreamClients.add(client);
        client.connect();
    }

    private void reconnect(JDA jda, LogTracking logTracking) {
        log.info("Reconnecting to stream for channel {}", logTracking.getChannelId());

        logTrackingStreamClients.removeIf(client -> client.getURI().toString().contains(logTracking.getChannelId()));

        Executors.newSingleThreadScheduledExecutor()
                .schedule(
                        () -> createAndConnectClient(jda, logTracking),
                        LOKI_CLIENT_RECONNECTION_DELAY_SECONDS,
                        TimeUnit.SECONDS);
    }

    private void flushAll() {
        for (Map.Entry<LogTracking, List<String>> entry : logBuffers.entrySet()) {
            LogTracking config = entry.getKey();
            List<String> buffer = entry.getValue();

            if (buffer == null || buffer.isEmpty()) continue;

            TextChannel textChannel = jda.getTextChannelById(config.getChannelId());

            if (textChannel == null) {
                log.warn("Couldn't find text channel with ID {}", config.getChannelId());
                continue;
            }

            List<String> toSend = truncateLogs(buffer, MAX_LOGS_PER_MESSAGE);
            buffer.clear();

            sendToDiscord(toSend, textChannel, MAX_MESSAGE_LENGTH);
        }

        logTrackingStreamClients.removeIf(client -> {
            if (client.shouldCloseSoon()) {
                client.close(1000, "Restarting due to max tail duration");
                return true;
            }
            return false;
        });
    }

    private List<String> truncateLogs(List<String> original, int maxLines) {
        if (original.size() <= maxLines) return new ArrayList<>(original);

        int linesToSkip = original.size() - maxLines + 1;
        int keepStart = maxLines / 2;
        int keepEnd = maxLines - keepStart - 1;

        List<String> cut = new ArrayList<>(maxLines);
        cut.addAll(original.subList(0, keepStart));
        cut.add("...skipped " + linesToSkip + " lines...");
        cut.addAll(original.subList(original.size() - keepEnd, original.size()));
        return cut;
    }

    private void sendToDiscord(List<String> lines, TextChannel channel, int maxLength) {
        StringBuilder chunk = new StringBuilder("```\n");

        for (String line : lines) {
            if (chunk.length() + line.length() >= maxLength) {
                chunk.append("```");
                channel.sendMessage(chunk.toString()).queue();
                chunk = new StringBuilder("```\n");
            }
            chunk.append(line).append("\n");
        }

        if (chunk.length() > 4) {
            chunk.append("```");
            channel.sendMessage(chunk.toString()).queue();
        }
    }
}
