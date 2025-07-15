package org.prombot.channeltracking;

import com.google.inject.Inject;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.prombot.config.YamlConfigService;
import org.prombot.config.domain.BotConfig;
import org.prombot.config.domain.ChannelTracking;
import org.prombot.config.domain.NamedQuery;
import org.prombot.prom.PromFetcher;
import org.prombot.utils.FormatUtil;

@Slf4j
public class ChannelTrackingService {
  @Inject YamlConfigService yamlConfigService;

  @Inject PromFetcher promFetcher;

  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

  public void startTracking(JDA jda) {
    BotConfig botConfig = yamlConfigService.getBotConfig();

    List<ChannelTracking> trackChannels = yamlConfigService.getBotConfig().getTrackChannels();

    executor.scheduleAtFixedRate(
        () -> {
          try {
            for (ChannelTracking channelTracking : trackChannels) {
              String channelId = channelTracking.getChannelId();
              String nameTemplate = channelTracking.getName();

              StringBuilder updatedNameBuilder = new StringBuilder(nameTemplate);

              for (NamedQuery metric : botConfig.getMetrics()) {
                String placeholder = "{" + metric.getName() + "}";

                int index = updatedNameBuilder.indexOf(placeholder);

                if (index != -1) {
                  Double value = promFetcher.fetchLastValue(metric.getQuery());
                  String valueStr =
                      value == null
                          ? "N/A"
                          : FormatUtil.formatValue(
                              promFetcher.fetchLastValue(metric.getQuery()), metric.getFormat());

                  updatedNameBuilder.replace(index, index + placeholder.length(), valueStr);
                }
              }

              String updatedName = updatedNameBuilder.toString();

              GuildChannel channel = jda.getGuildChannelById(channelId);
              if (channel != null) {
                String currentName = channel.getName();

                if (!currentName.equals(updatedName))
                  channel.getManager().setName(updatedName).queue();
              } else log.warn("Channel ID {} not found in guild", channelId);
            }
          } catch (Exception e) {
            log.error("Error in channel tracking task", e);
          }
        },
        0,
        15,
        TimeUnit.MINUTES);
  }

  public void stopTracking() {
    executor.shutdown();
  }
}
