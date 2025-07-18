package org.prombot.logtracking;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import org.prombot.config.YamlConfigService;
import org.prombot.config.domain.BotConfig;
import org.prombot.config.domain.LogTracking;

@Slf4j
public class LogTrackingService {
  @Inject private YamlConfigService yamlConfigService;

  @Inject private LogTrackingStreamClientFactory logTrackingStreamClientFactory;

  @Getter private final List<LogTrackingStreamClient> logTrackingStreamClients = new ArrayList<>();

  public void startTracking(JDA jda) {
    BotConfig botConfig = yamlConfigService.getBotConfig();

    log.info("Will track {} streams", botConfig.getLogTracking().size());

    for (LogTracking logTracking : botConfig.getLogTracking()) {
      createAndConnectClient(jda, logTracking);
    }
  }

  private void createAndConnectClient(JDA jda, LogTracking logTracking) {
    LogTrackingStreamClient client =
        logTrackingStreamClientFactory.create(jda, logTracking, () -> reconnect(jda, logTracking));
    logTrackingStreamClients.add(client);
    client.connect();
  }

  private void reconnect(JDA jda, LogTracking logTracking) {
    log.info("Reconnecting to stream for channel {}", logTracking.getChannelId());

    logTrackingStreamClients.removeIf(
        client -> client.getURI().toString().contains(logTracking.getChannelId()));

    Executors.newSingleThreadScheduledExecutor()
        .schedule(() -> createAndConnectClient(jda, logTracking), 5, TimeUnit.SECONDS);
  }
}
