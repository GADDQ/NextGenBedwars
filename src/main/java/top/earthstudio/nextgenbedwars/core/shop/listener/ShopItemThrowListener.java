package top.earthstudio.nextgenbedwars.core.shop.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import top.earthstudio.nextgenbedwars.api.PdcKeys;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;

public final class ShopItemThrowListener implements Listener {
    public final GameInstance gameInstance;

    public ShopItemThrowListener(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemThrow(PlayerDropItemEvent event) {
        if (event.getPlayer().getWorld() != gameInstance.getWorld())
            return;

        ItemStack item = event.getItemDrop().getItemStack();
        if (isNotThrowable(item)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrop(InventoryClickEvent event) {
        if (event.getWhoClicked().getWorld() != gameInstance.getWorld())
            return;

        if (event.getSlot() == -999 || event.getClick() == ClickType.DROP || event.getClick() == ClickType.CONTROL_DROP) {
            ItemStack cursor = event.getCursor();
            ItemStack current = event.getCurrentItem();
            if (isNotThrowable(cursor) || isNotThrowable(current)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isNotThrowable(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return false;
        var pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.has(PdcKeys.shop.throwable) && Boolean.FALSE.equals(pdc.get(PdcKeys.shop.throwable, PersistentDataType.BOOLEAN));
    }
}
