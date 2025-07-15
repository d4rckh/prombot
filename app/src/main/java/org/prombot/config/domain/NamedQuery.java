package org.prombot.config.domain;

import lombok.Data;

@Data
public class NamedQuery {
  private String query;
  private String name;
}
