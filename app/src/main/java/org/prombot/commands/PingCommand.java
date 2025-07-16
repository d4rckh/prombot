package org.prombot.commands;

import lombok.Getter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class PingCommand implements Command {
  @Getter private final CommandData commandData = Commands.slash("ping", "Replies with pong!");

  @Override
  public void handle(SlashCommandInteractionEvent event) {
    event.reply("Hello").queue();
  }
}
