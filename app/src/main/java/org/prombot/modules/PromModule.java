package org.prombot.modules;

import org.prombot.prom.PromFetcher;

import com.google.inject.AbstractModule;

public class PromModule extends AbstractModule{
  
  @Override
  protected void configure() {
    bind(PromFetcher.class).asEagerSingleton();
  }

}
