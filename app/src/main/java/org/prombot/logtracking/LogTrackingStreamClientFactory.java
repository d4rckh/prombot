package org.prombot.logtracking;

import java.util.function.Consumer;
import lombok.NoArgsConstructor;
import org.prombot.config.domain.LogTracking;

@NoArgsConstructor
public class LogTrackingStreamClientFactory {
    LogTrackingStreamClient create(LogTracking logTracking, Consumer<String> logHandler, Runnable onClose) {
        return new LogTrackingStreamClient(logTracking, logHandler, onClose);
    }
}
