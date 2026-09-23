package top.earthstudio.nextgenbedwars.core;

import org.bukkit.plugin.java.JavaPlugin;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;

public final class BedwarsAPIImpl implements BedwarsAPI {
    private final JavaPlugin plugin;

    public BedwarsAPIImpl(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public JavaPlugin getPlugin() {
        return plugin;
    }
}