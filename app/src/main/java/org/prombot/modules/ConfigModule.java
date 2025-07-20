package org.prombot.modules;

import com.google.inject.AbstractModule;
import org.prombot.config.ConfigService;

public class ConfigModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ConfigService.class).asEagerSingleton();
    }
}
