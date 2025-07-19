package org.prombot.modules;

import com.google.inject.AbstractModule;
import org.prombot.config.YamlConfigService;

public class ConfigModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(YamlConfigService.class).asEagerSingleton();
    }
}
