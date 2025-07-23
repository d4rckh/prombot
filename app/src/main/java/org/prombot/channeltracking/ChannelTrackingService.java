package org.prombot.channeltracking;

import com.google.inject.Inject;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import org.prombot.config.ConfigService;
import org.prombot.config.domain.BotConfig;
import org.prombot.config.domain.ChannelTracking;
import org.prombot.config.domain.NamedQuery;
import org.prombot.prom.PromFetcher;
import org.prombot.utils.FormatUtil;

@Slf4j
public class ChannelTrackingService {
    @Inject
    private ConfigService configService;

    @Inject
    private PromFetcher promFetcher;

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private static final int CHANNEL_TRACKING_UPDATE_RATE_MINUTES = 15;

    public void startTracking(JDA jda) {
        BotConfig config = configService.getBotConfig();
        List<ChannelTracking> trackChannels = config.getTrackChannels();

        executor.scheduleAtFixedRate(
                () -> {
                    try {
                        for (ChannelTracking tracking : trackChannels) {
                            updateChannel(jda, config, tracking);
                        }
                    } catch (Exception e) {
                        log.error("Error in channel tracking task", e);
                    }
                },
                0,
                CHANNEL_TRACKING_UPDATE_RATE_MINUTES,
                TimeUnit.MINUTES);
    }

    public void stopTracking() {
        executor.shutdown();
    }

    private void updateChannel(JDA jda, BotConfig config, ChannelTracking tracking) {
        String channelId = tracking.getChannelId();
        String template = tracking.getName();

        String newName = generateChannelName(template, config.getMetrics());

        GuildChannel channel = jda.getGuildChannelById(channelId);

        if (channel == null) {
            log.error("Channel with ID {} not found in guild", channelId);
            return;
        }

        String currentName = channel.getName();

        if (!currentName.equals(newName)) {
            channel.getManager().setName(newName).queue();
        }
    }

    private String generateChannelName(String template, List<NamedQuery> metrics) {
        String result = template;

        for (NamedQuery metric : metrics) {
            String placeholder = "{" + metric.getName() + "}";
            if (result.contains(placeholder)) {
                Double value = promFetcher.fetchLastValue(metric.getQuery());
                String formatted = value == null ? "N/A" : FormatUtil.formatValue(value, metric.getFormat());

                result = result.replace(placeholder, formatted);
            }
        }

        return result;
    }
}
