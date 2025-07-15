package org.prombot.modules;

import com.google.inject.AbstractModule;
import org.prombot.prom.PromFetcher;

public class PromModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(PromFetcher.class).asEagerSingleton();
  }
}
