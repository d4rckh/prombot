package org.prombot.modules;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

import org.prombot.commands.FetchLastDoubleCommand;
import org.prombot.commands.ICommand;
import org.prombot.commands.PingCommand;
import org.prombot.commands.ShowConfigCommand;
import org.prombot.events.ReadyEventHandler;
import org.prombot.events.SlashCommandInteractionHandler;

public class BotModule extends AbstractModule {
  @Override
  protected void configure() {
    Multibinder<ICommand> commandBinder = Multibinder.newSetBinder(binder(), ICommand.class);

    commandBinder.addBinding().to(PingCommand.class);
    commandBinder.addBinding().to(FetchLastDoubleCommand.class);
    commandBinder.addBinding().to(ShowConfigCommand.class);
    
    bind(SlashCommandInteractionHandler.class).asEagerSingleton();
    bind(ReadyEventHandler.class).asEagerSingleton();
  }
}
