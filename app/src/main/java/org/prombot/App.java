package org.prombot;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.login.LoginException;

import org.prombot.commands.ICommand;
import org.prombot.commands.PingCommand;
import org.prombot.events.ReadyEventHandler;
import org.prombot.events.SlashCommandInteractionHandler;
import org.prombot.modules.BotModule;
import org.prombot.modules.ConfigModule;
import org.prombot.modules.PromModule;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

@Slf4j
public class App {
    @Inject
    private SlashCommandInteractionHandler slashCommandInteractionHandler;

    @Inject
    private ReadyEventHandler readyEventHandler;

    public static void main(String[] args) throws LoginException {
        Injector injector = Guice.createInjector(new BotModule(), new PromModule(), new ConfigModule());
        injector.getInstance(App.class).start();
    }

    public void start() {
        String token = System.getenv("DISCORD_TOKEN");
        var jda = JDABuilder.createDefault(token)
                .addEventListeners(slashCommandInteractionHandler, readyEventHandler)
                .setActivity(Activity.playing("with metrics"))
                .build();
    }
}
