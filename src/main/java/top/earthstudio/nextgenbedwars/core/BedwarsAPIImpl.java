package top.earthstudio.nextgenbedwars.core;

import org.bukkit.plugin.java.JavaPlugin;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.game.IGameSubSystem;
import top.earthstudio.nextgenbedwars.api.game.SubSystemConstructor;
import top.earthstudio.nextgenbedwars.api.gui.GuiManager;

import top.earthstudio.nextgenbedwars.core.game.GameManager;
import top.earthstudio.nextgenbedwars.core.gui.GuiManagerImpl;

public record BedwarsAPIImpl(JavaPlugin plugin) implements BedwarsAPI {
    @Override
    public <T extends IGameSubSystem> void registerSubSystem(Class<T> type, SubSystemConstructor<T> constructor) {
        GameManager.registerSubSystem(type, constructor);
    }

    @Override
    public GuiManager getGuiManager() {
        return GuiManagerImpl.get();
    }
}