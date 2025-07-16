package org.prombot.commands;

import com.google.inject.Inject;
import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.prombot.prom.PromFetcher;

public class FetchLastDoubleCommand implements Command {
  @Getter
  private final CommandData commandData =
      Commands.slash("fetchlastdouble", "Fetches the last double")
          .addOption(OptionType.STRING, "query", "The query");

  @Inject private PromFetcher promFetcher;

  @Override
  public void handle(SlashCommandInteractionEvent event) {
    OptionMapping optionMapping = event.getOption("query");

    if (optionMapping == null) throw new RuntimeException("Option is null");

    String query = optionMapping.getAsString();

    event.reply(promFetcher.fetchLastValue(query).toString()).queue();
  }
}
