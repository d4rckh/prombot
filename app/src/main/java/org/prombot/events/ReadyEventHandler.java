package org.prombot.events;

import com.google.inject.Inject;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.prombot.commands.ICommand;

@Slf4j
public class ReadyEventHandler extends ListenerAdapter {
  @Inject private Set<ICommand> commands;

  @Override
  public void onReady(ReadyEvent event) {
    JDA jda = event.getJDA();
    List<CommandData> commandDatas = this.commands.stream().map(c -> c.getCommandData()).toList();

    log.info("Loading {} commands", commandDatas.size());

    Guild guild = jda.getGuildById("1394383899999604756");

    log.info("Currently in {} guilds", jda.getGuilds().size());

    commandDatas.stream().forEach(c -> log.info("Command: {}", c.getName()));

    guild
        .updateCommands()
        .addCommands(commandDatas)
        .queue(
            success -> log.info("Successfully registered guild commands!"),
            error -> log.error("Failed to register guild commands", error));
    jda.updateCommands()
        .addCommands(commandDatas)
        .queue(
            success -> log.info("Successfully registered globals commands!"),
            error -> log.error("Failed to register globals commands", error));
  }
}
