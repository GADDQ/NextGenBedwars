package top.earthstudio.nextgenbedwars.api.gui.chest;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

@FunctionalInterface
public interface ChestGuiAction {
    void execute(Player player, ClickType clickType);
}