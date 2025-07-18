package org.prombot.logtracking;

import net.dv8tion.jda.api.JDA;
import org.prombot.config.domain.LogTracking;

public class LogTrackingStreamClientFactory {
  LogTrackingStreamClient create(JDA jda, LogTracking logTracking, Runnable reconnectCallback) {
    return new LogTrackingStreamClient(jda, logTracking, reconnectCallback);
  }
  ;
}
