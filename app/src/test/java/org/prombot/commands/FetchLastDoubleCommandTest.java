package org.prombot.commands;

import static org.mockito.Mockito.*;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prombot.prom.PromFetcher;

class FetchLastDoubleCommandTest {
  @Mock private PromFetcher promFetcher;

  @Mock private SlashCommandInteractionEvent slashCommandInteractionEvent;

  @Mock private InteractionHook interactionHook;

  @InjectMocks private FetchLastDoubleCommand command;

  @Mock private OptionMapping optionMapping;

  @Mock private ReplyCallbackAction replyCallbackAction;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testHandleWithValidQuery_shouldReplyWithDouble() {
    String query = "node_cpu_seconds_total";
    double expectedValue = 42.0d;

    when(this.slashCommandInteractionEvent.getOption("query")).thenReturn(optionMapping);
    when(this.optionMapping.getAsString()).thenReturn(query);
    when(this.promFetcher.fetchLastValue(query)).thenReturn(expectedValue);
    when(this.slashCommandInteractionEvent.reply(anyString())).thenReturn(this.replyCallbackAction);

    command.handle(slashCommandInteractionEvent);

    verify(this.slashCommandInteractionEvent).getOption("query");
    verify(this.promFetcher).fetchLastValue(query);
    verify(this.slashCommandInteractionEvent).reply("42.0");
    verify(this.replyCallbackAction).queue();
  }
}
