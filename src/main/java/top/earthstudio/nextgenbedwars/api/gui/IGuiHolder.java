package top.earthstudio.nextgenbedwars.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.InventoryHolder;

public abstract class IGuiHolder implements InventoryHolder {
    public final IGuiPage guiPage;

    protected IGuiHolder(IGuiPage guiPage) {
        this.guiPage = guiPage;
    }

    abstract public void refreshInventory();
    abstract public void closeInventory();
    abstract public void handleClick(int slot, Player player, ClickType clickType);
}
