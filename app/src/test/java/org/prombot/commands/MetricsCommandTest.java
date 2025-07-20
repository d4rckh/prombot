package org.prombot.commands;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
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
import org.prombot.config.ConfigService;
import org.prombot.config.domain.BotConfig;
import org.prombot.config.domain.NamedQuery;
import org.prombot.prom.PromFetcher;

class MetricsCommandTest {
    @Mock
    private PromFetcher promFetcher;

    @Mock
    private ConfigService yamlConfigService;

    @Mock
    private SlashCommandInteractionEvent slashCommandInteractionEvent;

    @Mock
    private InteractionHook interactionHook;

    @InjectMocks
    private MetricsCommand command;

    @Mock
    private OptionMapping optionMapping;

    @Captor
    private ArgumentCaptor<String> replyTextArgumentCaptor;

    @Mock
    private ReplyCallbackAction replyCallbackAction;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testHandleWithValidConfig_shouldReplyWithFetchedDoubles() {
        NamedQuery cpuUsageQuery = new NamedQuery("cpu_usage_query", "CPU Usage", "percentage");

        BotConfig botConfig =
                BotConfig.builder().metrics(List.of(cpuUsageQuery)).build();

        double cpuUsageValue = 90.00d;

        when(this.yamlConfigService.getBotConfig()).thenReturn(botConfig);
        when(this.promFetcher.fetchLastValue(cpuUsageQuery.getQuery())).thenReturn(cpuUsageValue);
        when(this.slashCommandInteractionEvent.reply(anyString())).thenReturn(this.replyCallbackAction);

        command.handle(slashCommandInteractionEvent);

        verify(this.promFetcher).fetchLastValue(cpuUsageQuery.getQuery());
        verify(this.slashCommandInteractionEvent).reply(replyTextArgumentCaptor.capture());
        verify(this.replyCallbackAction).queue();

        assertTrue(this.replyTextArgumentCaptor.getValue().contains(cpuUsageQuery.getName()));
        assertTrue(this.replyTextArgumentCaptor.getValue().contains(String.valueOf(cpuUsageValue)));
    }
}
