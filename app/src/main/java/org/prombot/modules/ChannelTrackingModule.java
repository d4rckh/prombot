package org.prombot.modules;

import com.google.inject.AbstractModule;
import org.prombot.channeltracking.ChannelTrackingService;

public class ChannelTrackingModule extends AbstractModule {
  @Override
  public void configure() {
    bind(ChannelTrackingService.class).asEagerSingleton();
  }
}
