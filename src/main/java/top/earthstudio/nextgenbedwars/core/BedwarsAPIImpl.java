package top.earthstudio.nextgenbedwars.core;

import org.bukkit.plugin.java.JavaPlugin;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;
import top.earthstudio.nextgenbedwars.api.game.SubSystemConstructor;
import top.earthstudio.nextgenbedwars.core.game.GameManager;

public final class BedwarsAPIImpl implements BedwarsAPI {
    private final JavaPlugin plugin;

    public BedwarsAPIImpl(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public JavaPlugin getPlugin() {
        return plugin;
    }

    @Override
    public <T extends IGameSubSystem> void registerSubSystem(Class<T> type, SubSystemConstructor<T> constructor) {
        GameManager.registerSubSystem(type, constructor);
    }
}