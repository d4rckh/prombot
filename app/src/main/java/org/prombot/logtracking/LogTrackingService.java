package org.prombot.logtracking;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import org.prombot.config.YamlConfigService;
import org.prombot.config.domain.BotConfig;
import org.prombot.config.domain.LogTracking;

@Slf4j
public class LogTrackingService {
  @Inject private YamlConfigService yamlConfigService;

  private List<LogTrackingStreamClient> logTrackingStreamClients = new ArrayList<>();

  public void startTracking(JDA jda) {
    BotConfig botConfig = yamlConfigService.getBotConfig();

    log.info("Will track {} streams", botConfig.getLogTracking().size());

    for (LogTracking logTracking : botConfig.getLogTracking()) {
      this.logTrackingStreamClients.add(new LogTrackingStreamClient(jda, logTracking));
    }

    for (LogTrackingStreamClient logTrackingStreamClient : this.logTrackingStreamClients) {
      logTrackingStreamClient.connect();
    }
  }
}
