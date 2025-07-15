package org.prombot.config.domain;

import java.util.List;
import lombok.Data;

@Data
public class BotConfig {
  private String prometheusUrl;

  private List<NamedQuery> metrics;
  private List<ChannelTracking> trackChannels;
}
