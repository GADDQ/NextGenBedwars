package top.earthstudio.nextgenbedwars.core.game.listener;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import top.earthstudio.nextgenbedwars.api.game.GameInstance;

public class GameAdvancementListener implements Listener {
    private final GameInstance gameInstance;

    public GameAdvancementListener(GameInstance gameInstance) {
        this.gameInstance = gameInstance;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerGetAdvancement(PlayerAdvancementCriterionGrantEvent event) {
        if (event.getPlayer().getWorld() != gameInstance.getWorld())
            return;

        event.setCancelled(true);
    }
}
