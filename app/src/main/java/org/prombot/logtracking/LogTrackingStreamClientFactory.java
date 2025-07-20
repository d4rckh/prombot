package org.prombot.logtracking;

import lombok.NoArgsConstructor;
import net.dv8tion.jda.api.JDA;
import org.prombot.config.domain.LogTracking;

@NoArgsConstructor
public class LogTrackingStreamClientFactory {
    LogTrackingStreamClient create(JDA jda, LogTracking logTracking, Runnable reconnectCallback) {
        return new LogTrackingStreamClient(jda, logTracking, reconnectCallback);
    }
}
