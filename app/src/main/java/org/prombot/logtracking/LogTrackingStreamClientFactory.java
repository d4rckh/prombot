package org.prombot.logtracking;

import org.prombot.config.domain.LogTracking;

import net.dv8tion.jda.api.JDA;

public class LogTrackingStreamClientFactory {
  LogTrackingStreamClient create(JDA jda, LogTracking logTracking, Runnable reconnectCallback) {
    return new LogTrackingStreamClient(jda, logTracking, reconnectCallback);
  };
}
