package top.earthstudio.nextgenbedwars.core.language;

import net.kyori.adventure.text.Component;

import org.jetbrains.annotations.NotNull;

import top.earthstudio.nextgenbedwars.api.config.IConfig;

import java.util.Map;

public class LanguageConfigBase extends IConfig<Map<String, Component>> {
    protected LanguageConfigBase(@NotNull String fileName) {
        super(null, "Locale/" + fileName, true, "translations");
    }

    @Override
    protected Map<String, Component> load() {
        return super.load();
    }
}
