package org.prombot;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class App extends ListenerAdapter {
    public static void main(String[] args) throws LoginException {
        String token = System.getenv("DISCORD_TOKEN");
        var jda = JDABuilder.createDefault(token)
                .addEventListeners(new App())
                .setActivity(Activity.playing("with metrics"))
                .build();

        jda.updateCommands().addCommands(
                Commands.slash("ping", "Replies with pong!")
        ).queue();
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("ping")) {
            event.reply("Pong! 🏓").queue();
        }
    }
}
