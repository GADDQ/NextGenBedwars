package top.earthstudio.nextgenbedwars.core.shop.listener;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.shop.ShopManager;

public class ShopEntityProtectListener implements Listener {
    private final ShopManager shopManager;
    private final GameInstance gameInstance;

    public ShopEntityProtectListener(ShopManager shopManager, GameInstance gameInstance) {
        this.shopManager = shopManager;
        this.gameInstance = gameInstance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShopWasTakenExploded(EntityDamageEvent event) {
        if (event.getEntity().getWorld() != gameInstance.getWorld())
            return;

        Entity entity = event.getEntity();
        if (!shopManager.contains(entity.getUniqueId()))
            return;

        event.setCancelled(true);
    }
}
