package top.earthstudio.nextgenbedwars.core.language;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.translation.Translator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.language.LanguageTranslator;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LanguageTranslatorImpl implements LanguageTranslator {
    public Map<Locale, Map<String, Component>> languages;

    private static LanguageTranslator handler;

    public LanguageTranslatorImpl() {
        languages = new Object2ObjectOpenHashMap<>();
        reload();
        handler = this;
    }

    public static LanguageTranslator get() {
        return handler;
    }

    public void reload() {
        languages.clear();

        File localeRoot = new File(BedwarsAPI.getInstance().plugin().getDataFolder(), "Locale");
        File[] langDirs = localeRoot.listFiles(File::isDirectory);
        if (langDirs == null) return;

        for (File langDir : langDirs) {
            Locale locale = Translator.parseLocale(langDir.getName());
            if (locale == null) continue;

            File[] langFiles = langDir.listFiles((dir, name) -> name.endsWith(".yml"));
            if (langFiles == null) continue;

            Map<String, Component> localeMap = languages.computeIfAbsent(locale, k -> new Object2ObjectOpenHashMap<>());

            for (File langFile : langFiles) {
                String fileName = langFile.getName();
                String subNamespace = fileName.substring(0, fileName.length() - 4);
                String prefix = "nextgenbedwars." + subNamespace + ".";

                Map<String, Component> loaded = new LanguageConfigBase(langDir.getName() + "/" + fileName).load();
                if (loaded != null) {
                    loaded.forEach((key, value) -> localeMap.put(prefix + key, value));
                }
            }
        }
    }

    @Override
    public @Nullable Component translate(@NotNull TranslatableComponent component, @NotNull Locale locale) {
        Component template = get(locale, component.key());
        if (template == null) return null;

        List<TranslationArgument> args = component.arguments();
        if (args.isEmpty()) return template;

        return template.replaceText(builder -> builder
                .match(ARG_PATTERN)
                .replacement((matchResult, textBuilder) -> {
                    int index = Integer.parseInt(matchResult.group(1));
                    if (index >= 0 && index < args.size()) {
                        return args.get(index).asComponent();
                    }
                    return Component.text(matchResult.group(0));
                })
        );
    }

    @Override
    public boolean canTranslate(@NotNull String key, @NotNull Locale locale) {
        return get(locale, key) != null;
    }

    private Component get(Locale locale, String translationId) {
        Map<String, Component> map = languages.get(locale);
        if (map != null && map.containsKey(translationId)) {
            return map.get(translationId);
        }

        for (var entry : languages.entrySet()) {
            if (entry.getKey().getLanguage().equals(locale.getLanguage()) && entry.getValue().containsKey(translationId)) {
                return entry.getValue().get(translationId);
            }
        }

        Map<String, Component> fallback = languages.get(DEFAULT_LOCALE);
        return fallback != null ? fallback.get(translationId) : null;
    }
}
