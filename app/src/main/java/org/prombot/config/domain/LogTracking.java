package org.prombot.config.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogTracking {
  private String lokiInstance;
  private String query;
  private String channelId;
}
