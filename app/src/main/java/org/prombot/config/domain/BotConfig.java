package org.prombot.config.domain;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotConfig {
  private String prometheusUrl;

  private List<NamedQuery> metrics;
  private List<ChannelTracking> trackChannels;
  private List<LogTracking> logTracking;
}
