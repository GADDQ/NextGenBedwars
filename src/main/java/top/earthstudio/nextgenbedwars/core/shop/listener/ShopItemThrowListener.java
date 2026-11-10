package top.earthstudio.nextgenbedwars.core.shop.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
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
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked().getWorld() != gameInstance.getWorld())
            return;

        if (!isNotThrowable(event.getOldCursor()))
            return;

        Inventory top = event.getView().getTopInventory();
        if (!isExternalContainer(top, event.getWhoClicked()))
            return;

        int topSize = top.getSize();
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot < topSize) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked().getWorld() != gameInstance.getWorld())
            return;

        if (event.getSlot() == -999 || event.getClick() == ClickType.DROP || event.getClick() == ClickType.CONTROL_DROP) {
            ItemStack cursor = event.getCursor();
            ItemStack current = event.getCurrentItem();
            if (isNotThrowable(cursor) || isNotThrowable(current)) {
                event.setCancelled(true);
                return;
            }
        }

        Inventory top = event.getView().getTopInventory();
        Inventory clicked = event.getClickedInventory();

        if (event.isShiftClick()) {
            if (clicked != null && isPlayerInventory(clicked, event.getWhoClicked()) && isExternalContainer(top, event.getWhoClicked())) {
                if (isNotThrowable(event.getCurrentItem())) {
                    event.setCancelled(true);
                    return;
                }
            }
        }


        if (clicked != null && isExternalContainer(clicked, event.getWhoClicked())) {
            if (isNotThrowable(event.getCursor())) {
                event.setCancelled(true);
                return;
            }

            if (event.getClick() == ClickType.NUMBER_KEY) {
                int hotbarSlot = event.getHotbarButton();
                if (hotbarSlot >= 0 && hotbarSlot < 9) {
                    ItemStack hotbarItem = event.getWhoClicked().getInventory().getItem(hotbarSlot);
                    if (isNotThrowable(hotbarItem)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    private boolean isExternalContainer(Inventory inventory, org.bukkit.entity.HumanEntity player) {
        if (inventory == null) return false;

        if (inventory.equals(player.getInventory())) return false;

        return true;
    }

    private boolean isPlayerInventory(Inventory inventory, org.bukkit.entity.HumanEntity player) {
        return inventory != null && inventory.equals(player.getInventory());
    }

    private boolean isNotThrowable(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return false;
        var pdc = item.getItemMeta().getPersistentDataContainer();
        return pdc.has(PdcKeys.shop.throwable) && Boolean.FALSE.equals(pdc.get(PdcKeys.shop.throwable, PersistentDataType.BOOLEAN));
    }
}
