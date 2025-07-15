package org.prombot.commands;

import com.google.inject.Inject;
import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.prombot.config.YamlConfigService;
import org.prombot.config.domain.BotConfig;
import org.prombot.config.domain.NamedQuery;

public class ShowConfigCommand implements ICommand {
  @Inject YamlConfigService yamlConfigService;

  @Getter
  private final CommandData commandData = Commands.slash("showconfig", "Shows current bot config");

  @Override
  public void handle(SlashCommandInteractionEvent event) {
    BotConfig config = yamlConfigService.getBotConfig();

    if (config == null || config.getMetrics() == null || config.getMetrics().isEmpty()) {
      event.reply("No metrics configured.").queue();
      return;
    }

    StringBuilder response = new StringBuilder("Configured Metrics:\n");
    for (NamedQuery nq : config.getMetrics()) {
      response
          .append("- **")
          .append(nq.getName())
          .append("**: ")
          .append(nq.getQuery())
          .append("\n");
    }

    event.reply(response.toString()).queue();
  }
}
