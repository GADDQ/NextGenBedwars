package top.earthstudio.nextgenbedwars.core.shop.listener;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.gui.chest.ChestGuiOpener;
import top.earthstudio.nextgenbedwars.api.shop.ShopManager;

public class ShopInteractListener implements Listener {
    private final GameInstance gameInstance;
    private final ShopManager shopManager;

    public ShopInteractListener(ShopManager shopManager, GameInstance gameInstance) {
        this.shopManager = shopManager;
        this.gameInstance = gameInstance;
    }

    @EventHandler
    public void onShopUse(PlayerInteractAtEntityEvent event) {
        if (event.getHand() != org.bukkit.inventory.EquipmentSlot.HAND)
            return;

        Entity entity = event.getRightClicked();
        if (!shopManager.contains(entity.getUniqueId()))
            return;

        event.setCancelled(true);
        gameInstance.get(ChestGuiOpener.class).openFor(event.getPlayer(), shopManager.getShopGui(entity.getUniqueId()));
    }

    @EventHandler
    public void onShopWasAttacked(PrePlayerAttackEntityEvent event) {
        Entity entity = event.getAttacked();
        if (!shopManager.contains(entity.getUniqueId()))
            return;

        event.setCancelled(true);
    }
}
