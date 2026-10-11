package top.earthstudio.nextgenbedwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import org.bstats.bukkit.Metrics;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.config.Config;
import top.earthstudio.nextgenbedwars.api.config.ConfigManager;
import top.earthstudio.nextgenbedwars.api.util.ticker.GlobalTicker;

import top.earthstudio.nextgenbedwars.core.BedwarsAPIImpl;
import top.earthstudio.nextgenbedwars.core.command.debug.DebugCommand;
import top.earthstudio.nextgenbedwars.core.game.GameManager;
import top.earthstudio.nextgenbedwars.core.gui.GuiManagerImpl;
import top.earthstudio.nextgenbedwars.core.gui.listener.GuiInteractListener;
import top.earthstudio.nextgenbedwars.core.gui.listener.SpecialGuiFixListener;
import top.earthstudio.nextgenbedwars.core.spawner.listener.SpawnerMergeListener;
import top.earthstudio.nextgenbedwars.core.world.WorldManager;
import top.earthstudio.nextgenbedwars.core.world.listener.WorldReadyListener;

public final class NextGenBedwars extends JavaPlugin{
    static private Metrics metrics;

    static public ComponentLogger logger;

    static public WorldReadyListener worldReadyListener; // TODO: public for debug

    static public DebugCommand debugCommand; // TODO: DEBUG ONLY

    @Override
    public void onEnable() {
        metrics = new Metrics(this, 34627); // bStats

        BedwarsAPI.register(new BedwarsAPIImpl(this));

        logger = getComponentLogger();
        logger.info(Component.text("NextGenBedwars v1.0.0, MIT Licence. Made by Earth_Studio with <3. Now starting up...").color(NamedTextColor.GREEN));

        worldReadyListener = new WorldReadyListener();

        GlobalTicker.initialize(this);
        ConfigManager.initialize();
        WorldManager.initialize(this, worldReadyListener);
        GameManager.initialize(worldReadyListener);
        GuiManagerImpl.initialize();

        registerListeners();

        bootstrapDefaultConfigs();

        logger.info(Component.text("Startup!").color(NamedTextColor.GREEN));

        // --------DEBUG--------
        debugCommand = new DebugCommand(this);
    }

    @Override
    public void onDisable() {
        logger.info(Component.text("NextGenBedwars is shutting down now...").color(NamedTextColor.GREEN));

        debugCommand.shutdown();
        debugCommand = null;

        GuiManagerImpl.shutdown();
        GameManager.shutdown();
        WorldManager.shutdown();
        ConfigManager.shutdown();
        GlobalTicker.shutdown();

        HandlerList.unregisterAll(this);
        worldReadyListener = null;

        logger.info(Component.text("Shutdown!").color(NamedTextColor.GREEN));
        logger = null;

        BedwarsAPI.unregister();

        metrics.shutdown();
        metrics = null;
    }

    private void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();

        pm.registerEvents(new SpawnerMergeListener(), this);

        pm.registerEvents(worldReadyListener, this);
        pm.registerEvents(new GuiInteractListener(), this);
        pm.registerEvents(new SpecialGuiFixListener(), this);
    }

    private void bootstrapDefaultConfigs() {
        ConfigManager.bootstrap(new Config("spawner_types.yml"));
        ConfigManager.bootstrap(new Config("item_shop.yml"));
    }
}
