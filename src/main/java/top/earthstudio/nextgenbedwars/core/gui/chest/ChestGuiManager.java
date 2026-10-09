package top.earthstudio.nextgenbedwars.core.gui.chest;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import top.earthstudio.nextgenbedwars.api.BedwarsAPI;
import top.earthstudio.nextgenbedwars.api.gui.chest.ChestGuiPage;

import top.earthstudio.nextgenbedwars.core.gui.chest.listener.GuiInteractListener;

public class ChestGuiManager {
    private static GuiInteractListener guiInteractListener;

    private ChestGuiManager() {}

    static public void initialize() {
        guiInteractListener = new GuiInteractListener();
        Bukkit.getPluginManager().registerEvents(guiInteractListener, BedwarsAPI.getInstance().getPlugin());
    }

    static public void openFor(Player player, ChestGuiPage chestGuiPage) {
        ChestGuiHolder holder = new ChestGuiHolder(chestGuiPage);
        player.openInventory(holder.getInventory());
    }

    static public void shutdown() {
        HandlerList.unregisterAll(guiInteractListener);
        guiInteractListener = null;
    }
}
