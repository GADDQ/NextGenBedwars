package top.earthstudio.nextgenbedwars.core.game.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.bukkit.event.inventory.CraftItemEvent;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;

public final class GameCraftingListener implements Listener {
    private final GameInstance gameInstance;

    public GameCraftingListener(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true) // TODO: Configurable
    public void onPlayerCrafting(CraftItemEvent event) {
        if (event.getWhoClicked().getWorld() != gameInstance.getWorld())
            return;

        event.setCancelled(true);
    }
}
