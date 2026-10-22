package top.earthstudio.nextgenbedwars.core.shop.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import top.earthstudio.nextgenbedwars.api.game.GameInstance;
import top.earthstudio.nextgenbedwars.api.shop.tier.ProductTierManager;
import top.earthstudio.nextgenbedwars.api.team.event.BedwarsPlayerDeadEvent;

public class ProductTierListener implements Listener {
    public final GameInstance gameInstance;
    public final ProductTierManager productTierManager;

    public ProductTierListener(GameInstance gameInstance, ProductTierManager productTierManager) {
        this.gameInstance = gameInstance;
        this.productTierManager = productTierManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDead(BedwarsPlayerDeadEvent event) {
        if (event.player.getWorld() != gameInstance.getWorld())
            return;

        productTierManager.downgradeAllTiers(event.player);
    }
}
