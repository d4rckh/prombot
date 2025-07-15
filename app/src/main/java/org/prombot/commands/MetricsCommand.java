package org.prombot.commands;

import com.google.inject.Inject;
import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.prombot.config.YamlConfigService;
import org.prombot.config.domain.BotConfig;
import org.prombot.config.domain.NamedQuery;
import org.prombot.prom.PromFetcher;
import org.prombot.utils.FormatUtil;

public class MetricsCommand implements ICommand {
  @Getter
  private final CommandData commandData =
      Commands.slash("metrics", "Shows all metrics and their current values.");

  @Inject YamlConfigService yamlConfigService;
  @Inject PromFetcher promFetcher;

  @Override
  public void handle(SlashCommandInteractionEvent event) {
    BotConfig config = yamlConfigService.getBotConfig();

    if (config == null || config.getMetrics() == null || config.getMetrics().isEmpty()) {
      event.reply("No metrics configured.").queue();
      return;
    }

    StringBuilder response = new StringBuilder("Metrics:\n");
    for (NamedQuery nq : config.getMetrics()) {
      response
          .append("- **")
          .append(nq.getName())
          .append("**: ")
          .append(FormatUtil.formatValue(promFetcher.fetchLastValue(nq.getQuery()), nq.getFormat()))
          .append("\n");
    }

    event.reply(response.toString()).queue();
  }
}
