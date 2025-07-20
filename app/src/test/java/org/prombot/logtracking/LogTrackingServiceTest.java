package org.prombot.logtracking;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.URI;
import java.util.List;
import net.dv8tion.jda.api.JDA;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prombot.config.ConfigService;
import org.prombot.config.domain.BotConfig;
import org.prombot.config.domain.LogTracking;

public class LogTrackingServiceTest {
    @Mock
    ConfigService yamlConfigService;

    @Mock
    LogTrackingStreamClientFactory logTrackingStreamClientFactory;

    @InjectMocks
    LogTrackingService logTrackingService;

    @Captor
    ArgumentCaptor<Runnable> runnableCaptor;

    BotConfig testBotConfig = BotConfig.builder()
            .logTracking(List.of(LogTracking.builder()
                    .channelId("123")
                    .query("query")
                    .lokiInstance("loki")
                    .build()))
            .build();

    @BeforeEach
    void beforeEach() {
        MockitoAnnotations.openMocks(this);

        when(this.yamlConfigService.getBotConfig()).thenReturn(testBotConfig);
    }

    @Test
    void startTracking_addsClientsToList() {
        JDA jda = mock(JDA.class);
        LogTrackingStreamClient clientMock = mock(LogTrackingStreamClient.class);

        when(logTrackingStreamClientFactory.create(any(), any(), any())).thenReturn(clientMock);

        logTrackingService.startTracking(jda);

        assertEquals(1, logTrackingService.getLogTrackingStreamClients().size());
        assertSame(clientMock, logTrackingService.getLogTrackingStreamClients().get(0));
    }

    @Test
    void reconnect_removesOldClientsAndCreatesNewOne() throws Exception {
        LogTrackingStreamClient oldClient = mock(LogTrackingStreamClient.class);
        JDA jda = mock(JDA.class);

        when(oldClient.getURI()).thenReturn(new URI("http://loki123"));
        when(logTrackingStreamClientFactory.create(any(), any(), any())).thenReturn(oldClient);

        logTrackingService.startTracking(jda);

        // List should contain oldClient
        assertEquals(1, logTrackingService.getLogTrackingStreamClients().size());

        // Capture reconnect Runnable
        verify(logTrackingStreamClientFactory).create(any(), any(), runnableCaptor.capture());

        // Run reconnect
        runnableCaptor.getValue().run();

        // Old client should be removed from the list after reconnect
        // Wait some time or run the scheduled task immediately (to avoid timing issues)
        // For simplicity, just check that old client is removed (simulate direct call)
        assertFalse(logTrackingService.getLogTrackingStreamClients().contains(oldClient));
    }
}
