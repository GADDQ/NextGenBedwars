package top.earthstudio.nextgenbedwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.translation.GlobalTranslator;

import org.bstats.bukkit.Metrics;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.language.LanguageTranslator;
import top.earthstudio.nextgenbedwars.api.util.ticker.GlobalTicker;

import top.earthstudio.nextgenbedwars.core.BedwarsAPIImpl;
import top.earthstudio.nextgenbedwars.core.command.BedwarsCommand;
import top.earthstudio.nextgenbedwars.core.game.GameManager;
import top.earthstudio.nextgenbedwars.core.gui.GuiManagerImpl;
import top.earthstudio.nextgenbedwars.core.language.LanguageTranslatorImpl;
import top.earthstudio.nextgenbedwars.core.world.WorldManager;

import java.io.File;
import java.util.List;

public final class NextGenBedwars extends JavaPlugin{
    static private Metrics metrics;

    static public ComponentLogger logger;

    static private LanguageTranslator languageTranslator;

    @Override
    public void onEnable() {
        metrics = new Metrics(this, 34627); // bStats

        BedwarsAPI.register(new BedwarsAPIImpl(this));

        languageTranslator = new LanguageTranslatorImpl();

        logger = getComponentLogger();
        logger.info(Component.text("NextGenBedwars v0.2.0-Alpha, MIT Licence. Made by Earth_Studio with <3. Now starting up...").color(NamedTextColor.GREEN));

        extractLocaleResources();
        GlobalTranslator.translator().addSource(languageTranslator);

        GlobalTicker.initialize(this);
        WorldManager.initialize(this);
        GameManager.initialize();
        GuiManagerImpl.initialize(this);

        bootstrapDefaultConfigs();

        new BedwarsCommand(this);

        logger.info(Component.text("Startup!").color(NamedTextColor.GREEN));
    }

    @Override
    public void onDisable() {
        logger.info(Component.text("NextGenBedwars is shutting down now...").color(NamedTextColor.GREEN));

        GuiManagerImpl.shutdown();
        GameManager.shutdown();
        WorldManager.shutdown();
        GlobalTicker.shutdown();

        GlobalTranslator.translator().removeSource(languageTranslator);

        HandlerList.unregisterAll(this);

        logger.info(Component.text("Shutdown!").color(NamedTextColor.GREEN));
        logger = null;

        BedwarsAPI.unregister();

        metrics.shutdown();
        metrics = null;
    }

    private void bootstrapDefaultConfigs() {
        for (String path : List.of(
                "spawner_types.yml",
                "item_shop.yml",
                "Information/information_animations.yml",
                "Information/sidebar.yml",
                "Information/tab_header.yml",
                "Information/tab_footer.yml",
                "Information/player_name_prefix.yml",
                "Information/player_name_suffix.yml",
                "Information/player_name_under.yml"
        )) {
            if (!new File(getDataFolder(), path).exists()) {
                saveResource(path, false);
            }
        }
    }

    private void extractLocaleResources() {
        try (java.util.jar.JarFile jar = new java.util.jar.JarFile(getFile())) {
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                String name = entry.getName();
                if (!entry.isDirectory() && name.startsWith("Locale/") && name.endsWith(".yml")) {
                    File outFile = new File(getDataFolder(), name);
                    if (!outFile.exists()) {
                        saveResource(name, false);
                    }
                }
            }
        } catch (java.io.IOException e) {
            logger.error(Component.text("Failed to extract default Locale resources!").color(NamedTextColor.RED), e);
        }
    }
}
