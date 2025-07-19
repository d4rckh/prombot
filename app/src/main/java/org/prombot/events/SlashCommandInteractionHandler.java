package org.prombot.events;

import com.google.inject.Inject;
import java.util.Set;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.prombot.commands.Command;

public class SlashCommandInteractionHandler extends ListenerAdapter {
    @Inject
    private Set<Command> commands;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        commands.stream()
                .filter(a -> a.getCommandData().getName().equals(event.getName()))
                .findFirst()
                .ifPresent((c) -> {
                    c.handle(event);
                });
    }
}
