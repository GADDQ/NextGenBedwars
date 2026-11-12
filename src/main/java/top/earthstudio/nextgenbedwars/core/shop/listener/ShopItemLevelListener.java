package top.earthstudio.nextgenbedwars.core.shop.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;

import top.earthstudio.nextgenbedwars.api.PdcKeys;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.level.LevelManager;
import top.earthstudio.nextgenbedwars.api.shop.item.ShopItem;
import top.earthstudio.nextgenbedwars.api.respawn.event.BedwarsPlayerDeadEvent;

import top.earthstudio.nextgenbedwars.core.shop.item.ItemShopConfig;

public final class ShopItemLevelListener implements Listener {
    private final GameInstance gameInstance;
    private final LevelManager levelManager;
    private final ItemShopConfig itemShopConfig;

    public ShopItemLevelListener(GameInstance gameInstance, LevelManager levelManager, ItemShopConfig itemShopConfig) {
        this.gameInstance = gameInstance;
        this.levelManager = levelManager;
        this.itemShopConfig = itemShopConfig;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDead(BedwarsPlayerDeadEvent event) {
        if (event.player.getWorld() != gameInstance.getWorld())
            return;

        Player player = event.player;
        PlayerInventory inv = player.getInventory();

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType().isAir())
                continue;

            var meta = item.getItemMeta();
            if (meta == null)
                continue;

            var pdc = meta.getPersistentDataContainer();
            if (!pdc.has(PdcKeys.shop.downgradeWhenDead, PersistentDataType.BOOLEAN))
                continue;

            String itemId = pdc.get(PdcKeys.shop.itemId, PersistentDataType.STRING);
            if (itemId == null)
                continue;

            levelManager.downgrade(player.getUniqueId(), itemId, 1);
            int newLevel = levelManager.getLevel(player.getUniqueId(), itemId);

            ShopItem shopItem = itemShopConfig.getItem(itemId);
            if (shopItem != null) {
                ItemStack newItem = (shopItem.actualItems != null && !shopItem.actualItems.isEmpty())
                        ? shopItem.getActualItem(newLevel).clone()
                        : new ItemStack(shopItem.getShowItem(newLevel).getType(), shopItem.getShowItem(newLevel).getAmount());

                newItem.editMeta(itemMeta -> {
                    var newPdc = itemMeta.getPersistentDataContainer();
                    if (shopItem.isDecreaseLevelWhenPlayerDead)
                        newPdc.set(PdcKeys.shop.downgradeWhenDead, PersistentDataType.BOOLEAN, true);
                    if (shopItem.isKeepWhenDead)
                        newPdc.set(PdcKeys.shop.keepWhenDead, PersistentDataType.BOOLEAN, true);
                    if (!shopItem.isThrowable)
                        newPdc.set(PdcKeys.shop.throwable, PersistentDataType.BOOLEAN, false);
                    if (shopItem.id != null)
                        newPdc.set(PdcKeys.shop.itemId, PersistentDataType.STRING, shopItem.id);
                });

                inv.setItem(i, newItem);
            }
        }
    }
}