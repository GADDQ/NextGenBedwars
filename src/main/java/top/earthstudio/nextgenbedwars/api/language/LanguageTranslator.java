package top.earthstudio.nextgenbedwars.api.language;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.translation.Translator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.regex.Pattern;

public interface LanguageTranslator extends Translator {
    Key TRANSLATOR_KEY = Key.key("nextgenbedwars", "translator");
    Pattern ARG_PATTERN = Pattern.compile("\\{(\\d+)}");
    Locale DEFAULT_LOCALE = Locale.ENGLISH;

    void reload();

    @Override
    default @NotNull Key name() {
        return TRANSLATOR_KEY;
    }

    @Override
    default @Nullable MessageFormat translate(@NotNull String key, @NotNull Locale locale) {
        return null;
    }
}