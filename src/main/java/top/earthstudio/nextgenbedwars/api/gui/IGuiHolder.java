package top.earthstudio.nextgenbedwars.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryHolder;

public interface IGuiHolder extends InventoryHolder {
    void refreshInventory();
    void closeInventory();
    void handleClick(int slot, Player player, ClickType clickType);
}
