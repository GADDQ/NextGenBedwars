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
import top.earthstudio.nextgenbedwars.api.util.ticker.GlobalTicker;

import top.earthstudio.nextgenbedwars.core.BedwarsAPIImpl;
import top.earthstudio.nextgenbedwars.core.command.BedwarsCommand;
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

    static private WorldReadyListener worldReadyListener;

    @Override
    public void onEnable() {
        metrics = new Metrics(this, 34627); // bStats

        BedwarsAPI.register(new BedwarsAPIImpl(this));

        logger = getComponentLogger();
        logger.info(Component.text("NextGenBedwars v1.0.0, MIT Licence. Made by Earth_Studio with <3. Now starting up...").color(NamedTextColor.GREEN));

        worldReadyListener = new WorldReadyListener();

        GlobalTicker.initialize(this);
        WorldManager.initialize(this);
        GameManager.initialize();
        GuiManagerImpl.initialize();

        registerListeners();

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
        saveResource("spawner_types.yml", false);
        saveResource("item_shop.yml", false);
    }
}
