package org.prombot.config.domain;

import java.util.List;
import lombok.Data;

@Data
public class BotConfig {
  private List<NamedQuery> metrics;
}
