package org.prombot.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prombot.config.YamlConfigService;
import org.prombot.config.domain.BotConfig;
import org.prombot.config.domain.ChannelTracking;
import org.prombot.config.domain.NamedQuery;

class ShowConfigCommandTest {
  @Mock private YamlConfigService yamlConfigService;

  @Mock private JDA jda;

  @Mock private SlashCommandInteractionEvent slashCommandInteractionEvent;

  @Mock private InteractionHook interactionHook;

  @InjectMocks private ShowConfigCommand command;

  @Mock private OptionMapping optionMapping;

  @Captor private ArgumentCaptor<String> replyTextArgumentCaptor;

  @Mock private ReplyCallbackAction replyCallbackAction;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testHandleWithValidConfig_shouldReplyWithCorrectValues() {
    NamedQuery cpuUsageQuery = new NamedQuery("cpu_usage_query", "CPU Usage", "percentage");
    ChannelTracking channelTracking = new ChannelTracking("123456", "Test name 123");

    GuildChannel guildChannelMock = mock(GuildChannel.class);

    BotConfig botConfig =
        BotConfig.builder()
            .metrics(List.of(cpuUsageQuery))
            .trackChannels(List.of(channelTracking))
            .prometheusUrl("http://test_url")
            .build();

    when(this.yamlConfigService.getBotConfig()).thenReturn(botConfig);
    when(this.slashCommandInteractionEvent.reply(anyString())).thenReturn(this.replyCallbackAction);
    when(this.slashCommandInteractionEvent.getJDA()).thenReturn(this.jda);
    when(this.jda.getGuildChannelById(channelTracking.getChannelId())).thenReturn(guildChannelMock);

    command.handle(slashCommandInteractionEvent);

    verify(this.slashCommandInteractionEvent).reply(replyTextArgumentCaptor.capture());
    verify(this.replyCallbackAction).queue();

    assertTrue(
        this.replyTextArgumentCaptor.getValue().contains(botConfig.getPrometheusUrl()),
        "reply does not contain prometheus url");
    assertTrue(
        this.replyTextArgumentCaptor.getValue().contains(cpuUsageQuery.getName()),
        "reply does not contain query name");
    assertTrue(
        this.replyTextArgumentCaptor.getValue().contains(cpuUsageQuery.getQuery()),
        "reply does not contain query");
    assertTrue(
        this.replyTextArgumentCaptor.getValue().contains(channelTracking.getChannelId()),
        "reply does not contain channel id");
    assertTrue(
        this.replyTextArgumentCaptor.getValue().contains(channelTracking.getName()),
        "reply does not contain channel tracking name");
  }
}
