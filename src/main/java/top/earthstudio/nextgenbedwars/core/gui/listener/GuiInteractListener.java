package top.earthstudio.nextgenbedwars.core.gui.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

import top.earthstudio.nextgenbedwars.core.gui.GuiHolder;

public class GuiInteractListener implements Listener {
    @EventHandler
    public void onGuiClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof GuiHolder guiHolder) {
            event.setCancelled(true);

            if (event.getClickedInventory() == event.getInventory())
                guiHolder.handleClick(event.getSlot(), (Player) event.getWhoClicked(), event.getClick());
        }
    }

    @EventHandler
    public void onGuiDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof GuiHolder)
            event.setCancelled(true);
    }
}
