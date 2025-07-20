package org.prombot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.prombot.config.domain.BotConfig;

@Slf4j
public class ConfigService {
    private BotConfig cachedBotConfig;

    public BotConfig getBotConfig() {
        if (cachedBotConfig == null) {
            cachedBotConfig = this.loadConfig();
        }

        return cachedBotConfig;
    }

    public BotConfig loadConfig() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        try {
            String cwd = System.getProperty("user.dir");
            log.info("Current working directory: {}", cwd);
            File configFile = new File(cwd, "config.yml");
            log.info("Reading config from {}", configFile.getAbsolutePath());
            BotConfig config = mapper.readValue(configFile, BotConfig.class);
            return config;
        } catch (IOException e) {
            log.error("Failed to read file", e);
        }

        return BotConfig.builder().build();
    }
}
