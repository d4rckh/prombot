package org.prombot.modules;

import com.google.inject.AbstractModule;
import org.prombot.logtracking.LogTrackingService;

public class LogTrackingModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(LogTrackingService.class).asEagerSingleton();
  }
}
