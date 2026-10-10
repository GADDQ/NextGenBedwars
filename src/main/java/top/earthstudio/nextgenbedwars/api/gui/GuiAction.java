package top.earthstudio.nextgenbedwars.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

@FunctionalInterface
public interface GuiAction {
    void execute(Player player, ClickType clickType);
}