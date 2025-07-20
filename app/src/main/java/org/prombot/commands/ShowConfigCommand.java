package org.prombot.commands;

import com.google.inject.Inject;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.prombot.config.ConfigService;
import org.prombot.config.domain.BotConfig;
import org.prombot.config.domain.ChannelTracking;
import org.prombot.config.domain.NamedQuery;

public class ShowConfigCommand implements Command {
    @Inject
    ConfigService yamlConfigService;

    @Getter
    private final CommandData commandData = Commands.slash("showconfig", "Shows current bot config");

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        JDA jda = event.getJDA();
        BotConfig config = yamlConfigService.getBotConfig();

        if (config == null || config.getMetrics() == null || config.getMetrics().isEmpty()) {
            event.reply("No metrics configured.").queue();
            return;
        }

        StringBuilder response = new StringBuilder("-- Config --\n");

        response.append("Prometheus URL: ");
        response.append(config.getPrometheusUrl());

        response.append("\n\nConfigured Metrics\n");
        for (NamedQuery nq : config.getMetrics()) {
            response.append("- **")
                    .append(nq.getName())
                    .append("**: ")
                    .append(nq.getQuery())
                    .append("\n");
        }

        response.append("\nChannel tracking\n");
        for (ChannelTracking nq : config.getTrackChannels()) {
            GuildChannel channel = jda.getGuildChannelById(nq.getChannelId());

            response.append("- **")
                    .append(nq.getChannelId())
                    .append(channel == null ? " (invalid channel)" : " (valid)")
                    .append("**: ")
                    .append(nq.getName())
                    .append("\n");
        }

        event.reply(response.toString()).queue();
    }
}
