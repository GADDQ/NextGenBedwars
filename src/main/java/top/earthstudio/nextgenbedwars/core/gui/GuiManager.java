package top.earthstudio.nextgenbedwars.core.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.gui.GuiPage;

import top.earthstudio.nextgenbedwars.core.gui.listener.GuiInteractListener;

public class GuiManager {
    private static GuiInteractListener guiInteractListener;

    private GuiManager() {}

    static public void initialize() {
        guiInteractListener = new GuiInteractListener();
        Bukkit.getPluginManager().registerEvents(guiInteractListener, BedwarsAPI.getInstance().getPlugin());
    }

    static public void openFor(Player player, GuiPage guiPage) {
        GuiHolder holder = new GuiHolder(guiPage);
        player.openInventory(holder.getInventory());
    }

    static public void shutdown() {
        HandlerList.unregisterAll(guiInteractListener);
        guiInteractListener = null;
    }
}
