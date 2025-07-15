package org.prombot.modules;

import org.prombot.config.YamlConfigService;

import com.google.inject.AbstractModule;

public class ConfigModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(YamlConfigService.class).asEagerSingleton();
  }
  
}
