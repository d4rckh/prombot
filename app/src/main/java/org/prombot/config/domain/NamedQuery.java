package org.prombot.config.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NamedQuery {
  private String query;
  private String name;
  private String format;
}
