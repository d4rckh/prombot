package org.prombot.events;

import java.util.Set;

import org.prombot.commands.ICommand;

import com.google.inject.Inject;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SlashCommandInteractionHandler extends ListenerAdapter {
  @Inject
  private Set<ICommand> commands;
  
  @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
      commands
        .stream()
        .filter(a -> a.getCommandData().getName().equals(event.getName()))
        .findFirst()
        .ifPresent((c) -> {
          c.handle(event);
        });
    }
}
