package top.earthstudio.nextgenbedwars.api.config;

import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;

public final class ConfigurateHelper {
    public static final ConfigurationOptions OPTIONS = ConfigurationOptions.defaults()
            .serializers(ConfigurateSerializers.createCollection());

    public static YamlConfigurationLoader loader(File file) {
        return YamlConfigurationLoader.builder()
                .file(file)
                .nodeStyle(NodeStyle.BLOCK)
                .defaultOptions(OPTIONS)
                .build();
    }
}