package org.prombot.config.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogTracking {
  private String lokiInstance;
  private String query;
  private String channelId;
  private String serverTailMaxDuration;

  public Duration getServerMaxDuration() {
    if (Objects.isNull(this.serverTailMaxDuration)) {
      return Duration.ofHours(1);
    }

    String timeStr = this.serverTailMaxDuration.toLowerCase();

    if (timeStr.endsWith("ms")) {
      return Duration.ofMillis(Long.parseLong(timeStr.replace("ms", "")));
    } else if (timeStr.endsWith("s")) {
      return Duration.ofSeconds(Long.parseLong(timeStr.replace("s", "")));
    } else if (timeStr.endsWith("m")) {
      return Duration.ofMinutes(Long.parseLong(timeStr.replace("m", "")));
    } else if (timeStr.endsWith("h")) {
      return Duration.ofHours(Long.parseLong(timeStr.replace("h", "")));
    } else if (timeStr.endsWith("d")) {
      return Duration.ofDays(Long.parseLong(timeStr.replace("d", "")));
    } else {
      throw new IllegalArgumentException("Unsupported time format: " + timeStr);
    }
  }
}
