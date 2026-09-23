package top.earthstudio.nextgenbedwars;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.util.ticker.GlobalTicker;

import top.earthstudio.nextgenbedwars.core.BedwarsAPIImpl;
import top.earthstudio.nextgenbedwars.core.command.debug.DebugCommand;
import top.earthstudio.nextgenbedwars.core.game.GameManager;
import top.earthstudio.nextgenbedwars.core.world.WorldManager;
import top.earthstudio.nextgenbedwars.core.spawner.listener.SpawnerMergeListener;
import top.earthstudio.nextgenbedwars.core.world.listener.WorldReadyListener;

public final class NextGenBedwars extends JavaPlugin{
    static public ComponentLogger logger;

    static public WorldReadyListener worldReadyListener;

    static public DebugCommand debugCommand;

    @Override
    public void onEnable() {
        BedwarsAPI.register(new BedwarsAPIImpl(this));

        logger = getComponentLogger();
        logger.info(Component.text("NextGenBedwars v1.0.0, MIT Licence. Made by Earth_Studio with <3. Now starting up...").color(NamedTextColor.GREEN));

        worldReadyListener = new WorldReadyListener();

        GlobalTicker.initialize(this);
        WorldManager.initialize(this, worldReadyListener);

        GameManager.initialize(worldReadyListener);

        registerListeners();

        logger.info(Component.text("Startup!").color(NamedTextColor.GREEN));

        // --------DEBUG--------
        debugCommand = new DebugCommand(this);
    }

    @Override
    public void onDisable() {
        logger.info(Component.text("NextGenBedwars is shutting down now...").color(NamedTextColor.GREEN));

        debugCommand.shutdown();
        debugCommand = null;

        GameManager.shutdown();

        WorldManager.shutdown();

        GlobalTicker.shutdown();

        logger.info(Component.text("Shutdown!").color(NamedTextColor.GREEN));
        logger = null;

        BedwarsAPI.unregister();
    }

    private void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();

        pm.registerEvents(new SpawnerMergeListener(), this);

        pm.registerEvents(worldReadyListener, this);
    }
}
